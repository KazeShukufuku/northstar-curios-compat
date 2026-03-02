package com.createdelight.compat.northstarcurios.client;

import com.createdelight.compat.northstarcurios.NorthstarCuriosCompatMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.text.NumberFormat;
import java.util.Locale;

@Mod.EventBusSubscriber(modid = NorthstarCuriosCompatMod.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class OxygenTooltipHandler {

    private static final NumberFormat OXYGEN_NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    private static final TagKey<Item> OXYGEN_SOURCE_TAG_2 = TagKey.create(
            Registries.ITEM,
            new ResourceLocation("northstar", "oxygen_sources_2")
    );

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        if (stack.isEmpty() || !stack.is(OXYGEN_SOURCE_TAG_2)) {
            return;
        }

        CompoundTag tag = stack.getTag();
        int oxygen = tag != null ? tag.getInt("Oxygen") : 0;

        if (oxygen < 0) {
            oxygen = 0;
        }

        String formattedOxygen = OXYGEN_NUMBER_FORMAT.format(oxygen);

        MutableComponent line = Component.translatable("northstar.gui.tooltip.oxygen")
            .append(formattedOxygen)
            .append("mB")
                .withStyle(ChatFormatting.GRAY);

        int insertionIndex = Math.min(1, event.getToolTip().size());
        event.getToolTip().add(insertionIndex, line);
    }
}