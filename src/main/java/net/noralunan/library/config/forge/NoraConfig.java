package net.noralunan.library.config.forge;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ModConfig;
import net.noralunan.library.config.ConfigStackException;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
            if (super.comment.isEmpty()) super.comment.append(" ");
            else super.comment.append("\n ");

            super.comment.append(depict.replace("\n", "\n "));
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
                    if (super.comment.isEmpty()) super.comment.append(" ");
                    else super.comment.append("\n ");

                    super.comment.append(single.replace("\n", "\n "));
                }
            }
        }
        return this;
    }

    public NoraConfig key(String key)
    {
        super.builder.translation(key);
        return this;
    }

    public NoraConfig restart()
    {
        this.builder.worldRestart();
        return this;
    }

    public NoraConfig header(String name, String version, ModConfig.Type type)
    {
        super.builder.comment(BannerUtil.header(name, version, type, super.comment.toString()));
        super.comment.setLength(0);
        return this;
    }

    public StackBuilder layer(String path)
    {
        if (!super.comment.isEmpty())
        {
            super.builder.comment(BannerUtil.layer(super.comment.toString(), this.depth));
            super.comment.setLength(0);
        }
        super.builder.push(path);
        this.depth++;
        return new StackBuilder(this);
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

    public static class StackBuilder
    {
        private final NoraConfig context;
        private StackBuilder(NoraConfig context)
        {
            this.context = context;
        }

        public void nest(Consumer<NoraConfig> group)
        {
            group.accept(this.context);
            this.context.out();
        }

        public void nest(Runnable group)
        {
            group.run();
            this.context.out();
        }

        public <T> T nest(Function<NoraConfig, T> group)
        {
            T result = group.apply(this.context);
            this.context.out();
            return result;
        }

        public <T> T nest(Supplier<T> group)
        {
            T result = group.get();
            this.context.out();
            return result;
        }
    }

    public NoraConfig lineBreak()
    {
        super.row = true;
        return this;
    }

    public NoraConfig noSuffix()
    {
        super.suffix = false;
        return this;
    }

    public ListBuilder list(boolean allowEmpty)
    {
        return new ListBuilder(this, allowEmpty);
    }

    public ListBuilder list()
    {
        return list(false);
    }

    public ListBuilder allowEmpty()
    {
        return list(true);
    }

    public ListBuilder validator(Predicate<Object> validator)
    {
        return new ListBuilder(this, validator);
    }

    public ListBuilder validator(Class<?> validator)
    {
        return validator(validator::isInstance);
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
        return new IntBuilder(this, min, max);
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
        return new LongBuilder(this, min, max);
    }

    public DoubleBuilder min(double value)
    {
        return range(value, Double.POSITIVE_INFINITY);
    }

    public DoubleBuilder max(double value)
    {
        return range(Double.NEGATIVE_INFINITY, value);
    }

    public DoubleBuilder range(double min, double max)
    {
        return new DoubleBuilder(this, min, max);
    }

    public <T> OptionBuilder<T> in(List<T> option)
    {
        return new OptionBuilder<>(option, this);
    }

    @SafeVarargs
    public final <T> OptionBuilder<T> in(T... option)
    {
        return new OptionBuilder<>(Arrays.stream(option).toList(), this);
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
                            depict,
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
                            depict,
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
