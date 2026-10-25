package net.noralunan.library.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.noralunan.library.common.NoraHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class ConfigEntry<T>
{
    private static final BiConsumer<String, Object> FIX_LOGGER =
            (key, value) -> SimpleConfig.LOGGER.warn("Incorrect key {}, has been set to {}", key, value);

    final String key;
    volatile T value;
    final T defaultValue;
    final Predicate<T> validator;
    SimpleConfig standard;
    boolean change;

    ConfigEntry(String key, T defaultValue, Predicate<T> validator)
    {
        this.key = key;
        this.value = defaultValue;
        this.defaultValue = defaultValue;
        this.validator = validator;
        this.change = false;
    }

    public T get()
    {
        return this.value;
    }

    public synchronized void set(T value)
    {
        if (this.validator.test(value))
        {
            setValue(value);
        }
    }

    protected synchronized void setValue(T value)
    {
        this.value = value;
        this.change = true;
    }

    protected void reset()
    {
        SimpleConfig.CORRECT.accept(this.standard);
        FIX_LOGGER.accept(this.key, this.defaultValue);
        this.standard.config.set(this.key, this.defaultValue);
        this.value = this.defaultValue;
    }

    void maintain()
    {
        Optional<T> optional = this.standard.config.getOptional(this.key);
        if (optional.isPresent())
        {
            T value = optional.get();
            if (this.validator.test(value))
            {
                this.value = value;
                return;
            }
        }
        this.reset();
    }

    public static class ListEntry<T> extends ConfigEntry<List<T>>
    {
        private final Predicate<T> entryVerify;
        private final boolean allowEmpty;

        ListEntry(String key, List<T> defaultValue, Predicate<List<T>> validator, Predicate<T> entryVerify, boolean allowEmpty)
        {
            super(key, defaultValue, validator);
            this.entryVerify = entryVerify;
            this.allowEmpty = allowEmpty;
        }

        @Override
        void maintain()
        {
            CommentedFileConfig config = this.standard.config;
            Optional<List<T>> optional = config.getOptional(this.key);

            if (optional.isPresent())
            {
                List<T> value = new ArrayList<>(optional.get());

                if (value.isEmpty() && !this.allowEmpty)
                {
                    this.reset();
                    return;
                }

                if (!this.validator.test(value))
                {
                    this.reset();
                    return;
                }

                List<T> illegal = value.stream()
                        .filter(t -> !this.entryVerify.test(t))
                        .toList();
                if (!illegal.isEmpty())
                {
                    illegal.forEach(value::remove);
                    config.set(this.key, value);
                }

                this.value = value;
            }
            else
            {
                this.reset();
            }
        }
    }

    public static class EnumEntry<T extends Enum<T>> extends ConfigEntry<T>
    {
        private final Class<T> clazz;

        @SuppressWarnings("unchecked")
        EnumEntry(String key, T defaultValue, Predicate<T> validator)
        {
            super(key, defaultValue, validator);
            this.clazz = (Class<T>) defaultValue.getClass();
        }

        @Override
        void maintain()
        {
            try {
                Optional<String> optional = this.standard.config.getOptional(this.key);
                if (optional.isPresent())
                {
                    String value = optional.get();
                    this.value = NoraHelper.getEnum(this.clazz, value);
                }
                else
                {
                    this.reset();
                }
            }
            catch (IllegalArgumentException | ClassCastException exception) {
                this.reset();
            }
        }

        @Override
        protected void reset()
        {
            SimpleConfig.CORRECT.accept(this.standard);
            FIX_LOGGER.accept(this.key, this.defaultValue);
            this.standard.config.set(this.key, String.valueOf(this.defaultValue));
            this.value = this.defaultValue;
        }
    }

    public static class OptionEntry<T> extends ConfigEntry<T>
    {
        private final List<T> allowed;
        OptionEntry(String key, T defaultValue, Predicate<T> validator, List<T> allowed)
        {
            super(key, defaultValue, validator);
            this.allowed = allowed;
        }

        @Override
        void maintain()
        {
            Optional<T> optional = this.standard.config.get(this.key);
            if (optional.isPresent())
            {
                T value = optional.get();
                if (!this.validator.test(value) || !this.allowed.contains(value))
                {
                    this.reset();
                    return;
                }

                this.value = value;
            }
            else this.reset();
        }
    }
}
