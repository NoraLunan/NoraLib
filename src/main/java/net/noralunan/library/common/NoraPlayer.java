package net.noralunan.library.common;

import net.minecraft.server.level.ServerPlayer;

public class NoraPlayer {
    public static boolean noCheat(ServerPlayer player) {
        return player.gameMode.isSurvival();
    }

    public static boolean isCheat(ServerPlayer player) {
        return player.gameMode.isCreative();
    }

}
