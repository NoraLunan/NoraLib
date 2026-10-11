package net.noralunan.library.config;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class NoraConfigBuilder {
    protected final Builder builder;
    protected StringBuilder comment;
    protected boolean suffix;
    protected boolean row;

    protected NoraConfigBuilder()
    {
        this.builder = new Builder();
        this.comment = new StringBuilder();
        this.suffix = true;
        this.row = false;
    }

    public NoraConfigBuilder restart()
    {
        this.builder.worldRestart();
        return this;
    }

    private <T> ConfigValue<T> entry(String key, T def, Predicate<Object> validator, String suffix)
    {
        if (this.suffix)
        {
            this.comment.append(suffix);
        }
        else this.suffix = true;

        if (!this.comment.isEmpty())
        {
            this.builder.comment(this.comment.toString());
        }

        this.comment.setLength(0);

        return this.builder.define(key, def, validator);
    }

    @SuppressWarnings("unchecked")
    public <E extends Enum<E>> ConfigValue<String> entryIn(String key, E def)
    {
        Class<E> clazz = (Class<E>) def.getClass();

        List<String> options = Arrays.stream(
                Arrays.stream(clazz.getEnumConstants())
                        .map(Enum::name)
                        .toArray(String[]::new)
        ).toList();

        String depict = OptionBuilder.combine(def.name(), options, this.comment, this.row, this.suffix);
        this.comment.setLength(0);

        return this.builder.comment(depict).defineInList(key, def.name(), options);
    }

    public <T> ConfigValue<T> entry(String key, T def, Predicate<Object> verify)
    {
        String suffix;
        if (this.row)
        {
            suffix = "\n " + SuffixUtil.defNewRow(def);
            this.row = false;
        }
        else
            suffix = SuffixUtil.defSuffix(def);
        return entry(key, def, verify, suffix);
    }

    public <T> ConfigValue<T> entry(String key, T def)
    {
        return entry(key, def, Objects::nonNull);
    }

    public static class IntBuilder extends NumberBuilder<Integer>
    {
        protected IntBuilder(Integer min, Integer max)
        {
            super(min, max);
        }

        @Override
        protected Integer getMin()
        {
            return Integer.MIN_VALUE;
        }

        @Override
        protected Integer getMax()
        {
            return Integer.MAX_VALUE;
        }

        @Override
        protected Predicate<Object> verify()
        {
            return object -> object instanceof Integer value && value >= super.min && value <= super.max;
        }
    }

    public static class LongBuilder extends NumberBuilder<Long> {
        protected LongBuilder(Long min, Long max)
        {
            super(min, max);
        }

        @Override
        protected Long getMin()
        {
            return Long.MIN_VALUE;
        }

        @Override
        protected Long getMax()
        {
            return Long.MAX_VALUE;
        }

        @Override
        protected Predicate<Object> verify()
        {
            return object -> object instanceof Long value && value >= super.min && value <= super.max;
        }
    }

    public static class DoubleBuilder extends NumberBuilder<Double>
    {
        protected DoubleBuilder(Double min, Double max)
        {
            super(min, max);
        }

        @Override
        protected Double getMin()
        {
            return Double.NEGATIVE_INFINITY;
        }

        @Override
        protected Double getMax()
        {
            return Double.POSITIVE_INFINITY;
        }

        @Override
        protected Predicate<Object> verify()
        {
            return object -> object instanceof Double value && value >= super.min && value <= super.max;
        }
    }

    public static abstract class NumberBuilder<N extends Number> extends NoraConfigBuilder
    {
        protected N min;
        protected N max;

        protected abstract N getMin();

        protected abstract N getMax();

        protected abstract Predicate<Object> verify();

        private NumberBuilder(N min, N max)
        {
            this.min = min;
            this.max = max;
        }

        public ConfigValue<N> entryRange(String key, N def)
        {
            String suffix;
            if (super.row)
            {
                suffix = "\n " + defNewRow(def, this.min, this.max);
                super.row = false;
            }
            else
                suffix = defSuffix(def, this.min, this.max);

            return super.entry(key, def, verify(), suffix);
        }

        private String defSuffix(N def, N min, N max)
        {
            return " [" + defNewRow(def, min, max) + "]";
        }

        private String defNewRow(N def, N min, N max)
        {
            if (Objects.equals(min, getMax()) || Objects.equals(max, getMin()))
            {
                return SuffixUtil.format(def, "INVALID");
            }

            return verify(def, min, max, Objects.equals(min, getMin()), Objects.equals(max, getMax()));
        }

        private String verify(N value, N min, N max, boolean lower, boolean upper)
        {
            if (lower && upper)
                return SuffixUtil.format(value, null);
            else if (lower)
                return SuffixUtil.format(value, "< " + max);
            else if (upper)
                return SuffixUtil.format(value, "> " + min);
            else
                return SuffixUtil.format(value, min + " ~ " + max);
        }
    }

    public static class ListBuilder extends NoraConfigBuilder
    {
        private final boolean empty;

        protected ListBuilder(boolean allowEmpty)
        {
            this.empty = allowEmpty;
        }

        private static final Splitter DOT_SPLITTER = Splitter.on(".");
        public <T> ConfigValue<List<? extends T>> entry(String key, List<T> def, Predicate<Object> validator)
        {
            if (this.empty)
                return super.builder.defineListAllowEmpty(Lists.newArrayList(DOT_SPLITTER.split(key)), () -> def, validator);
            else
                return super.builder.defineList(key, def, validator);
        }

        public <T> ConfigValue<List<? extends T>> entryList(String key, List<T> def, Class<?> validator)
        {
            return entry(key, def, validator::isInstance);
        }

        public <T> ConfigValue<List<? extends T>> entryList(String key, List<T> def)
        {
            Predicate<Object> validator = def.isEmpty() ? Objects::nonNull : object ->  def.get(0).getClass().isInstance(object);
            return entry(key, def, validator);
        }
    }

    public static class OptionBuilder<T> extends NoraConfigBuilder
    {
        private final List<T> option;

        protected OptionBuilder(List<T> option, StringBuilder comment)
        {
            this.option = option;
            this.comment = comment;
        }

        public ConfigValue<T> entryIn(String key, T def)
        {
            String depict = combine(def, this.option, super.comment, super.row, super.suffix);
            super.comment.setLength(0);
            return super.builder.comment(depict).defineInList(key, def, this.option);
        }

        private static <T> String combine(T def, List<T> options, StringBuilder depict, boolean line, boolean suffix)
        {
            if (!options.contains(def))
            {
                String exception = "Default value " + def + " is not in the options: " + options;
                throw new IllegalArgumentException(exception);
            }
            StringBuilder result;
            String string = options
                    .stream()
                    .map(Objects::toString)
                    .collect(Collectors.joining(", "));
            if (depict == null || depict.isEmpty())
            {
                result = new StringBuilder();
                if (suffix) result.append(" ").append(SuffixUtil.defNewRow(def)).append("\n");
                result.append(" Options: ").append(string);
            }
            else
            {
                result = new StringBuilder(depict);
                if (suffix)
                {
                    if (line)
                        result.append("\n ").append(SuffixUtil.defNewRow(def));
                    else
                        result.append(SuffixUtil.defSuffix(def));
                }
                result.append("\n Options: ").append(string);
            }

            return result.toString();
        }
    }

    private static class SuffixUtil
    {
        private static String defSuffix(Object value)
        {
            return " [" + format(value, null) + "]";
        }

        private static String defNewRow(Object value)
        {
            return format(value, null);
        }

        private static String format(Object value, @Nullable String range)
        {
            if (range == null)
            {
                return String.format("Default: %s", value);
            }
            return String.format("Range: %s, Default: %s", range, value);
        }
    }
}
