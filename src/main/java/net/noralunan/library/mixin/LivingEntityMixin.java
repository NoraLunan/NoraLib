package net.noralunan.library.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.noralunan.library.NoraLib;
import net.noralunan.library.common.NoraLiving;
import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LivingEntity.class)
@Implements(@Interface(iface = NoraLiving.class, prefix = NoraLib.MOD_ID + "$"))
public abstract class LivingEntityMixin {}
