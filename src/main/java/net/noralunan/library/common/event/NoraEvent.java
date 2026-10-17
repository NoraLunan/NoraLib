package net.noralunan.library.common.event;

import net.minecraft.client.Minecraft;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.noralunan.library.common.NoraLevel;
import net.noralunan.library.common.NoraMath;
import net.noralunan.library.common.NoraWorld;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class NoraEvent
{
    @FunctionalInterface
    public interface ServerTickHandler extends Consumer<NoraTickEvent.ServerTick>
    {
        @Override
        void accept(NoraTickEvent.ServerTick event);
    }

    @FunctionalInterface
    public interface ClientTickHandler extends Consumer<NoraTickEvent.ClientTick>
    {
        @Override
        void accept(NoraTickEvent.ClientTick event);
    }

    @FunctionalInterface
    public interface LevelTickHandler extends Consumer<NoraTickEvent.LevelTick>
    {
        @Override
        void accept(NoraTickEvent.LevelTick event);
    }

    private record ScheduledTask<T extends NoraTickEvent>(Consumer<T> handler, int interval) {}

    private static List<ScheduledTask<NoraTickEvent.ServerTick>> server_tasks = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean SERVER_FREEZE = new AtomicBoolean(false);
    private static List<ScheduledTask<NoraTickEvent.ClientTick>> client_tasks = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean CLIENT_FREEZE = new AtomicBoolean(false);
    private static List<ScheduledTask<NoraTickEvent.LevelTick>> server_level_tasks = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean SERVER_LEVEL_FREEZE = new AtomicBoolean(false);
    private static List<ScheduledTask<NoraTickEvent.LevelTick>> client_level_tasks = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean CLIENT_LEVEL_FREEZE = new AtomicBoolean(false);

    public static void register(Class<?> clazz)
    {
        Arrays.stream(clazz.getDeclaredMethods())
                .filter(method -> Modifier.isPublic(method.getModifiers()))
                .filter(method -> Modifier.isStatic(method.getModifiers()))
                .filter(method -> method.isAnnotationPresent(PeriodicTickEvent.class))
                .forEach(NoraEvent::addListener);
    }

    private static void addListener(Method method)
    {
        PeriodicTickEvent annotation = method.getAnnotation(PeriodicTickEvent.class);
        int interval = annotation.interval();
        verify(interval);

        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != 1 || !NoraTickEvent.class.isAssignableFrom(paramTypes[0]))
        {
            throw new IllegalArgumentException("Method must have one NoraTickEvent parameter");
        }

        method.setAccessible(true);

        try
        {
            if (paramTypes[0].equals(NoraTickEvent.ServerTick.class))
            {
                ServerTickHandler handler = st -> {
                    try
                    {
                        method.invoke(null, st);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                };
                server_tasks.add(new ScheduledTask<>(handler, interval));
            }
            else if (paramTypes[0].equals(NoraTickEvent.ClientTick.class))
            {
                ClientTickHandler handler =ct ->
                {
                    try
                    {
                        method.invoke(null, ct);
                    }
                    catch (Exception e)
                    {
                        throw new RuntimeException(e);
                    }
                };
                client_tasks.add(new ScheduledTask<>(handler, interval));
            }
            else if (paramTypes[0].equals(NoraTickEvent.LevelTick.class))
            {
                LevelTickHandler handler =lt ->
                {
                    try {
                        method.invoke(null, lt);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };
                server_level_tasks.add(new ScheduledTask<>(handler, interval));
                client_level_tasks.add(new ScheduledTask<>(handler, interval));
            }
            else
            {
                throw new IllegalArgumentException("Unsupported event type: " + paramTypes[0].getName());
            }
        }
        catch (Exception exception)
        {
            throw new RuntimeException("Failed to register method: " + method.getName(), exception);
        }
    }

    private static void verify(int interval)
    {
        if (interval < 1)
        {
            throw new IllegalArgumentException("Interval must be >= 1");
        }
    }

    public static void addServer(int interval, Consumer<NoraTickEvent.ServerTick> handler)
    {
        verify(interval);
        server_tasks.add(new ScheduledTask<>(handler, interval));
    }

    public static void addClient(int interval, Consumer<NoraTickEvent.ClientTick> handler)
    {
        verify(interval);
        client_tasks.add(new ScheduledTask<>(handler, interval));
    }

    public static void addLevel(int interval, Consumer<NoraTickEvent.LevelTick> handler)
    {
        verify(interval);
        server_level_tasks.add(new ScheduledTask<>(handler, interval));
        client_level_tasks.add(new ScheduledTask<>(handler, interval));
    }

    public static void addServer(Consumer<NoraTickEvent.ServerTick> handler)
    {
        addServer(1, handler);
    }

    public static void addClient(Consumer<NoraTickEvent.ClientTick> handler)
    {
        addClient(1, handler);
    }

    public static void addLevel(Consumer<NoraTickEvent.LevelTick> handler)
    {
        addLevel(1, handler);
    }

    @SubscribeEvent
    public static void server(TickEvent.ServerTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            if (!SERVER_FREEZE.get())
            {
                SERVER_FREEZE.set(true);
                server_tasks = List.copyOf(server_tasks);
            }
            return;
        }

        if (server_tasks.isEmpty()) return;

        MinecraftServer ms = event.getServer();
        NoraLevel nl = (NoraLevel) ms.overworld();
        long playTime = nl.playTime();

        NoraTickEvent.ServerTick st = new NoraTickEvent.ServerTick(ms);

        for (var task : server_tasks)
        {
            if (NoraMath.divisible(playTime, task.interval))
                task.handler.accept(st);
        }
    }

    @SubscribeEvent
    public static void client(TickEvent.ClientTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            if (!CLIENT_FREEZE.get())
            {
                CLIENT_FREEZE.set(true);
                client_tasks = List.copyOf(client_tasks);
            }
            return;
        }

        if (client_tasks.isEmpty()) return;

        NoraTickEvent.ClientTick ct = new NoraTickEvent.ClientTick();

        NoraWorld world = (NoraWorld) Minecraft.getInstance().level;
        if (world == null) return;

        long playTime = world.playTime();

        for (var task : client_tasks)
        {
            if (NoraMath.divisible(playTime, task.interval))
                task.handler.accept(ct);
        }
    }

    @SubscribeEvent
    public static void level(TickEvent.LevelTickEvent event)
    {
        if (event.phase == TickEvent.Phase.START)
        {
            if (event.level.isClientSide())
            {
                if (!CLIENT_LEVEL_FREEZE.get())
                {
                    CLIENT_LEVEL_FREEZE.set(true);
                    client_level_tasks = List.copyOf(client_level_tasks);
                }
            }
            else
            {
                if (!SERVER_LEVEL_FREEZE.get())
                {
                    SERVER_LEVEL_FREEZE.set(true);
                    server_level_tasks = List.copyOf(server_level_tasks);
                }
            }

            return;
        }

        NoraWorld world = (NoraWorld) event.level;
        long playTime = world.playTime();
        NoraTickEvent.LevelTick lt = new NoraTickEvent.LevelTick(world);


        if (world.isClientSide())
        {
            if (client_level_tasks.isEmpty()) return;
            for (var task : client_level_tasks)
            {
                if (NoraMath.divisible(playTime, task.interval))
                    task.handler.accept(lt);
            }
        }
        else
        {
            if (server_level_tasks.isEmpty()) return;
            for (var task : server_level_tasks)
            {
                if (NoraMath.divisible(playTime, task.interval))
                    task.handler.accept(lt);
            }
        }
    }
}
