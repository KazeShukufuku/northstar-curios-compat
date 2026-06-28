package com.createdelight.compat.northstarcurios.mixin;

import com.createdelight.compat.northstarcurios.util.NullSafety;
import com.lightning.northstar.block.tech.oxygen_filler.OxygenFillerBlockEntity;
import com.lightning.northstar.content.NorthstarTags;
import com.lightning.northstar.world.oxygen.NorthstarOxygen;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Field;

@Mixin(value = OxygenFillerBlockEntity.class, remap = false)
public class OxygenFillerBlockEntityMixin {

    private static final int EXPANDED_OXYGEN_CAPACITY = 3600;

    private static final TagKey<Item> OXYGEN_SOURCE_TAG_T2 = NullSafety.northstarItemTag("oxygen_sources_t2");

    private static Container getContainer(Object self) {
        try {
            Field field = self.getClass().getField("container");
            return (Container) field.get(self);
        } catch (ReflectiveOperationException exception) {
            throw new RuntimeException(exception);
        }
    }

    @Inject(method = "getContainedItem", at = @At("HEAD"), cancellable = true, remap = false)
    private void northstarCuriosCompat$acceptT2OxygenSourceTag(CallbackInfoReturnable<ItemStack> cir) {
        ItemStack stack = getContainer(this).getItem(0);

        if (!stack.isEmpty() && stack.is(NullSafety.nonNull(OXYGEN_SOURCE_TAG_T2))) {
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
    private boolean northstarCuriosCompat$acceptT2TagInTooltip(NorthstarTags.NorthstarItemTags tag, ItemStack stack) {
        return tag.matches(stack) || stack.is(NullSafety.nonNull(OXYGEN_SOURCE_TAG_T2));
    }

    @Redirect(
            method = "addToGoggleTooltip",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/lightning/northstar/world/oxygen/NorthstarOxygen;getTankCapacity(Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;)I"
            ),
            remap = false
    )
    private int northstarCuriosCompat$dynamicOxygenTooltipCapacity(Level level, ItemStack stack) {
        if (!stack.isEmpty() && stack.is(NullSafety.nonNull(OXYGEN_SOURCE_TAG_T2))) {
            return EXPANDED_OXYGEN_CAPACITY;
        }

        return NorthstarOxygen.getTankCapacity(level, stack);
    }
}
