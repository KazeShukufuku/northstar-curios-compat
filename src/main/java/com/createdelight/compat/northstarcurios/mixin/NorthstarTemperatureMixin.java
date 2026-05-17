package com.createdelight.compat.northstarcurios.mixin;

import com.createdelight.compat.northstarcurios.util.NullSafety;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import com.lightning.northstar.world.temperature.NorthstarTemperature;

@Mixin(value = NorthstarTemperature.class, remap = false)
public class NorthstarTemperatureMixin {

    private static final int REQUIRED_PROTECTION_SCORE = 4;

        private static final TagKey<Item> INSULATING_TAG = NullSafety.northstarItemTag("insulating");

        private static final TagKey<Item> INSULATING_2_TAG = NullSafety.northstarItemTag("insulating_2");

        private static final TagKey<Item> HEAT_RESISTANT_TAG = NullSafety.northstarItemTag("heat_resistant");

        private static final TagKey<Item> HEAT_RESISTANT_2_TAG = NullSafety.northstarItemTag("heat_resistant_2");

    @Inject(method = "hasInsulation", at = @At("HEAD"), cancellable = true, remap = false)
    private static void northstarCuriosCompat$hasInsulation(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (countProtectionScore(entity, INSULATING_TAG, INSULATING_2_TAG) >= REQUIRED_PROTECTION_SCORE) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "hasHeatProtection", at = @At("HEAD"), cancellable = true, remap = false)
    private static void northstarCuriosCompat$hasHeatProtection(LivingEntity entity, CallbackInfoReturnable<Boolean> cir) {
        if (countProtectionScore(entity, HEAT_RESISTANT_TAG, HEAT_RESISTANT_2_TAG) >= REQUIRED_PROTECTION_SCORE) {
            cir.setReturnValue(true);
        }
    }

    private static int countProtectionScore(LivingEntity entity, TagKey<Item> baseTag, TagKey<Item> advancedTag) {
        int score = 0;

        score += getProtectionValue(entity.getItemBySlot(EquipmentSlot.HEAD), baseTag, advancedTag);
        score += getProtectionValue(entity.getItemBySlot(EquipmentSlot.CHEST), baseTag, advancedTag);
        score += getProtectionValue(entity.getItemBySlot(EquipmentSlot.LEGS), baseTag, advancedTag);
        score += getProtectionValue(entity.getItemBySlot(EquipmentSlot.FEET), baseTag, advancedTag);

        var inventoryOptional = CuriosApi.getCuriosInventory(entity);
        if (inventoryOptional.isEmpty()) {
            return score;
        }

        ICuriosItemHandler inventory = inventoryOptional.get();
        for (SlotResult slotResult : inventory.findCurios(stack -> stack.is(NullSafety.nonNull(baseTag)) || stack.is(NullSafety.nonNull(advancedTag)))) {
            ItemStack liveStack = resolveLiveCuriosStack(inventory, slotResult);
            if (!liveStack.isEmpty()) {
                score += getProtectionValue(liveStack, baseTag, advancedTag);
            }
        }

        return score;
    }

    private static int getProtectionValue(ItemStack stack, TagKey<Item> baseTag, TagKey<Item> advancedTag) {
        if (stack.is(NullSafety.nonNull(advancedTag))) {
            return 2;
        }
        if (stack.is(NullSafety.nonNull(baseTag))) {
            return 1;
        }
        return 0;
    }

    private static ItemStack resolveLiveCuriosStack(ICuriosItemHandler inventory, SlotResult slotResult) {
        var slotContext = slotResult.slotContext();
        var handlerOptional = inventory.getStacksHandler(slotContext.identifier());

        if (handlerOptional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ICurioStacksHandler stacksHandler = handlerOptional.get();
        IDynamicStackHandler stacks = slotContext.cosmetic()
                ? stacksHandler.getCosmeticStacks()
                : stacksHandler.getStacks();

        int slotIndex = slotContext.index();
        if (slotIndex < 0 || slotIndex >= stacks.getSlots()) {
            return ItemStack.EMPTY;
        }

        return stacks.getStackInSlot(slotIndex);
    }
}