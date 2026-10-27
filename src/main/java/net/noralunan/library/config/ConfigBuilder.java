package net.noralunan.library.config;

import net.noralunan.library.config.ConfigEntry.*;

import java.util.*;
import java.util.function.*;
import java.util.stream.Collectors;

@SuppressWarnings({"unchecked", "UnusedReturnValue", "unused"})
public class ConfigBuilder
{
    private SimpleConfig config;
    private final List<String> layers;
    String path;
    private int depth;
    final Map<String, String> depictMap;
    final List<ConfigEntry<?>> entries;

    final boolean suffixGlobal;
    final boolean rowGlobal;

    public ConfigBuilder()
    {
        this(false, false);
    }

    ConfigBuilder(boolean noSuffix, boolean newRow)
    {
        this.config = null;
        this.layers = new ArrayList<>();
        this.path = "";
        this.depth = 0;
        this.depictMap = new LinkedHashMap<>();
        this.entries = new ArrayList<>();
        this.suffixGlobal = !noSuffix;
        this.rowGlobal = newRow;
    }

    public SimpleConfig build()
    {
        if (this.depth != 0)
            throw new ConfigStackException("Config stack not empty!");

        if (this.config == null)
        {
            this.config = new SimpleConfig(this.depictMap, this.entries);
            this.entries.forEach(entry -> entry.standard = this.config);
        }

        return this.config;
    }

    public StackBuilder enter(String path)
    {
        this.layers.add(path + ".");
        this.path = String.join("", this.layers);
        this.depth++;
        return new StackBuilder(this);
    }

    public void out()
    {
        if (this.depth < 0) throw new ConfigStackException("Config stack underflow!");
        this.layers.remove(this.layers.size() - 1);
        this.path = String.join("", this.layers);
        this.depth--;
    }

    public void out(int count)
    {
        if (count < 0) throw new IllegalArgumentException("Attempted to pass a negative count value!");
        if (count > this.depth) throw new ConfigStackException("Target out count exceeds current config stack depth!");

        for (int i = count; i > 0; i--)
        {
            this.out();
        }
    }

    public void root()
    {
        this.out(this.depth);
    }

    public static class StackBuilder
    {
        private final ConfigBuilder builder;

        private StackBuilder(ConfigBuilder builder)
        {
            this.builder = builder;
        }

        public void nest(Consumer<ConfigBuilder> group)
        {
            group.accept(this.builder);
            this.builder.out();
        }

        public void nest(Runnable group)
        {
            group.run();
            this.builder.out();
        }

        public <T> T nest(Function<ConfigBuilder, T> group)
        {
            T result = group.apply(this.builder);
            this.builder.out();
            return result;
        }

        public <T> T nest(Supplier<T> group)
        {
            T result = group.get();
            this.builder.out();
            return result;
        }
    }

    public <T> EntryBuilder<T> bind() { return new EntryBuilder<>(this); }

    public <T> EntryBuilder<T> bind(Class<T> ignored) { return bind(); }

    public EntryBuilder<Boolean> bindBoolean() { return bind(); }

    public IntBuilder bindInt() { return new IntBuilder(this); }

    public LongBuilder bindLong() { return new LongBuilder(this); }

    public EntryBuilder<Float> bindFloat() { return bind(); }

    public DoubleBuilder bindDouble() { return new DoubleBuilder(this); }

    public EntryBuilder<String> bindString() { return bind(); }

    public <T> ListBuilder<T> bindList() { return new ListBuilder<>(this); }

    public <T> ListBuilder<T> bindList(Class<T> ignored) { return bindList(); }

    public ListBuilder<String> bindStringList() { return bindList(); }

    public <T> OptionBuilder<T> bindIn() { return new OptionBuilder<>(this); }

    public <T> OptionBuilder<T> bindIn(Class<T> ignored) { return bindIn(); }

    public <T extends Enum<T>> EnumBuilder<T> bindEnum() { return new EnumBuilder<>(this); }

    public <T extends Enum<T>> EnumBuilder<T> bindEnum(Class<T> ignored) { return bindEnum(); }

    public static class EntryBuilder<T>
    {
        final ConfigBuilder context;
        final StringBuilder comment;
        Predicate<T> validator;
        boolean row;

        private EntryBuilder(ConfigBuilder context)
        {
            this.context = context;
            this.comment = new StringBuilder();
            this.validator = Objects::nonNull;
            this.row = context.rowGlobal;
        }

        public EntryBuilder<T> depict(String depict)
        {
            String content = depict.replace("\n", "\n ");

            if (this.comment.isEmpty()) this.comment.append(" ");
            else this.comment.append("\n ");

            this.comment.append(content);

            return this;
        }

