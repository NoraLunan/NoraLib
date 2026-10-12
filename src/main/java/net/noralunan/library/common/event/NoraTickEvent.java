package net.noralunan.library.common.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.LevelAccessor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.noralunan.library.common.NoraMath;
import net.noralunan.library.common.NoraWorld;

public class NoraTickEvent extends Event {
    public static class ServerTick extends NoraTickEvent {
        private final MinecraftServer server;
        private ServerTick(MinecraftServer server) {
            this.server = server;
        }

        public MinecraftServer getServer() {
            return server;
        }
    }

    public static class ClientTick extends NoraTickEvent {
        private ClientTick(Minecraft placeholder) {}
    }

    public static class LevelTick extends NoraTickEvent {
        private final NoraWorld world;
        private LevelTick(NoraWorld world) {
            this.world = world;
        }

        public boolean isClientSide() {
            return world.isClientSide();
        }

        public NoraWorld getWorld() {
            return world;
        }
    }

    public static class ServerSecond extends NoraTickEvent {
        private final MinecraftServer server;
        private ServerSecond(MinecraftServer server) {
            this.server = server;
        }

        public MinecraftServer getServer() {
            return server;
        }
    }

    public static class ClientSecond extends NoraTickEvent {
        private ClientSecond(Minecraft placeholder) {}
    }

    public static class LevelSecond extends NoraTickEvent {
        private final NoraWorld world;
        private LevelSecond(NoraWorld world) {
            this.world = world;
        }

        public boolean isClientSide() {
            return world.isClientSide();
        }

        public NoraWorld getWorld() {
            return world;
        }
    }

    public static class ServerMinute extends NoraTickEvent {
        private final MinecraftServer server;
        private ServerMinute(MinecraftServer server) {
            this.server = server;
        }

        public MinecraftServer getServer() {
            return server;
        }
    }

    public static class ClientMinute extends NoraTickEvent {
        private ClientMinute(Minecraft placeholder) {}
    }

    public static class LevelMinute extends NoraTickEvent {
        private final NoraWorld world;
        private LevelMinute(NoraWorld world) {
            this.world = world;
        }

        public boolean isClientSide() {
            return world.isClientSide();
        }

        public NoraWorld getWorld() {
            return world;
        }
    }

    public static class ServerFive extends NoraTickEvent {
        private final MinecraftServer server;
        private ServerFive(MinecraftServer server) {
            this.server = server;
        }

        public MinecraftServer getServer() {
            return server;
        }
    }

    public static class ClientFive extends NoraTickEvent {
        private ClientFive(Minecraft placeholder) {}
    }

    public static class LevelFive extends NoraTickEvent {
        private final NoraWorld world;
        private LevelFive(NoraWorld world) {
            this.world = world;
        }

        public boolean isClientSide() {
            return world.isClientSide();
        }

        public NoraWorld getWorld() {
            return world;
        }
    }

    public static class Post {
        @SubscribeEvent
        public static void server(TickEvent.ServerTickEvent event) {
            if (event.phase == TickEvent.Phase.START) return;
            MinecraftServer server = event.getServer();
            Server.tick(server);
            Server.second(server);
            Server.five(server);
            Server.minute(server);
        }

        @SubscribeEvent
        public static void client(TickEvent.ClientTickEvent event) {
            if (event.phase == TickEvent.Phase.START) return;
            Client.tick();
            Client.second();
            Client.five();
            Client.minute();
        }

        @SubscribeEvent
        public static void level(TickEvent.LevelTickEvent event) {
            if (event.phase == TickEvent.Phase.START) return;
            NoraWorld world = (NoraWorld) event.level;
            Level.tick(world);
            Level.second(world);
            Level.five(world);
            Level.minute(world);
        }

        private static class Server {
            private static void tick(MinecraftServer server) {
                MinecraftForge.EVENT_BUS.post(new ServerTick(server));
            }

            private static void second(MinecraftServer server) {
                if (divisible(server.overworld(), 20))
                    MinecraftForge.EVENT_BUS.post(new ServerSecond(server));
            }

            private static void five(MinecraftServer server) {
                if (divisible(server.overworld(), 100))
                    MinecraftForge.EVENT_BUS.post(new ServerFive(server));
            }

            private static void minute(MinecraftServer server) {
                if (divisible(server.overworld(), 1200))
                    MinecraftForge.EVENT_BUS.post(new ServerMinute(server));
            }
        }

        private static class Client {
            private static void tick() {
                MinecraftForge.EVENT_BUS.post(new ClientTick(null));
            }

            private static void second() {
                if (check(20)) MinecraftForge.EVENT_BUS.post(new ClientSecond(null));
            }

            private static void five() {
                if (check(100)) MinecraftForge.EVENT_BUS.post(new ClientFive(null));
            }

            private static void minute() {
                if (check(1200)) MinecraftForge.EVENT_BUS.post(new ClientMinute(null));
            }

            private static boolean check(int target) {
                ClientLevel level = Minecraft.getInstance().level;
                if (level == null) return false;
                return divisible(level, target);
            }
        }

        private static class Level {
            private static void tick(NoraWorld world) {
                MinecraftForge.EVENT_BUS.post(new LevelTick(world));
            }

            private static void second(NoraWorld world) {
                if (divisible(world.level(), 20))
                    MinecraftForge.EVENT_BUS.post(new LevelSecond(world));
            }

            private static void five(NoraWorld world) {
                if (divisible(world.level(), 100))
                    MinecraftForge.EVENT_BUS.post(new LevelFive(world));
            }

            private static void minute(NoraWorld world) {
                if (divisible(world.level(), 1200))
                    MinecraftForge.EVENT_BUS.post(new LevelMinute(world));
            }
        }

        private static boolean divisible(LevelAccessor level, int target) {
            return NoraMath.divisible(level.getLevelData().getGameTime(), target);
        }
    }
}
