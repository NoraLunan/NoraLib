package net.noralunan.library.config.forge;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import net.minecraftforge.common.ForgeConfigSpec.Builder;
import net.minecraftforge.common.ForgeConfigSpec.ConfigValue;
import net.noralunan.library.config.SuffixUtil;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@SuppressWarnings("removal")
@Deprecated(since = "0.2.18", forRemoval = true)
class NoraConfigBuilder
{
    protected final Builder builder;
    protected final StringBuilder comment;
    protected boolean suffix;
    protected boolean row;

    protected NoraConfigBuilder()
    {
        this.builder = new Builder();
        this.comment = new StringBuilder();
        this.suffix = true;
        this.row = false;
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
    public <E extends Enum<E>> ConfigValue<String> entry(String key, E def)
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
            suffix = (this.comment.isEmpty() ? " " : "\n ") + SuffixUtil.defNewRow(def);
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
        protected IntBuilder(NoraConfig context, Integer min, Integer max) {
            super(context, min, max);
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

    public static class LongBuilder extends NumberBuilder<Long>
    {
        protected LongBuilder(NoraConfig context, Long min, Long max) {
            super(context, min, max);
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
        protected DoubleBuilder(NoraConfig context, Double min, Double max) {
            super(context, min, max);
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

    public static abstract class NumberBuilder<N extends Number>
    {
        private final NoraConfigBuilder context;
        protected final N min;
        protected final N max;

        protected abstract N getMin();

        protected abstract N getMax();

        protected abstract Predicate<Object> verify();

        private NumberBuilder(NoraConfigBuilder context, N min, N max)
        {
            this.context = context;
            this.min = min;
            this.max = max;
        }

        public ConfigValue<N> entry(String key, N def)
        {
            String suffix;
            if (this.context.row)
            {
                suffix = (this.context.comment.isEmpty() ? " " : "\n ") + defNewRow(def, this.min, this.max);
                this.context.row = false;
            }
            else
                suffix = defSuffix(def, this.min, this.max);

            return this.context.entry(key, def, verify(), suffix);
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

    public static class ListBuilder
    {
        private final NoraConfigBuilder context;
        private final boolean empty;
        private Predicate<Object> validator;

        protected ListBuilder(NoraConfig context, boolean allowEmpty)
        {
            this.context = context;
            this.empty = allowEmpty;
            this.validator = Objects::nonNull;
        }

        protected ListBuilder(NoraConfig context, Predicate<Object> validator)
        {
            this.context = context;
            this.empty = false;
            this.validator = validator;
        }

        public ListBuilder validator(Predicate<Object> validator)
        {
            this.validator = this.validator.and(validator);
            return this;
        }

        public ListBuilder validator(Class<?> validator)
        {
            return validator(validator::isInstance);
        }

        private static final Splitter DOT_SPLITTER = Splitter.on(".");
        public <T> ConfigValue<List<? extends T>> entry(String key, List<T> def)
        {
            StringBuilder comment = this.context.comment;
            boolean isEmpty = comment.isEmpty();
            boolean isLine = this.context.row;

            if (this.context.suffix)
            {
                String prefix = "";
                if (isEmpty) prefix = " ";
                else if (isLine) prefix = "\n ";

                String suffix = (isEmpty || isLine) ? SuffixUtil.defNewRow(def) : SuffixUtil.defSuffix(def);

                comment.append(prefix).append(suffix);
            }

            if (!isEmpty)
            {
                this.context.builder.comment(comment.toString());
                comment.setLength(0);
                if (isLine)
                {
                    this.context.row = false;
                }
            }

            if (this.empty)
                return this.context.builder
                        .defineListAllowEmpty(Lists.newArrayList(DOT_SPLITTER.split(key)), () -> def, this.validator); // 兼容旧版Forge
            else
                return this.context.builder.defineList(key, def, this.validator);
        }
    }

    public static class OptionBuilder<T>
    {
        private final List<T> option;
        private final NoraConfigBuilder context;

        protected OptionBuilder(List<T> option, NoraConfig context)
        {
            this.option = option;
            this.context = context;
        }

        public ConfigValue<T> entry(String key, T def)
        {
            String depict = combine(def, this.option, this.context.comment, this.context.row, this.context.suffix);
            this.context.comment.setLength(0);
            return this.context.builder.comment(depict).defineInList(key, def, this.option);
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

}
