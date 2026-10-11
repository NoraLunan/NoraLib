package net.noralunan.library.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.function.Function;

public class NoraConfig extends NoraConfigBuilder
{
    private ForgeConfigSpec spec;
    private int depth;
    private NoraConfig()
    {
        this.spec = null;
        this.depth = 0;
    }

    public static <T> Pair<T, ForgeConfigSpec> create(Function<NoraConfig, T> consumer)
    {
        NoraConfig config = new NoraConfig();
        T object = consumer.apply(config);

        if (config.depth != 0)
        {
            throw new ConfigStackException("Config stack not empty!");
        }
        if (config.spec == null)
        {
            config.spec = config.builder.build();
        }
        return Pair.of(object, config.spec);
    }

    public NoraConfig depict(String depict)
    {
        if (depict != null)
        {
            super.comment.append(" ").append(depict.replace("\n", "\n "));
        }
        return this;
    }

    public NoraConfig depict(String... depict)
    {
        if (depict != null)
        {
            for (String single : depict)
            {
                if (single != null)
                {
                    super.comment.append(" ").append(single.replace("\n", "\n "));
                }
            }
        }
        return this;
    }

    public NoraConfigBuilder key(String key)
    {
        super.builder.translation(key);
        return this;
    }

    public NoraConfig header(String name, String version, ModConfig.Type type)
    {
        super.builder.comment(BannerUtil.header(name, version, type, super.comment.toString()));
        super.comment.setLength(0);
        return this;
    }

    public void layer(String path)
    {
        if (!super.comment.isEmpty())
        {
            super.builder.comment(BannerUtil.layer(super.comment.toString(), this.depth));
            super.comment.setLength(0);
        }
        super.builder.push(path);
        this.depth++;
    }

    public void out()
    {
        if (this.depth < 0) throw new ConfigStackException("Config stack underflow!");
        super.builder.pop();
        this.depth--;
    }

    public void out(int count)
    {
        if (this.depth < 0)
            throw new ConfigStackException("Config stack underflow!");
        else if (count < 0)
            throw new IllegalArgumentException("Attempted to pass a negative count value!");
        else if (count > this.depth)
            throw new ConfigStackException("Target out count exceeds current config stack depth!");
        else
        {
            super.builder.pop(count);
            this.depth -= count;
        }
    }

    public void root()
    {
        if (this.depth < 0) throw new ConfigStackException("Config stack underflow!");

        super.builder.pop(this.depth);
        this.depth = 0;
    }

    public NoraConfigBuilder lineBreak()
    {
        super.row = true;
        return this;
    }

    public NoraConfigBuilder noSuffix()
    {
        super.suffix = false;
        return this;
    }

    public IntBuilder min(int value)
    {
        return range(value, Integer.MAX_VALUE);
    }

    public IntBuilder max(int value)
    {
        return range(Integer.MIN_VALUE, value);
    }

    public IntBuilder range(int min, int max)
    {
        return new IntBuilder(min, max);
    }

    public LongBuilder min(long value)
    {
        return range(value, Long.MAX_VALUE);
    }

    public LongBuilder max(long value)
    {
        return range(Long.MIN_VALUE, value);
    }

    public LongBuilder range(long min, long max)
    {
        return new LongBuilder(min, max);
    }

    public DoubleBuilder min(double value)
    {
        return range(value, Double.NEGATIVE_INFINITY);
    }

    public DoubleBuilder max(double value)
    {
        return range(Double.POSITIVE_INFINITY, value);
    }

    public DoubleBuilder range(double min, double max)
    {
        return new DoubleBuilder(min, max);
    }

    public <T> OptionBuilder<T> in(List<T> option)
    {
        return new OptionBuilder<>(option, super.comment);
    }

    private static class BannerUtil
    {
        private static String[] header(String name, String version, ModConfig.Type type, String depict)
        {
            String target;
            switch (type)
            {
                case SERVER -> target = "Server";
                case CLIENT -> target = "Client";
                case COMMON -> target = "Common";
                default -> throw new IllegalArgumentException();
            }
            return new String[]
                    {
                            banner("=", 0),
                            " Mod: " + name,
                            " Version: " + version,
                            " ConfigType: " + target,
                            banner("-", 0),
                            " " + depict,
                            banner("=", 0)
                    };
        }

        private static String[] layer(String depict, int indent)
        {
            if (indent < 0) throw new IllegalArgumentException();
            if (depict == null || depict.isBlank()) return new String[]{ banner("=", indent) };

            return new String[]
                    {
                            banner("=", indent),
                            " " + depict,
                            banner("=", indent)
                    };
        }

        private static String banner(String symbol, int indent)
        {
            int count = Math.max(0, 128 - indent * 4);
            return symbol.repeat(count) + "#";
        }
    }
}
