package net.noralunan.library.common;

import net.minecraft.server.level.ColumnPos;
import net.minecraft.world.phys.Vec2;

import java.util.concurrent.ThreadLocalRandom;

public class NoraMath {
    public static int round(double target) {
        if (Double.isNaN(target) || Double.isInfinite(target)) return 0;
        if (target > Integer.MAX_VALUE) return Integer.MAX_VALUE;
        if (target < Integer.MIN_VALUE) return Integer.MIN_VALUE;
        return (int) Math.round(target);
    }

    public static int nextInt(int min, int max) {
        if (min > max) {
            throw new IllegalArgumentException();
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static double nextDouble(double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException();
        }
        if (min == max) {
            return max;
        }
        return ThreadLocalRandom.current().nextDouble(min, max);
    }

    public static double nextDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    public static double nextDouble(double bound) {
        return ThreadLocalRandom.current().nextDouble(bound);
    }

    /**
     * @param place 0 ~ 8
     */
    public static double retain(double target, int place) {
        if (place < 0 || place > 8) {
            throw new IllegalArgumentException();
        }
        double carry = Math.pow(10, place);
        return Math.round(carry * target) / carry;
    }

    public static boolean divisible(long a, long b) {
        if (b == 0) {
            return false;
        }
        return Math.floorMod(a, b) == 0;
    }

    public static ColumnPos nextPlane(int x, int z, int min, int max) {
        Vec2 pos = nextPlane((float) x, z, min, max);
        return new ColumnPos(round(pos.x), round(pos.y));
    }

    public static Vec2 nextPlane(float x, float z, double min, double max) {
        if (min > max) {
            throw new IllegalArgumentException();
        }
        double theta = nextDouble() * Math.PI * 2;
        double distance = nextDouble(min, max);
        float rx = x + round(distance * Math.cos(theta));
        float rz = z + round(distance * Math.sin(theta));
        return new Vec2(rx, rz);
    }

    public static boolean between(long target, long left, long right) {
        return target >= left && target <= right;
    }

    public static boolean between(double target, double left, double right) {
        return target >= left && target <= right;
    }

    public static boolean within(long target, long left, long right) {
        return target > left && target < right;
    }

    public static boolean within(double target, double left, double right) {
        return target > left && target < right;
    }

}
