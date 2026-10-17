package net.noralunan.library.common.event;

import net.minecraft.server.MinecraftServer;
import net.noralunan.library.common.NoraWorld;

public class NoraTickEvent {
    private NoraTickEvent() {}

    public static class ServerTick extends NoraTickEvent {
        private final MinecraftServer server;
        ServerTick(MinecraftServer server) {
            this.server = server;
        }

        @SuppressWarnings("unused")
        public MinecraftServer getServer() {
            return server;
        }
    }

    public static class ClientTick extends NoraTickEvent {
        ClientTick() {
        }
    }

    public static class LevelTick extends NoraTickEvent {
        private final NoraWorld world;
        LevelTick(NoraWorld world) {
            this.world = world;
        }

        @SuppressWarnings("unused")
        public boolean isClientSide() {
            return world.isClientSide();
        }

        @SuppressWarnings("unused")
        public NoraWorld getWorld() {
            return world;
        }
    }
}
