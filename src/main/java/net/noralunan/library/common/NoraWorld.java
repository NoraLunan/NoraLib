package net.noralunan.library.common;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.LevelData;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Objects;

public interface NoraWorld {
    default Level level() {
        return (Level) this;
    }

    default LevelAccessor levelAccessor() {
        return (LevelAccessor) this;
    }

    default boolean isClientSide() {
        return levelAccessor().isClientSide();
    }

    default LevelData levelData() {
        return levelAccessor().getLevelData();
    }

    default long worldTime() {
        return levelData().getDayTime();
    }

    default int dayTime() {
        long dayTime = Math.floorMod(worldTime(), 24000);
        return Math.toIntExact(dayTime);
    }

    default long playTime() {
        return levelData().getGameTime();
    }

    default int day() {
        return Math.toIntExact(Math.floorDiv(worldTime(), 24000));
    }

    default boolean isFullMoon() {
        return levelAccessor().getMoonPhase() == 0;
    }

    default int worldSurface(int x, int z) {
        if (Math.abs(x) > 3e7 || Math.abs(z) > 3e7) {
            return 0;
        }
        return levelAccessor().getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
    }

    default Brightness brightness(double x, double y, double z) {
        return new Brightness(levelAccessor(), BlockPos.containing(x, y, z));
    }

    default Brightness brightness(BlockPos pos) {
        return new Brightness(levelAccessor(), pos);
    }

    class Brightness {
        public final int block;
        public final int sky;
        private Brightness(LevelAccessor level, BlockPos pos) {
            this.block = level.getBrightness(LightLayer.BLOCK, pos);
            this.sky = level.getBrightness(LightLayer.SKY, pos);
        }
    }

    default String dimensionID() {
        try {
            return level().dimension().location().toString();
        } catch (Exception exception) {
            return "minecraft:overworld";
        }
    }

    default boolean raining() {
        return levelData().isRaining();
    }

    default boolean thundering() {
        return levelData().isThundering();
    }

    default boolean isPeaceful() {
        return levelAccessor().getDifficulty() == Difficulty.PEACEFUL;
    }

    default boolean isEasy() {
        return levelAccessor().getDifficulty() == Difficulty.EASY;
    }

    default boolean isNormal() {
        return levelAccessor().getDifficulty() == Difficulty.NORMAL;
    }

    default boolean isHard() {
        return levelAccessor().getDifficulty() == Difficulty.HARD;
    }

    default boolean isHardcore() {
        return levelData().isHardcore();
    }

    default int playerCount() {
        if (level().isClientSide()) {
            return Objects.requireNonNull(Minecraft.getInstance().getConnection()).getOnlinePlayers().size();
        } else {
            return ServerLifecycleHooks.getCurrentServer().getPlayerCount();
        }
    }

}
