package com.createdelight.compat.northstarcurios.client;

import com.createdelight.compat.northstarcurios.NorthstarCuriosCompatMod;
import com.createdelight.compat.northstarcurios.util.NullSafety;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.text.NumberFormat;
import java.util.Locale;

@EventBusSubscriber(modid = NorthstarCuriosCompatMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME, value = Dist.CLIENT)
public class OxygenTooltipHandler {

    private static final NumberFormat OXYGEN_NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    private static final TagKey<Item> OXYGEN_SOURCE_TAG_2 = NullSafety.northstarItemTag("oxygen_sources_2");

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();

        if (stack.isEmpty() || !stack.is(NullSafety.nonNull(OXYGEN_SOURCE_TAG_2))) {
            return;
        }

        int oxygen = NullSafety.getOxygen(stack);

        if (oxygen < 0) {
            oxygen = 0;
        }

        String formattedOxygen = OXYGEN_NUMBER_FORMAT.format(oxygen);

        MutableComponent line = Component.translatable("northstar.gui.tooltip.oxygen")
            .append(NullSafety.nonNull(formattedOxygen))
            .append("mB")
                .withStyle(ChatFormatting.GRAY);

        int insertionIndex = Math.min(1, event.getToolTip().size());
        event.getToolTip().add(insertionIndex, line);
    }
}