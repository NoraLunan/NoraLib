package net.noralunan.library.mixin;

import net.minecraft.world.level.Level;
import net.noralunan.library.NoraLib;
import net.noralunan.library.common.NoraWorld;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Level.class)
@Implements(@Interface(iface = NoraWorld.class, prefix = NoraLib.MOD_ID + "$"))
public abstract class LevelMixin {}
