package net.noralunan.library.data;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.noralunan.library.NoraLib;
import net.noralunan.library.common.NoraLogger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Function;

@SuppressWarnings("unused")
public class NoraData<T extends ICapability<T>>
{
    private static final NoraLogger LOGGER = NoraLib.logger();

    public static Context from(String name)
    {
        return from(new ResourceLocation(name));
    }

    public static Context from(String namespace, String name)
    {
        return from(new ResourceLocation(namespace, name));
    }

    public static Context from(ResourceLocation id)
    {
        return new Context(id);
    }

    public static void register(NoraData<?> holder)
    {
        if (holder.registered) return;
        holder.registered = true;
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, holder::attach);
        MinecraftForge.EVENT_BUS.addListener(holder::copyData);
    }

    private final Capability<T> capability;
    private final Set<Saved<?>> templates;
    private final Function<Builder, T> factory;
    private final ResourceLocation id;
    private boolean registered = false;

    private NoraData(Set<Saved<?>> templates, Function<Builder, T> factory, T object, ResourceLocation id)
    {
        this.capability = object.capability();
        this.templates = templates;
        this.factory = factory;
        this.id = id;

        Class<?> clazz = object.getClass();

        Map<String, Saved<?>> map = new HashMap<>();
        templates.forEach(saved -> map.put(saved.key, saved));

        Arrays.stream(clazz.getDeclaredFields())
                .filter(field -> Saved.class.isAssignableFrom(field.getType()))
                .forEach(field -> {
                    String key;
                    if (field.isAnnotationPresent(NbtKey.class))
                        key = field.getAnnotation(NbtKey.class).value();
                    else
                        key = field.getName();

                    Saved<?> saved = map.get(key);
                    if (saved == null)
                    {
                        throw new NullPointerException("Saved key "
                                + key
                                + " has no matched field in class "
                                + clazz.getName()
                        );
                    }
                    else
                    {
                        field.setAccessible(true);
                        saved.attachField(field);
                        map.remove(key);
                    }
                });

        if (!map.isEmpty())
        {
            throw new IllegalStateException("Unmatched templates found for keys: " + map.keySet());
        }
    }

    public T of(Player player)
    {
        return player.getCapability(this.capability)
                .orElseThrow(() -> new IllegalStateException(
                        "NoraData capability not attached for player "
                        + player.getName().getString()
                        + ", ID: "
                        + this.id.toString()
                ));
    }

    private void copyData(PlayerEvent.Clone event)
    {
        Player old = event.getOriginal();
        Player now = event.getEntity();
        if (now.level().isClientSide || now instanceof FakePlayer) return;

        old.getCapability(this.capability).ifPresent(oldData ->
                now.getCapability(this.capability).ifPresent(nowData ->
                        this.templates.forEach(saved -> {
                            try
                            {
                                var field = saved.cache_field;
                                if (field != null)
                                {
                                    Saved<?> o = (Saved<?>) field.get(oldData);
                                    Saved<?> n = (Saved<?>) field.get(nowData);
                                    if (saved.lifecycle && event.isWasDeath())
                                        n.reset();
                                    else
                                        n.copy(o);
                                }
                            }
                            catch (IllegalAccessException exception)
                            {
                                LOGGER.error("Failed to sync data: Cannot access field {} for player {}",
                                        saved.key,
                                        old.getName().getString(),
                                        exception
                                );
                            }
                        })
                )
        );
    }

    private void attach(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player && !(event.getObject() instanceof FakePlayer))
        {
            Builder builder = new Builder();
            this.templates.forEach(template -> template.copyToBuilder(builder));
            T data = factory.apply(builder);
            Provider<T> container = new Provider<>(builder.savers, LazyOptional.of(() -> data), this.capability);
            event.addCapability(this.id, container);
            event.addListener(container.lazy::invalidate);
        }
    }

    public static class Context
    {
        private final ResourceLocation id;
        private Context(ResourceLocation id)
        {
            this.id = id;
        }

        public <T extends ICapability<T>> NoraData<T> create(Function<Builder, T> function)
        {
            Builder builder = new Builder();
            T object = function.apply(builder);

            return new NoraData<>(builder.savers, function, object, this.id);
        }
    }

    public static class Provider<T extends ICapability<T>> implements ICapabilityProvider, INBTSerializable<Tag>
    {
        private final Set<Saved<?>> savers;
        private final LazyOptional<T> lazy;
        private final Capability<T> capability;

        private Provider(Set<Saved<?>> savers, LazyOptional<T> lazy, Capability<T> capability)
        {
            this.savers = savers;
            this.lazy = lazy;
            this.capability = capability;
        }

        @Override
        public @NotNull <I> LazyOptional<I> getCapability(@NotNull Capability<I> cap, @Nullable Direction side)
        {
            return cap == this.capability ? this.lazy.cast() : LazyOptional.empty();
        }

        @Override
        public Tag serializeNBT()
        {
            CompoundTag nbt = new CompoundTag();
            this.savers.forEach(saved -> saved.write(nbt));
            return nbt;
        }

        @Override
        public void deserializeNBT(Tag tag)
        {
            CompoundTag nbt = (CompoundTag) tag;
            this.savers.forEach(saved -> saved.read(nbt));
        }
    }

    public static class Builder
    {
        private final Set<Saved<?>> savers;
        private boolean lifecycle;
        private Builder()
        {
            this.savers = new HashSet<>();
            this.lifecycle = false;
        }

        public Builder lifecycle()
        {
            this.lifecycle = true;
            return this;
        }

        public Saved<Boolean> defBoolean(String name, boolean defaultValue)
        {
            Saved<Boolean> result = new SavedBoolean(name, defaultValue, this.lifecycle);
            reset();
            this.savers.add(result);
            return result;
        }

        public Saved<Boolean> defBoolean(String name)
        {
            return defBoolean(name, false);
        }

        public Saved<Integer> defInt(String name, int defaultValue)
        {
            Saved<Integer> result = new SavedInt(name, defaultValue, this.lifecycle);
            reset();
            this.savers.add(result);
            return result;
        }

        public Saved<Integer> defInt(String name)
        {
            return defInt(name, 0);
        }

        public Saved<Long> defLong(String name, long defaultValue)
        {
            Saved<Long> result = new SavedLong(name, defaultValue, this.lifecycle);
            reset();
            this.savers.add(result);
            return result;
        }

        public Saved<Long> defLong(String name)
        {
            return defLong(name, 0L);
        }

        public Saved<Short> defShort(String name, short defaultValue)
        {
            Saved<Short> result = new SavedShort(name, defaultValue, this.lifecycle);
            reset();
            this.savers.add(result);
            return result;
        }

        public Saved<Short> defShort(String name)
        {
            return defShort(name, (short) 0);
        }

        public Saved<Byte> defByte(String name, byte defaultValue)
        {
            Saved<Byte> result = new SavedByte(name, defaultValue, this.lifecycle);
            reset();
            this.savers.add(result);
            return result;
        }

        public Saved<Byte> defByte(String name)
        {
            return defByte(name, (byte) 0);
        }

        public Saved<Float> defFloat(String name, float defaultValue)
        {
            Saved<Float> result = new SavedFloat(name, defaultValue, this.lifecycle);
            reset();
            this.savers.add(result);
            return result;
        }

        public Saved<Float> defFloat(String name)
        {
            return defFloat(name, 0f);
        }

        public Saved<Double> defDouble(String name, double defaultValue)
        {
            Saved<Double> result = new SavedDouble(name, defaultValue, this.lifecycle);
            reset();
            this.savers.add(result);
            return result;
        }

        public Saved<Double> defDouble(String name)
        {
            return defDouble(name, 0d);
        }

        private void reset()
        {
            setLifecycle(false);
        }

        Builder setLifecycle(boolean value)
        {
            this.lifecycle = value;
            return this;
        }
    }
}
