package net.noralunan.library.data;

import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;

public interface ICapability<T>
{
    @NotNull
    Capability<T> capability();

    default void onRespawn(Player player) {}

    default void onCopy(Player player) {}

    default void onClone(Player player) {}

    default void onAttach(Player player) {}
}
