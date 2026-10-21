package net.noralunan.library.data;

import net.minecraftforge.common.capabilities.Capability;

public interface ICapability<T>
{
    Capability<T> capability();
}
