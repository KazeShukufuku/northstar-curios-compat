package com.createdelight.compat.northstarcurios;

import com.createdelight.compat.northstarcurios.config.NorthstarCuriosCompatConfig;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;

@Mod(NorthstarCuriosCompatMod.MOD_ID)
public class NorthstarCuriosCompatMod {
    public static final String MOD_ID = "northstar_curios_compat";

    public NorthstarCuriosCompatMod(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, NorthstarCuriosCompatConfig.SPEC);
    }
}
