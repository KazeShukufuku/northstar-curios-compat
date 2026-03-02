package com.createdelight.compat.northstarcurios.mixin;

import com.lightning.northstar.world.oxygen.NorthstarOxygen;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Method;

@Mixin(targets = "com.lightning.northstar.block.tech.oxygen_filler.OxygenFillerBlockEntity$1", remap = false)
public class OxygenFillerFluidHandlerMixin {

    private static final int EXPANDED_OXYGEN_CAPACITY = 3600;

    private static final TagKey<Item> OXYGEN_SOURCE_TAG_2 = TagKey.create(
            Registries.ITEM,
            new ResourceLocation("northstar", "oxygen_sources_2")
    );

    private ItemStack getContainedItem(Object self) {
        try {
            Method this0Getter = self.getClass().getDeclaredMethod("getContainedItem");
            this0Getter.setAccessible(true);
            Object value = this0Getter.invoke(self);
            return value instanceof ItemStack stack ? stack : ItemStack.EMPTY;
        } catch (ReflectiveOperationException ignored) {
            try {
                var outerField = self.getClass().getDeclaredField("this$0");
                outerField.setAccessible(true);
                Object outer = outerField.get(self);
                Method method = outer.getClass().getDeclaredMethod("getContainedItem");
                method.setAccessible(true);
                Object value = method.invoke(outer);
                return value instanceof ItemStack stack ? stack : ItemStack.EMPTY;
            } catch (ReflectiveOperationException e) {
                return ItemStack.EMPTY;
            }
        }
    }

    private void sendData(Object self) {
        try {
            var outerField = self.getClass().getDeclaredField("this$0");
            outerField.setAccessible(true);
            Object outer = outerField.get(self);
            Method method = outer.getClass().getMethod("sendData");
            method.invoke(outer);
        } catch (ReflectiveOperationException ignored) {
        }
    }

    private static boolean isExpandedTank(ItemStack stack) {
        return !stack.isEmpty() && stack.is(OXYGEN_SOURCE_TAG_2);
    }

    @Inject(method = "getTankCapacity", at = @At("HEAD"), cancellable = true, remap = false)
    private void northstarCuriosCompat$expandTankCapacity(int tank, CallbackInfoReturnable<Integer> cir) {
        if (tank != 0) {
            return;
        }

        ItemStack stack = getContainedItem(this);

        if (isExpandedTank(stack)) {
            cir.setReturnValue(EXPANDED_OXYGEN_CAPACITY);
        }
    }

    @Inject(method = "fill", at = @At("HEAD"), cancellable = true, remap = false)
    private void northstarCuriosCompat$expandFillCapacity(
            FluidStack resource,
            IFluidHandler.FluidAction action,
            CallbackInfoReturnable<Integer> cir
    ) {
        ItemStack stack = getContainedItem(this);

        if (!isExpandedTank(stack)) {
            return;
        }

        if (!NorthstarOxygen.isOxygen(resource.getFluid())) {
            cir.setReturnValue(0);
            return;
        }

        CompoundTag tag = stack.getOrCreateTag();
        int oxygen = tag.getInt("Oxygen");
        int fillAmount = Mth.clamp(EXPANDED_OXYGEN_CAPACITY - oxygen, 0, resource.getAmount());

        if (action.execute() && fillAmount > 0) {
            tag.putInt("Oxygen", oxygen + fillAmount);
            sendData(this);
        }

        cir.setReturnValue(fillAmount);
    }
}
