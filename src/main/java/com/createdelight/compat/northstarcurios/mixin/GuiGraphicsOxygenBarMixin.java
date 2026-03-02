package com.createdelight.compat.northstarcurios.mixin;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiGraphics.class)
public class GuiGraphicsOxygenBarMixin {

    private static final int EXPANDED_OXYGEN_CAPACITY = 3600;
    private static final int OXYGEN_BAR_BACKGROUND_COLOR = -16777216;
    private static final int OXYGEN_BAR_FOREGROUND_COLOR = -11691782;

    private static final TagKey<Item> OXYGEN_SOURCE_TAG_2 = TagKey.create(
            Registries.ITEM,
            new ResourceLocation("northstar", "oxygen_sources_2")
    );

    @Inject(
            method = "renderItemDecorations(Lnet/minecraft/client/gui/Font;Lnet/minecraft/world/item/ItemStack;IILjava/lang/String;)V",
            at = @At("TAIL")
    )
    private void northstarCuriosCompat$renderTag2OxygenBar(Font font, ItemStack stack, int x, int y, String text, CallbackInfo ci) {
        if (!stack.is(OXYGEN_SOURCE_TAG_2)) {
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int oxygen = Math.max(0, tag.getInt("Oxygen"));
        float filled = oxygen / (float) EXPANDED_OXYGEN_CAPACITY;
        int barWidth = (int) (13.0F * filled);

        GuiGraphics guiGraphics = (GuiGraphics) (Object) this;
        int barX = x + 2;
        int barY = y + 14;

        guiGraphics.fill(RenderType.guiOverlay(), barX, barY, barX + 13, barY + 2, OXYGEN_BAR_BACKGROUND_COLOR);
        guiGraphics.fill(RenderType.guiOverlay(), barX, barY, barX + barWidth, barY + 1, OXYGEN_BAR_FOREGROUND_COLOR);
    }
}