package com.createdelight.compat.northstarcurios.registry;

import com.createdelight.compat.northstarcurios.NorthstarCuriosCompatMod;
import com.createdelight.compat.northstarcurios.util.NullSafety;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Enchantments are data-driven in 1.21.1. The actual enchantment definition lives in
 * data/northstar_curios_compat/enchantment/space_walk.json.
 * This class only holds the ResourceKey used to look up the enchantment at runtime.
 */
public final class NorthstarCuriosCompatEnchantments {

    public static final ResourceKey<Enchantment> SPACE_WALK = ResourceKey.create(
            NullSafety.nonNull(Registries.ENCHANTMENT),
            NullSafety.nonNull(ResourceLocation.fromNamespaceAndPath(NorthstarCuriosCompatMod.MOD_ID, "space_walk"))
    );

    private NorthstarCuriosCompatEnchantments() {
    }
}
