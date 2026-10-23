package net.noralunan.library.data;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class FieldHandler<T>
{
    private static final Set<Class<?>> SUPPORTED;
    static
    {
        Set<Class<?>> supported = new HashSet<>()
        {{
            add(byte.class);
            add(Byte.class);
            add(int.class);
            add(Integer.class);
            add(short.class);
            add(Short.class);
            add(long.class);
            add(Long.class);
            add(float.class);
            add(Float.class);
            add(double.class);
            add(Double.class);
            add(boolean.class);
            add(Boolean.class);
            add(String.class);
            add(UUID.class);
            add(Tag.class);
        }};
        SUPPORTED = Set.copyOf(supported);
    }

    public static boolean isSupportedType(Class<?> clazz)
    {
        return SUPPORTED.contains(clazz);
    }

    public static boolean isSupportedType(Field field)
    {
        return isSupportedType(field.getType());
    }

    final transient Field field;
    final String key;
    final boolean lifecycle;
    final T value;
    FieldHandler(Field field, String key, boolean lifecycle, T defaultValue)
    {
        this.field = field;
        this.key = key;
        this.lifecycle = lifecycle;
        this.value = defaultValue;
    }

    <C extends ICapability<C>> void reset(C instance)
    {
        try
        {
            this.field.set(instance, this.value);
        }
        catch (IllegalAccessException exception)
        {
            SimpleData.LOGGER.error("Failed to reset field {}: {}", exception);
        }
    }

    <C extends ICapability<C>> void copy(C from, C to)
    {
        try
        {
            this.field.set(to, this.field.get(from));
        }
        catch (IllegalAccessException exception)
        {
            SimpleData.LOGGER.error("Failed to copy {} field: {}", this.field.getName(), exception);
        }
    }

    @SuppressWarnings("unchecked")
    <C extends ICapability<C>> T getValue(C instance) throws IllegalAccessException, ClassCastException
    {
        return (T) this.field.get(instance);
    }

    abstract <C extends ICapability<C>> void read(CompoundTag nbt, C instance) throws IllegalAccessException;
    abstract <C extends ICapability<C>> void write(CompoundTag nbt, C instance) throws IllegalAccessException;

    static class ByteField extends FieldHandler<Byte>
    {
        ByteField(Field field, String key, boolean lifecycle, byte defaultValue)
        {
            super(field, key, lifecycle, defaultValue);
        }

        @Override
        <C extends ICapability<C>> void read(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            if (nbt.contains(this.key)) this.field.set(instance, nbt.getByte(this.key));
        }

        @Override
        <C extends ICapability<C>> void write(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            nbt.putByte(this.key, getValue(instance));
        }
    }

    static class IntField extends FieldHandler<Integer>
    {
        IntField(Field field, String key, boolean lifecycle, int defaultValue)
        {
            super(field, key, lifecycle, defaultValue);
        }

        @Override
        <C extends ICapability<C>> void read(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            if (nbt.contains(this.key)) this.field.set(instance, nbt.getInt(this.key));
        }

        @Override
        <C extends ICapability<C>> void write(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            nbt.putInt(this.key, getValue(instance));
        }
    }

    static class ShortField extends FieldHandler<Short>
    {
        ShortField(Field field, String key, boolean lifecycle, short defaultValue)
        {
            super(field, key, lifecycle, defaultValue);
        }

        @Override
        <C extends ICapability<C>> void read(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            if (nbt.contains(this.key)) this.field.set(instance, nbt.getShort(this.key));
        }

        @Override
        <C extends ICapability<C>> void write(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            nbt.putShort(this.key, getValue(instance));
        }
    }

    static class LongField extends FieldHandler<Long>
    {
        LongField(Field field, String key, boolean lifecycle, long defaultValue)
        {
            super(field, key, lifecycle, defaultValue);
        }

        @Override
        <C extends ICapability<C>> void read(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            if (nbt.contains(this.key)) this.field.set(instance, nbt.getLong(this.key));
        }

        @Override
        <C extends ICapability<C>> void write(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            nbt.putLong(this.key, getValue(instance));
        }
    }

    static class FloatField extends FieldHandler<Float>
    {
        FloatField(Field field, String key, boolean lifecycle, float defaultValue)
        {
            super(field, key, lifecycle, defaultValue);
        }

        @Override
        <C extends ICapability<C>> void read(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            if (nbt.contains(this.key)) this.field.set(instance, nbt.getFloat(this.key));
        }

        @Override
        <C extends ICapability<C>> void write(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            nbt.putFloat(this.key, getValue(instance));
        }
    }

    static class DoubleField extends FieldHandler<Double>
    {
        DoubleField(Field field, String key, boolean lifecycle, double defaultValue)
        {
            super(field, key, lifecycle, defaultValue);
        }

        @Override
        <C extends ICapability<C>> void read(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            if (nbt.contains(this.key)) this.field.set(instance, nbt.getDouble(this.key));
        }

        @Override
        <C extends ICapability<C>> void write(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            nbt.putDouble(this.key, getValue(instance));
        }
    }

    static class BooleanField extends FieldHandler<Boolean>
    {
        BooleanField(Field field, String key, boolean lifecycle, boolean defaultValue)
        {
            super(field, key, lifecycle, defaultValue);
        }

        @Override
        <C extends ICapability<C>> void read(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            if (nbt.contains(this.key)) this.field.set(instance, nbt.getBoolean(this.key));
        }

        @Override
        <C extends ICapability<C>> void write(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            nbt.putBoolean(this.key, getValue(instance));
        }
    }

    static class StringField extends FieldHandler<String>
    {
        StringField(Field field, String key, boolean lifecycle, String defaultValue)
        {
            super(field, key, lifecycle, defaultValue);
        }

        @Override
        <C extends ICapability<C>> void read(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            if (nbt.contains(this.key)) this.field.set(instance, nbt.getString(this.key));
        }

        @Override
        <C extends ICapability<C>> void write(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            nbt.putString(this.key, getValue(instance));
        }
    }

    static class UUIDField extends FieldHandler<UUID>
    {
        UUIDField(Field field, String key, boolean lifecycle, UUID defaultValue)
        {
            super(field, key, lifecycle, defaultValue);
        }

        @Override
        <C extends ICapability<C>> void read(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            if (nbt.contains(this.key)) this.field.set(instance, nbt.getUUID(this.key));
        }

        @Override
        <C extends ICapability<C>> void write(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            nbt.putUUID(this.key, getValue(instance));
        }
    }

    static class TagField extends FieldHandler<Tag>
    {
        TagField(Field field, String key, boolean lifecycle, Tag defaultValue)
        {
            super(field, key, lifecycle, defaultValue);
        }

        @Override
        <C extends ICapability<C>> void read(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            if (nbt.contains(this.key)) this.field.set(instance, nbt.get(this.key));
        }

        @Override
        <C extends ICapability<C>> void write(CompoundTag nbt, C instance) throws IllegalAccessException
        {
            nbt.put(this.key, getValue(instance));
        }
    }
}
