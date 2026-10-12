package net.noralunan.library.common;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;

public interface NoraLiving {
    default LivingEntity toLiving() {
        return (LivingEntity) this;
    }

    default float getShield() {
        return toLiving().getAbsorptionAmount();
    }

    default boolean isDeath() {
        return !toLiving().isAlive() && toLiving().deathTime > 0;
    }

    default String getID() {
        ResourceLocation key = ForgeRegistries.ENTITY_TYPES.getKey(toLiving().getType());
        if (key == null)
            return "minecraft:pig";
        return key.toString();
    }

    default ItemStack getHelmet() {
        return toLiving().getItemBySlot(EquipmentSlot.HEAD);
    }

    default ItemStack getChestplate() {
        return toLiving().getItemBySlot(EquipmentSlot.CHEST);
    }

    default ItemStack getLeggings() {
        return toLiving().getItemBySlot(EquipmentSlot.LEGS);
    }

    default ItemStack getBoots() {
        return toLiving().getItemBySlot(EquipmentSlot.FEET);
    }

    default Iterator<ItemStack> getArmors() {
        return toLiving().getArmorSlots().iterator();
    }

    default Iterator<ItemStack> getEquipments() {
        return Stream.of(EquipmentSlot.values())
                .map(slot -> toLiving().getItemBySlot(slot))
                .iterator();
    }


    default CompoundTag getNbt() {
        return toLiving().getPersistentData();
    }

    default boolean getBool(String key) {
        return getNbt().getBoolean(key);
    }

    default void putBool(String key, boolean value) {
        getNbt().putBoolean(key, value);
    }

    default byte getByte(String key) {
        return getNbt().getByte(key);
    }

    default void putByte(String key, byte value) {
        getNbt().putByte(key, value);
    }

    default int getInt(String key) {
        return getNbt().getInt(key);
    }

    default void putInt(String key, int value) {
        getNbt().putInt(key, value);
    }

    default double getDouble(String key) {
        return getNbt().getDouble(key);
    }

    default void putDouble(String key, double value) {
        getNbt().putDouble(key, value);
    }

    default String getString(String key) {
        return getNbt().getString(key);
    }

    default void putString(String key, String value) {
        getNbt().putString(key, value);
    }

    default Set<String> tags() {
        return toLiving().getTags();
    }

    default boolean containsTag(String tag) {
        return tags().contains(tag);
    }

    default void addTag(String tag) {
        tags().add(tag);
    }

    default void removeTag(String tag) {
        tags().remove(tag);
    }

}
