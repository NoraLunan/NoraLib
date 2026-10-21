package net.noralunan.library.common;

import net.minecraftforge.common.ForgeConfigSpec;

public class NoraHelper {
    public static final StackWalker STACK_WALKER = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);

    public static <E extends Enum<E>> E getEnum(Class<E> eClass, String value) throws IllegalArgumentException {
        return Enum.valueOf(eClass, value);
    }

    public static <E extends Enum<E>> E getEnum(Class<E> eClass, String value, String def) {
        try {
            return Enum.valueOf(eClass, value);
        } catch (IllegalArgumentException exception) {
            return Enum.valueOf(eClass, def);
        }
    }

    public static <E extends Enum<E>> E getEnum(Class<E> eClass, String value, E def) {
        try {
            return Enum.valueOf(eClass, value);
        } catch (IllegalArgumentException exception) {
            return def;
        }
    }

    public static <E extends Enum<E>> E getEnum(Class<E> eClass, ForgeConfigSpec.ConfigValue<String> entry) {
        try {
            return Enum.valueOf(eClass, entry.get());
        } catch (IllegalArgumentException exception) {
            return Enum.valueOf(eClass, entry.getDefault());
        }
    }
}
