package net.noralunan.library;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.noralunan.library.common.NoraHelper;
import net.noralunan.library.common.NoraLogger;
import net.noralunan.library.common.event.NoraEvent;

import java.util.Objects;

@Mod(NoraLib.MOD_ID)
public class NoraLib
{
    public static final String MOD_ID = "noralib";
    public static final String MOD_NAME = "Nora Lib";
    public static final String VERSION = "0.2.18";

    public NoraLib()
    {
        MinecraftForge.EVENT_BUS.register(NoraEvent.class);
    }

    public static NoraLogger logger()
    {
        return NoraLogger.get(MOD_ID, NoraHelper.STACK_WALKER.getCallerClass());
    }

    public static void confirmNotNull(Object... obj)
    {
        for (Object object : obj)
        {
            Objects.requireNonNull(object);
        }
    }
}
