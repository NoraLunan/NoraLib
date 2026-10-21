package net.noralunan.library.common;

import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class NoraLogger {
    /**
     * @see NoraHelper
     */
    @Deprecated(forRemoval = true, since = "0.2.16")
    public static final StackWalker STACK_WALKER = NoraHelper.STACK_WALKER;

    public static NoraLogger get() {
        Class<?> clazz = NoraHelper.STACK_WALKER.getCallerClass();
        return clazz.isAnnotationPresent(Mod.class)
                ? get(clazz.getAnnotation(Mod.class).value(), "MainClass")
                : get(clazz.getName(), "");
    }

    public static NoraLogger get(String name) {
        return new NoraLogger(name, NoraHelper.STACK_WALKER.getCallerClass().getSimpleName());
    }

    public static NoraLogger get(String name, Class<?> clazz) {
        return get(name, clazz.getSimpleName());
    }

    public static NoraLogger get(String name, String marker) {
        return new NoraLogger(name, marker);
    }


    private final Logger logger;
    private final Marker marker;
    private NoraLogger(String name, String marker) {
        this.logger = LoggerFactory.getLogger(name);
        this.marker = MarkerFactory.getMarker(marker);
    }

    public void trace(String string) {
        this.logger.trace(this.marker, string);
    }

    public void trace(String message, Object... p) {
        this.logger.trace(this.marker, message, p);
    }

    public void info(String string) {
        this.logger.info(this.marker, string);
    }

    public void info(String message, Object... p) {
        this.logger.info(this.marker, message, p);
    }

    public void debug(String message) {
        this.logger.debug(this.marker, message);
    }

    public void debug(String message, Object... p) {
        this.logger.debug(this.marker, message, p);
    }

    public void warn(String message) {
        this.logger.warn(this.marker, message);
    }

    public void warn(String message, Object... p) {
        this.logger.warn(this.marker, message, p);
    }

    public void error(String message) {
        this.logger.error(this.marker, message);
    }

    public void error(String message, Object... p) {
        this.logger.error(this.marker, message, p);
    }

}
