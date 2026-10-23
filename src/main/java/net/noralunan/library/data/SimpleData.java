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
import net.noralunan.library.data.FieldHandler.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@SuppressWarnings("unused")
public class SimpleData<T extends ICapability<T>>
{
    static final NoraLogger LOGGER = NoraLib.logger();

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

    public static void register(SimpleData<?> holder)
    {
        if (holder.registered) return;
        holder.registered = true;
        MinecraftForge.EVENT_BUS.addGenericListener(Entity.class, holder::attach);
        MinecraftForge.EVENT_BUS.addListener(holder::copyData);
    }

    private final Capability<T> capability;
    private final Set<FieldHandler<?>> templates;
    private final Supplier<T> factory;
    private final ResourceLocation id;
    private boolean registered = false;

    private SimpleData(Set<FieldHandler<?>> templates, Supplier<T> factory, T object, ResourceLocation id)
    {
        this.capability = object.capability();
        this.templates = Set.copyOf(templates);
        this.factory = factory;
        this.id = id;
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

        var removal = old.getRemovalReason();
        old.revive();
        T oldData = this.of(old);
        T nowData = this.of(now);

        this.templates.forEach(handler -> {
            if (handler.lifecycle && event.isWasDeath())
                handler.reset(nowData);
            else
                handler.copy(oldData, nowData);
        });

        if (event.isWasDeath())
            execute(now, nowData::onRespawn);
        else
            execute(now, nowData::onCopy);

        execute(now, nowData::onClone);

        old.setRemoved(removal == null ? Entity.RemovalReason.KILLED : removal);
    }

    private void attach(AttachCapabilitiesEvent<Entity> event)
    {
        if (event.getObject() instanceof Player player && !(player instanceof FakePlayer))
        {
            T data = this.factory.get();
            Provider<T> container = new Provider<>(data, this.templates, this.capability);
            event.addCapability(this.id, container);

            execute(player, data::onAttach);
        }
    }

    private void execute(Player player, Consumer<Player> consumer)
    {
        if (consumer != null)
        {
            consumer.accept(player);
        }
    }

    public static class Context
    {
        private final ResourceLocation id;
        private Context(ResourceLocation id)
        {
            this.id = id;
        }

        public <T extends ICapability<T>> SimpleData<T> create(Supplier<T> function)
        {
            T object = function.get();

            Set<FieldHandler<?>> handlers = new HashSet<>();
            Arrays.stream(object.getClass().getDeclaredFields())
                    .filter(FieldHandler::isSupportedType)
                    .filter(field -> field.isAnnotationPresent(Staff.class))
                    .forEach(field -> {
                        String annoValue = field.getAnnotation(Staff.class).key();
                        String key = annoValue.isBlank() ? field.getName() : annoValue;
                        boolean lifecycle = field.isAnnotationPresent(DeathReset.class)
                                && field.getAnnotation(DeathReset.class).value();

                        Object defaultValue;
                        try
                        {
                            field.setAccessible(true);
                            defaultValue = field.get(object);
                        }
                        catch (IllegalAccessException exception)
                        {
                            LOGGER.error("Could not read default value for field: {}", field.getName());
                            throw new RuntimeException("Failed to sync data: " + exception);
                        }

                        Class<?> clazz = field.getType();
                        FieldHandler<?> result;
                        if (clazz == byte.class || clazz == Byte.class)
                            result = new ByteField(field, key, lifecycle, (byte) defaultValue);
                        else if (clazz == int.class || clazz == Integer.class)
                            result = new IntField(field, key, lifecycle, (int) defaultValue);
                        else if (clazz == short.class || clazz == Short.class)
                            result = new ShortField(field, key, lifecycle, (short) defaultValue);
                        else if (clazz == long.class || clazz == Long.class)
                            result = new LongField(field, key, lifecycle, (long) defaultValue);
                        else if (clazz == float.class || clazz == Float.class)
                            result = new FloatField(field, key, lifecycle, (float) defaultValue);
                        else if (clazz == double.class || clazz == Double.class)
                            result = new DoubleField(field, key, lifecycle, (double) defaultValue);
                        else if (clazz == boolean.class || clazz == Boolean.class)
                            result = new BooleanField(field, key, lifecycle, (boolean) defaultValue);
                        else if (clazz == String.class)
                            result = new StringField(field, key, lifecycle, (String) defaultValue);
                        else if (clazz == UUID.class)
                            result = new UUIDField(field, key, lifecycle, (UUID) defaultValue);
                        else if (clazz == Tag.class)
                            result = new TagField(field, key, lifecycle, (Tag) defaultValue);
                        else
                            throw new RuntimeException(
                                    "Failed to read default value for field "
                                            + field.getName()
                                            + " in class "
                                            + clazz.getName());

                        handlers.add(result);
                    });

            return new SimpleData<>(handlers, function, object, this.id);
        }
    }

    public static class Provider<T extends ICapability<T>> implements ICapabilityProvider, INBTSerializable<Tag>
    {
        private final T data;
        private final Set<FieldHandler<?>> handlers;
        private final LazyOptional<T> lazy;
        private final Capability<T> capability;

        private Provider(T data, Set<FieldHandler<?>> handlers, Capability<T> capability)
        {
            this.data = data;
            this.handlers = handlers;
            this.lazy = LazyOptional.of(() -> data);
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
            this.handlers.forEach(handler -> {
                try
                {
                    handler.write(nbt, this.data);
                }
                catch (Exception exception)
                {
                    LOGGER.error("Failed to serialize NBT: {}", exception);
                }
            });

            return nbt;
        }

        @Override
        public void deserializeNBT(Tag tag)
        {
            CompoundTag nbt = (CompoundTag) tag;
            this.handlers.forEach(handler -> {
                try
                {
                    handler.read(nbt, this.data);
                }
                catch (Exception exception)
                {
                    LOGGER.error("Failed to deserialize NBT: {}", exception);
                }
            });
        }
    }
}
