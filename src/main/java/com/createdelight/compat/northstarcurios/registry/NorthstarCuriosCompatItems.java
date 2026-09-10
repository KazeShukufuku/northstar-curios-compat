package com.createdelight.compat.northstarcurios.registry;

import com.createdelight.compat.northstarcurios.NorthstarCuriosCompatMod;
import com.createdelight.compat.northstarcurios.item.OxygenTankItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = NorthstarCuriosCompatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class NorthstarCuriosCompatItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "ncc");
    public static final RegistryObject<Item> OXYGEN_TANK = ITEMS.register("oxygen_tank", OxygenTankItem::new);
    public static final RegistryObject<Item> STURDY_OXYGEN_TANK = ITEMS.register("sturdy_oxygen_tank", OxygenTankItem::new);

    private NorthstarCuriosCompatItems() {
    }

    @SubscribeEvent
    public static void addCreativeTabItems(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey().location().equals(new ResourceLocation("northstar", "northstar_items"))) {
            event.accept(OXYGEN_TANK);
            event.accept(STURDY_OXYGEN_TANK);
        }
    }
}
