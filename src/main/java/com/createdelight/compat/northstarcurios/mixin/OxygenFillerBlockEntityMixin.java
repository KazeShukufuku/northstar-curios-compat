package com.createdelight.compat.northstarcurios.mixin;

import com.lightning.northstar.content.NorthstarTags;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(targets = "com.lightning.northstar.block.tech.oxygen_filler.OxygenFillerBlockEntity", remap = false)
public class OxygenFillerBlockEntityMixin {

    private static final int DEFAULT_OXYGEN_CAPACITY = 1800;
    private static final int EXPANDED_OXYGEN_CAPACITY = 3600;

    private static final TagKey<Item> OXYGEN_SOURCE_TAG_2 = TagKey.create(
            Registries.ITEM,
            new ResourceLocation("northstar", "oxygen_sources_2")
    );

    private static Container getContainer(Object self) {
        try {
            Field field = self.getClass().getField("container");
            return (Container) field.get(self);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Inject(method = "getContainedItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void northstarCuriosCompat$acceptSecondOxygenSourceTag(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = getContainer(this).getItem(0);

        if (!stack.isEmpty() && stack.is(OXYGEN_SOURCE_TAG_2)) {
            cir.setReturnValue(stack);
        }
    }

    @Redirect(
            method = "addToGoggleTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/lightning/northstar/content/NorthstarTags$NorthstarItemTags;matches(Lnet/minecraft/world/item/ItemStack;)Z"
            ),
            remap = false
    )
    private boolean northstarCuriosCompat$acceptSecondTagInTooltip(NorthstarTags.NorthstarItemTags tag, ItemStack stack) {
        return tag.matches(stack) || stack.is(OXYGEN_SOURCE_TAG_2);
    }

    @ModifyConstant(
            method = "addToGoggleTooltip",
            constant = @Constant(doubleValue = 1800.0D),
            remap = false
    )
    private double northstarCuriosCompat$dynamicOxygenTooltipCapacity(double original) {
        ItemStack stack = getContainer(this).getItem(0);

        if (!stack.isEmpty() && stack.is(OXYGEN_SOURCE_TAG_2)) {
            return EXPANDED_OXYGEN_CAPACITY;
        }

        return DEFAULT_OXYGEN_CAPACITY;
    }
}
