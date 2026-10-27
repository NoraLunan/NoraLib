package net.noralunan.library.config;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.ParsingException;
import net.minecraftforge.fml.loading.FMLPaths;
import net.noralunan.library.NoraLib;
import net.noralunan.library.common.NoraLogger;
import net.noralunan.library.util.FileUtil;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class SimpleConfig
{
    static final NoraLogger LOGGER = NoraLib.logger();
    static final Consumer<SimpleConfig> CORRECT = config -> {
        if (!config.correct)
        {
            config.correct = true;
            LOGGER.warn("Configuration file {} is incorrect, correcting", config.file.getPath());
        }
    };

    public static <T> Pair<T, SimpleConfig> create(Function<ConfigBuilder, T> function)
    {
        return builder().beautify().create(function);
    }

    public static Context builder()
    {
        return new Context();
    }

    public static void register(SimpleConfig config, String path)
    {
        register(config, Path.of(path));
    }

    public static void register(SimpleConfig config, Path path)
    {
        if (!path.endsWith(".toml"))
            throw new IllegalArgumentException("Must be a Toml file");

        Path pth = path.isAbsolute() ? path : FMLPaths.CONFIGDIR.get().resolve(path);
        pth = pth.toAbsolutePath().normalize();

        config.file = pth.toFile();
        config.config = CommentedFileConfig
                .builder(config.file)
                .autosave()
                .preserveInsertionOrder()
                .build();

        NoraLib.confirmNotNull(config.depictMap, config.entries);

        if (!config.file.exists() && !FileUtil.create(config.file))
        {
            LOGGER.error("Failed to initialize the config file: {}", config.file.getPath());
            return;
        }
        config.init();
    }

    public static class Context
    {
        private boolean beautify = false;
        private boolean noSuffix = false;
        private boolean newRow = false;

        private Context() {}

        public <T> Pair<T, SimpleConfig> create(Function<ConfigBuilder, T> function)
        {
            ConfigBuilder builder = new ConfigBuilder(this.noSuffix, this.newRow);
            T obj = function.apply(builder);
            SimpleConfig config = builder.build();
            config.beautify = this.beautify;
            return Pair.of(obj, config);
        }

        public Context beautify()
        {
            this.beautify = true;
            return this;
        }

        public Context removeAllSuffix()
        {
            this.noSuffix = true;
            return this;
        }

        public Context suffixAlwaysNewLine()
        {
            this.newRow = true;
            return this;
        }
    }

    private final Map<String, String> depictMap;
    private final List<ConfigEntry<?>> entries;
    File file;
    CommentedFileConfig config;
    boolean correct;
    boolean beautify;

    private final List<Runnable> listener;

    private final Runnable overrideComment;
    private final Runnable read;
    private final Runnable save;

    SimpleConfig(Map<String, String> depictMap, List<ConfigEntry<?>> entries)
    {
        this.depictMap = depictMap;
        this.entries = entries;
        this.correct = false;
        this.listener = new ArrayList<>();

        this.overrideComment = () -> this.depictMap.forEach((path, depict) -> {
            NoraLib.confirmNotNull(path, depict);
            String comment = this.config.getComment(path);
            if (comment == null || !comment.equals(depict))
            {
                CORRECT.accept(this);
                this.config.setComment(path, depict);
            }
        });
        this.read = () -> this.entries.forEach(ConfigEntry::maintain);
        this.save = () -> protect(() -> this.entries.stream()
                .filter(entry -> entry.change)
                .forEach(entry -> {
                    this.config.set(entry.key, entry.get());
                    entry.change = false;
                })
        );
    }

    public void addListener(Runnable callback)
    {
        this.listener.add(callback);
    }

    public void addListener(List<Runnable> callback)
    {
        this.listener.addAll(callback);
    }

    public void removeListener(Runnable target)
    {
        this.listener.remove(target);
    }

    public void removeListener(int index)
    {
        this.listener.remove(index);
    }

    public void removeListener(List<Runnable> target)
    {
        this.listener.removeAll(target);
    }

    public void clearListener()
    {
        this.listener.clear();
    }

    private synchronized void init()
    {
        reload();
    }

    public synchronized void reload()
    {
        protect(this.read);
        this.listener.forEach(Runnable::run);
    }

    public synchronized void save()
    {
        protect(this.save);
    }

    private synchronized void protect(Runnable execute)
    {
        String filePath = this.file.getPath();
        try {
            this.config.load();

            List<String> invalid = new ArrayList<>();
            Set<String> keys = new HashSet<>();
            collect(this.config, "", keys);
            keys.stream().filter(key -> !this.depictMap.containsKey(key)).forEach(invalid::add);
            if (!invalid.isEmpty())
            {
                CORRECT.accept(this);
                invalid.forEach(this.config::remove);
            }

            this.overrideComment.run();

            execute.run();
        }
        catch (ParsingException exception) {
            int sequence = 0;
            File bak;

            int index = filePath.lastIndexOf(".");
            String name = filePath.substring(0, index) + "%s" + filePath.substring(index) + ".bak";

            do { bak = new File(String.format(name, ++sequence)); }
            while (bak.exists());

            FileUtil.create(bak);
            try {
                Files.copy(this.file.toPath(), bak.toPath(), StandardCopyOption.REPLACE_EXISTING);
                this.config.clear();

                FileUtil.write(this.file, "");
                this.overrideComment.run();
                this.read.run();

                LOGGER.error("Configuration file {} format is incorrect, has been regenerated.", filePath);
            }
            catch (IOException io) {
                LOGGER.error("Failed to back up the configuration file: {}", io);
            }
        }
        finally {
            this.config.save();
            this.config.close();
            this.correct = false;
        }

        if (this.beautify)
        {
            try {
                Path path = Paths.get(filePath);
                List<String> content = Files.readAllLines(path);
                List<String> lines = beautify(content);
                FileUtil.write(this.file, lines);
            }
            catch (IOException exception) {
                LOGGER.error("Failed to beautify the file: {}", exception);
            }
        }
    }

    private void collect(Config config, String prefix, Set<String> result)
    {
        config.entrySet().forEach(entry -> {
            String key = entry.getKey();
            Object value = entry.getValue();

            String path = prefix.isEmpty() ? key : prefix + "." + key;

            if (value instanceof Config cfg)
                collect(cfg, path, result);
            else
                result.add(path);
        });
    }

    private List<String> beautify(List<String> lines)
    {
        List<String> list = new ArrayList<>();
        String indent = "\t";

        for (int i = 0; i < lines.size(); i++)
        {
            String line = lines.get(i);
            String trimmed = line.trim();

            if (trimmed.contains("=") && trimmed.contains("[") && trimmed.contains("]"))
            {
                String formatted = tryFormat(line, indent);

                if (formatted != null)
                {
                    list.addAll(Arrays.asList(formatted.split("\n")));
                    list.add("");
                    continue;
                }
            }

            list.add(line);

            boolean comment = trimmed.startsWith("#");
            boolean layer = trimmed.startsWith("[") && trimmed.endsWith("]");
            boolean empty = trimmed.isEmpty();
            boolean entry = !comment && !layer && !empty && trimmed.contains("=");

            if (entry && i + 1 < lines.size())
            {
                String next = lines.get(i + 1).trim();
                if (!next.isEmpty())
                {
                    list.add("");
                }
            }
        }

        int[] array = IntStream.range(0, list.size())
                .filter(index -> index != 0)
                .filter(index -> list.get(index).isEmpty())
                .filter(index -> list.get(index - 1).isEmpty())
                .toArray();

        List<Integer> illegal = Arrays.stream(array)
                .boxed()
                .sorted((a, b) -> Integer.compare(b, a))
                .toList();

        illegal.forEach(index -> list.remove((int) index));

        return list;
    }

    @Nullable
    private String tryFormat(String line, String indent)
    {
        String prefix = "";

        Matcher matcher = Pattern.compile("^(\\t+)").matcher(line);
        if (matcher.find()) prefix = matcher.group(1);

        int index = line.indexOf('=');
        if (index == -1) return null;

        String keyPart = line.substring(0, index).trim();
        String valuePart = line.substring(index + 1).trim();

        if (!valuePart.startsWith("[") || !valuePart.endsWith("]"))
        {
            return null;
        }

        String content = valuePart.substring(1, valuePart.length() - 1).trim();

        if (content.isEmpty()) return prefix + keyPart + " = [\n" + prefix + "]";

        String[] elements = parse(content);

        StringBuilder builder = new StringBuilder();
        builder.append(prefix).append(keyPart).append(" = [\n");

        for (int i = 0; i < elements.length; i++)
        {
            String single = elements[i].trim();
            if (!single.isEmpty())
            {
                builder.append(prefix).append(indent).append(single);
                if (i < elements.length - 1)
                {
                    builder.append(",");
                }
                builder.append("\n");
            }
        }

        builder.append(prefix).append("]");

        return builder.toString();
    }

    public String[] parse(String string)
    {
        List<String> result = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean in = false;
        int backslash = 0;

        for (int i = 0; i < string.length(); i++)
        {
            char character = string.charAt(i);

            if (character == '\\')
            {
                backslash++;
                field.append(character);
                continue;
            }

            if (character == '"')
            {
                if (backslash % 2 == 0)
                {
                    in = !in;
                }
                field.append(character);
            }
            else if (character == ',' && !in)
            {
                result.add(field.toString());
                field.setLength(0);
            }
            else
            {
                field.append(character);
            }

            backslash = 0;
        }

        result.add(field.toString());
        return result.toArray(new String[0]);
    }
}
