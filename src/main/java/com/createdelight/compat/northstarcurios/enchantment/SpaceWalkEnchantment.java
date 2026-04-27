package com.createdelight.compat.northstarcurios.enchantment;

import com.createdelight.compat.northstarcurios.config.NorthstarCuriosCompatConfig;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class SpaceWalkEnchantment extends Enchantment {

    // Custom category whose predicate reads config at runtime.
    // This is the single gate for all enchanting paths:
    // enchanting table, anvil, /enchant command — they all call category.canEnchant(item).
    public static final EnchantmentCategory CATEGORY = EnchantmentCategory.create(
            "SPACE_WALK",
            item -> {
                if (item instanceof ArmorItem armorItem) {
                    return NorthstarCuriosCompatConfig.allowedEquipmentSlots()
                            .contains(armorItem.getEquipmentSlot());
                }
                return false;
            });

    public SpaceWalkEnchantment() {
        super(Rarity.UNCOMMON, CATEGORY,
                new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD});
    }

    @Override
    public int getMinCost(int level) {
        return 20;
    }

    @Override
    public int getMaxCost(int level) {
        return getMinCost(level) + 30;
    }

    @Override
    public int getMaxLevel() {
        return 2;
    }

    @Override
    public boolean isTradeable() {
        return true;
    }

    @Override
    public boolean isDiscoverable() {
        return true;
    }
}
