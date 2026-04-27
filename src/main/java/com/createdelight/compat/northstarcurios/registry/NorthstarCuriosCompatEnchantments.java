package com.createdelight.compat.northstarcurios.registry;

import com.createdelight.compat.northstarcurios.NorthstarCuriosCompatMod;
import com.createdelight.compat.northstarcurios.enchantment.SpaceWalkEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class NorthstarCuriosCompatEnchantments {

    public static final DeferredRegister<Enchantment> ENCHANTMENTS =
            DeferredRegister.create(ForgeRegistries.ENCHANTMENTS, NorthstarCuriosCompatMod.MOD_ID);

    public static final RegistryObject<Enchantment> SPACE_WALK =
            ENCHANTMENTS.register("space_walk", SpaceWalkEnchantment::new);

    private NorthstarCuriosCompatEnchantments() {
    }
}