        public EntryBuilder<T> depict(String... depicts)
        {
            for (String depict : depicts) this.depict(depict);
            return this;
        }

        public EntryBuilder<T> lineBreak()
        {
            this.row = true;
            return this;
        }

        public EntryBuilder<T> validator(Predicate<T> verify)
        {
            this.validator = this.validator.and(verify);
            return this;
        }

        public ConfigEntry<T> entry(String key, T defaultValue)
        {
            return this.def(key, defaultValue, suffix(defaultValue));
        }

        ConfigEntry<T> def(String key, T defaultValue, String suffix)
        {
            ConfigEntry<T> entry = new ConfigEntry<>(this.context.path + key, defaultValue, this.validator);
            if (this.context.suffixGlobal) this.comment.append(suffix);
            return def(key, this.comment.toString(), entry);
        }

        <E extends ConfigEntry<?>> E def(String key, String depict, E entry)
        {
            this.context.depictMap.put(this.context.path + key, depict);

            this.context.entries.add(entry);
            return entry;
        }

        private String suffix(Object defaultValue)
        {
            String suffix;
            if (this.row) suffix = (this.comment.isEmpty() ? " " : "\n ") + SuffixUtil.defNewRow(defaultValue);
            else suffix = SuffixUtil.defSuffix(defaultValue);
            return suffix;
        }
    }

    public static abstract class NumberBuilder<N extends Number> extends EntryBuilder<N>
    {
        N min = minValue();
        N max = maxValue();
        private NumberBuilder(ConfigBuilder context) { super(context); }

        public NumberBuilder<N> min(N value)
        {
            this.min = value;
            return this;
        }

        public NumberBuilder<N> max(N value)
        {
            this.max = value;
            return this;
        }

        public NumberBuilder<N> range(N min, N max)
        {
            this.min = min;
            this.max = max;
            return this;
        }

        abstract N minValue();
        abstract N maxValue();
        abstract Predicate<N> verify();

        @Override
        public ConfigEntry<N> entry(String key, N defaultValue)
        {
            String suffix;
            if (this.row)
                suffix = (this.comment.isEmpty() ? " " : "\n ") + defNewRow(defaultValue, this.min, this.max);
            else
                suffix = defSuffix(defaultValue, this.min, this.max);

            return this.validator(verify()).def(key, defaultValue, suffix);
        }

        private String defSuffix(N def, N min, N max)
        {
            return " [" + defNewRow(def, min, max) + "]";
        }

        private String defNewRow(N def, N min, N max)
        {
            if (Objects.equals(min, maxValue()) || Objects.equals(max, minValue()))
                throw new IllegalArgumentException("Min or Max value are incorrect");

            return rangeSuffix(def, min, max, Objects.equals(min, minValue()), Objects.equals(max, maxValue()));
        }

        private String rangeSuffix(N value, N min, N max, boolean lower, boolean upper)
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

        @Override
        public NumberBuilder<N> depict(String depict) { return (NumberBuilder<N>) super.depict(depict); }
        @Override
        public NumberBuilder<N> depict(String... depict) { return (NumberBuilder<N>) super.depict(depict); }
        @Override
        public NumberBuilder<N> lineBreak() { return (NumberBuilder<N>) super.lineBreak(); }
        @Override
        public NumberBuilder<N> validator(Predicate<N> verify) { return (NumberBuilder<N>) super.validator(verify); }
    }

    public static class IntBuilder extends NumberBuilder<Integer>
    {
        private IntBuilder(ConfigBuilder context) { super(context); }

        @Override
        Integer minValue() { return Integer.MIN_VALUE; }

        @Override
        Integer maxValue() { return Integer.MAX_VALUE; }

        @Override
        Predicate<Integer> verify() { return value -> value >= this.min && value <= this.max; }
    }

    public static class LongBuilder extends NumberBuilder<Long>
    {
        private LongBuilder(ConfigBuilder context) { super(context); }

        @Override
        Long minValue() { return Long.MIN_VALUE; }

        @Override
        Long maxValue() { return Long.MAX_VALUE; }

        @Override
        Predicate<Long> verify() { return value -> value >= this.min && value <= this.max; }
    }

    public static class DoubleBuilder extends NumberBuilder<Double>
    {
        private DoubleBuilder(ConfigBuilder context) { super(context); }

        @Override
        Double minValue() { return Double.NEGATIVE_INFINITY; }

        @Override
        Double maxValue() { return Double.POSITIVE_INFINITY; }

        @Override
        Predicate<Double> verify() { return value -> value >= this.min && value <= this.max; }
    }

