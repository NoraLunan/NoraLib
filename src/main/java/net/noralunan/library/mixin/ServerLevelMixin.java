package net.noralunan.library.mixin;

import net.minecraft.server.level.ServerLevel;
import net.noralunan.library.NoraLib;
import net.noralunan.library.common.NoraLevel;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ServerLevel.class)
@Implements(@Interface(iface = NoraLevel.class, prefix = NoraLib.MOD_ID + "$"))
public abstract class ServerLevelMixin {}
