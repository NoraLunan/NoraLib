package net.noralunan.library.data;

import net.minecraft.nbt.CompoundTag;

import java.lang.reflect.Field;

public abstract class Saved<T>
{
    protected T value;
    protected final String key;
    protected final T default_value;
    protected final boolean lifecycle;
    transient Field cache_field;

    Saved(String key, T defaultValue, boolean lifecycle)
    {
        this.key = key;
        this.default_value = defaultValue;
        this.lifecycle = lifecycle;
        this.value = defaultValue;
    }

    public T get()
    {
        return this.value;
    }

    public T getDefault()
    {
        return this.default_value;
    }

    public void set(T value)
    {
        this.value = value;
    }

    public void reset()
    {
        this.value = this.default_value;
    }

    @SuppressWarnings("unchecked")
    void copy(Saved<?> other)
    {
        this.value = (T) other.value;
    }

    void attachField(Field field)
    {
        this.cache_field = field;
    }

    protected abstract void read(CompoundTag nbt);
    protected abstract void write(CompoundTag nbt);
    protected abstract void copyToBuilder(NoraData.Builder builder);
}

class SavedBoolean extends Saved<Boolean>
{
    protected SavedBoolean(String key, Boolean defaultValue, boolean lifecycle)
    {
        super(key, defaultValue, lifecycle);
    }

    @Override
    protected void read(CompoundTag nbt)
    {
        if (nbt.contains(super.key)) super.value = nbt.getBoolean(super.key);
    }

    @Override
    protected void write(CompoundTag nbt)
    {
        nbt.putBoolean(super.key, super.value);
    }

    @Override
    protected void copyToBuilder(NoraData.Builder builder)
    {
        builder.setLifecycle(this.lifecycle).defBoolean(super.key, super.default_value);
    }
}

class SavedInt extends Saved<Integer>
{
    protected SavedInt(String key, Integer defaultValue, boolean lifecycle)
    {
        super(key, defaultValue, lifecycle);
    }

    @Override
    protected void read(CompoundTag nbt)
    {
        if (nbt.contains(super.key)) super.value = nbt.getInt(super.key);
    }

    @Override
    protected void write(CompoundTag nbt)
    {
        nbt.putInt(super.key, super.value);
    }

    @Override
    protected void copyToBuilder(NoraData.Builder builder)
    {
        builder.setLifecycle(this.lifecycle).defInt(super.key, super.default_value);
    }
}

class SavedLong extends Saved<Long>
{
    protected SavedLong(String key, Long defaultValue, boolean lifecycle)
    {
        super(key, defaultValue, lifecycle);
    }

    @Override
    protected void read(CompoundTag nbt)
    {
        if (nbt.contains(super.key)) super.value = nbt.getLong(super.key);
    }

    @Override
    protected void write(CompoundTag nbt)
    {
        nbt.putLong(super.key, super.value);
    }

    @Override
    protected void copyToBuilder(NoraData.Builder builder)
    {
        builder.setLifecycle(this.lifecycle).defLong(super.key, super.default_value);
    }
}

class SavedFloat extends Saved<Float>
{
    protected SavedFloat(String key, Float defaultValue, boolean lifecycle)
    {
        super(key, defaultValue, lifecycle);
    }

    @Override
    protected void read(CompoundTag nbt)
    {
        if (nbt.contains(super.key)) super.value = nbt.getFloat(super.key);
    }

    @Override
    protected void write(CompoundTag nbt)
    {
        nbt.putFloat(super.key, super.value);
    }

    @Override
    protected void copyToBuilder(NoraData.Builder builder)
    {
        builder.setLifecycle(this.lifecycle).defFloat(super.key, super.default_value);
    }
}

class SavedDouble extends Saved<Double>
{
    protected SavedDouble(String key, Double defaultValue, boolean lifecycle)
    {
        super(key, defaultValue, lifecycle);
    }

    @Override
    protected void read(CompoundTag nbt)
    {
        if (nbt.contains(super.key)) super.value = nbt.getDouble(super.key);
    }

    @Override
    protected void write(CompoundTag nbt)
    {
        nbt.putDouble(super.key, super.value);
    }

    @Override
    protected void copyToBuilder(NoraData.Builder builder)
    {
        builder.setLifecycle(this.lifecycle).defDouble(super.key, super.default_value);
    }
}

class SavedByte extends Saved<Byte>
{
    protected SavedByte(String key, Byte defaultValue, boolean lifecycle)
    {
        super(key, defaultValue, lifecycle);
    }

    @Override
    protected void read(CompoundTag nbt)
    {
        if (nbt.contains(super.key)) super.value = nbt.getByte(super.key);
    }

    @Override
    protected void write(CompoundTag nbt)
    {
        nbt.putByte(super.key, super.value);
    }

    @Override
    protected void copyToBuilder(NoraData.Builder builder)
    {
        builder.setLifecycle(this.lifecycle).defByte(super.key, super.default_value);
    }
}

class SavedShort extends Saved<Short>
{
    protected SavedShort(String key, Short defaultValue, boolean lifecycle)
    {
        super(key, defaultValue, lifecycle);
    }

    @Override
    protected void read(CompoundTag nbt)
    {
        if (nbt.contains(super.key)) super.value = nbt.getShort(super.key);
    }

    @Override
    protected void write(CompoundTag nbt)
    {
        nbt.putShort(super.key, super.value);
    }

    @Override
    protected void copyToBuilder(NoraData.Builder builder)
    {
        builder.setLifecycle(this.lifecycle).defShort(super.key, super.default_value);
    }
}