    public static class ListBuilder<T> extends EntryBuilder<List<T>>
    {
        private boolean empty = false;
        private Predicate<T> entryVerify = Objects::nonNull;
        private ListBuilder(ConfigBuilder context) { super(context); }

        public ListBuilder<T> entryVerify(Predicate<T> validate)
        {
            this.entryVerify = this.entryVerify.and(validate);
            return this;
        }

        public ListBuilder<T> allowEmpty()
        {
            this.empty = true;
            return this;
        }

        @Override
        public ListEntry<T> entry(String key, List<T> defaultValue)
        {
            var entry = new ListEntry<>(
                    this.context.path + key, defaultValue, this.validator, this.entryVerify, this.empty);
            String suffix = this.context.suffixGlobal ? super.suffix(defaultValue) : "";

            return this.def(key, this.comment.append(suffix).toString(), entry);
        }

        @Override
        public ListBuilder<T> depict(String depict) { return (ListBuilder<T>) super.depict(depict); }
        @Override
        public ListBuilder<T> depict(String... depict) { return (ListBuilder<T>) super.depict(depict); }
        @Override
        public ListBuilder<T> lineBreak() { return (ListBuilder<T>) super.lineBreak(); }
        @Override
        public ListBuilder<T> validator(Predicate<List<T>> verify) { return (ListBuilder<T>) super.validator(verify); }
    }

    public static class OptionBuilder<T> extends EntryBuilder<T>
    {
        List<T> options = new ArrayList<>();
        private OptionBuilder(ConfigBuilder context) { super(context); }

        public OptionBuilder<T> addOption(T value)
        {
            this.options.add(value);
            return this;
        }

        public OptionBuilder<T> addOption(T... value)
        {
            return addOption(Arrays.asList(value));
        }

        public OptionBuilder<T> addOption(List<T> value)
        {
            this.options.addAll(value);
            return this;
        }

        @Override
        public ConfigEntry<T> entry(String key, T defaultValue)
        {
            if (!this.options.contains(defaultValue))
            {
                String exception = "Default value " + defaultValue + " is not in the options: " + this.options;
                throw new IllegalArgumentException(exception);
            }

            String depict = combine(defaultValue, this.options);

            var entry = new OptionEntry<>(this.context.path + key, defaultValue, this.validator, this.options);
            return this.def(key, depict, entry);
        }

        private <V> String combine(V defaultValue, List<V> options)
        {
            StringBuilder result;
            String string = options
                    .stream()
                    .map(Objects::toString)
                    .collect(Collectors.joining(", "));
            if (this.comment.isEmpty())
            {
                result = new StringBuilder();
                if (this.context.suffixGlobal) result.append(" ").append(SuffixUtil.defNewRow(defaultValue)).append("\n");
                result.append(" Options: ").append(string);
            }
            else
            {
                result = new StringBuilder(this.comment);
                if (this.context.suffixGlobal)
                {
                    if (this.row)
                        result.append("\n ").append(SuffixUtil.defNewRow(defaultValue));
                    else
                        result.append(SuffixUtil.defSuffix(defaultValue));
                }
                result.append("\n Options: ").append(string);
            }

            return result.toString();
        }

        @Override
        public OptionBuilder<T> depict(String depict) { return (OptionBuilder<T>) super.depict(depict); }
        @Override
        public OptionBuilder<T> depict(String... depict) { return (OptionBuilder<T>) super.depict(depict); }
        @Override
        public OptionBuilder<T> lineBreak() { return (OptionBuilder<T>) super.lineBreak(); }
        @Override
        public OptionBuilder<T> validator(Predicate<T> verify) { return (OptionBuilder<T>) super.validator(verify); }
    }

    public static class EnumBuilder<T extends Enum<T>> extends OptionBuilder<T>
    {
        private EnumBuilder(ConfigBuilder context)
        {
            super(context);
        }

        @Override
        public EnumEntry<T> entry(String key, T defaultValue)
        {
            Class<T> clazz = (Class<T>) defaultValue.getClass();

            String depict = super.combine(defaultValue, Arrays.asList(clazz.getEnumConstants()));

            String path = this.context.path + key;
            var entry = new EnumEntry<>(path, defaultValue, this.validator);

            return this.def(key, depict, entry);
        }

        @Override
        public EnumBuilder<T> depict(String depict) { return (EnumBuilder<T>) super.depict(depict); }
        @Override
        public EnumBuilder<T> depict(String... depict) { return (EnumBuilder<T>) super.depict(depict); }
        @Override
        public EnumBuilder<T> lineBreak() { return (EnumBuilder<T>) super.lineBreak(); }
        @Override
        public EnumBuilder<T> validator(Predicate<T> verify) { return (EnumBuilder<T>) super.validator(verify); }
    }
}
