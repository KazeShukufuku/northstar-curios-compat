package com.createdelight.compat.northstarcurios;

import com.createdelight.compat.northstarcurios.config.NorthstarCuriosCompatConfig;
import com.createdelight.compat.northstarcurios.registry.NorthstarCuriosCompatEnchantments;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NorthstarCuriosCompatMod.MOD_ID)
public class NorthstarCuriosCompatMod {
    public static final String MOD_ID = "northstar_curios_compat";

    @SuppressWarnings("removal") // FMLJavaModLoadingContext.get() is correct API for 1.20.1; deprecated annotation targets 1.21.1+
    public NorthstarCuriosCompatMod() {
        var modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        NorthstarCuriosCompatEnchantments.ENCHANTMENTS.register(modEventBus);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, NorthstarCuriosCompatConfig.SPEC);
    }
}
