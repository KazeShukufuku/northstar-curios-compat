package com.createdelight.compat.northstarcurios.mixin;

import com.createdelight.compat.northstarcurios.util.NullSafety;
import com.createdelight.compat.northstarcurios.api.EquipmentChecks;
import com.lightning.northstar.content.NorthstarTags;
import com.lightning.northstar.world.oxygen.NorthstarOxygen;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingBreatheEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

@Mixin(value = NorthstarOxygen.class, remap = false)
public class NorthstarOxygenMixin {

    private static final int DEFAULT_OXYGEN_CAPACITY = 1800;
    private static final int EXPANDED_OXYGEN_CAPACITY = 3600;

    private static final TagKey<Item> OXYGEN_SOURCE_TAG = NullSafety.northstarItemTag("oxygen_sources");

    private static final TagKey<Item> OXYGEN_SOURCE_TAG_2 = NullSafety.northstarItemTag("oxygen_sources_2");

    private static final TagKey<Item> FULL_SEAL_TAG = NullSafety.northstarItemTag("full_seal");

    private static boolean isAnyOxygenSource(ItemStack stack) {
        return EquipmentChecks.isOxygenSource(stack);
    }

    private static ItemStack resolveLiveCuriosStack(ICuriosItemHandler inventory, SlotResult slotResult) {
        var slotContext = slotResult.slotContext();
        if (slotContext.cosmetic()) {
            return ItemStack.EMPTY;
        }
        var handlerOptional = inventory.getStacksHandler(slotContext.identifier());

        if (handlerOptional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ICurioStacksHandler stacksHandler = handlerOptional.get();
        IDynamicStackHandler stacks = stacksHandler.getStacks();

        int slotIndex = slotContext.index();

        if (slotIndex < 0 || slotIndex >= stacks.getSlots()) {
            return ItemStack.EMPTY;
        }

        return stacks.getStackInSlot(slotIndex);
    }

    private static ItemStack getUsableCuriosOxygenTank(LivingEntity entity) {
        var inventoryOptional = CuriosApi.getCuriosInventory(entity).resolve();

        if (inventoryOptional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ICuriosItemHandler inventory = inventoryOptional.get();

        for (SlotResult slotResult : inventory.findCurios(NorthstarOxygenMixin::isAnyOxygenSource)) {
            ItemStack liveStack = resolveLiveCuriosStack(inventory, slotResult);

            if (!liveStack.isEmpty() && isAnyOxygenSource(liveStack)
                    && canProvideOxygenWithoutConsuming(liveStack)) {
                return liveStack;
            }
        }

        return ItemStack.EMPTY;
    }

    private static boolean canProvideOxygenWithoutConsuming(ItemStack stack) {
        ItemStack probeStack = stack.copy();
        return NorthstarOxygen.depleteOxygen(probeStack, false);
    }

    private static boolean providesFullSeal(ItemStack stack, LivingEntity entity, String slot) {
        return !stack.isEmpty() && (stack.is(NullSafety.nonNull(FULL_SEAL_TAG))
                || EquipmentChecks.query("full_seal", stack, entity, slot) > 0);
    }

    private static boolean hasFullSeal(LivingEntity entity) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR
                    && providesFullSeal(entity.getItemBySlot(slot), entity, slot.getName())) {
                return true;
            }
        }

        var inventoryOptional = CuriosApi.getCuriosInventory(entity).resolve();

        if (inventoryOptional.isEmpty()) {
            return false;
        }

        ICuriosItemHandler inventory = inventoryOptional.get();

        for (SlotResult slotResult : inventory.findCurios(stack -> !stack.isEmpty())) {
            ItemStack liveStack = resolveLiveCuriosStack(inventory, slotResult);
            String slot = "curios:" + slotResult.slotContext().identifier() + ":" + slotResult.slotContext().index();
            if (providesFullSeal(liveStack, entity, slot)) {
                return true;
            }
        }

        return false;
    }

    private static boolean isSealed(LivingEntity entity) {
        if (hasFullSeal(entity)) {
            return true;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() != EquipmentSlot.Type.ARMOR) {
                continue;
            }
            ItemStack stack = entity.getItemBySlot(slot);
            if (stack.isEmpty() || (!NorthstarTags.NorthstarItemTags.OXYGEN_SEALING.matches(stack)
                    && EquipmentChecks.query("oxygen_sealing", stack, entity, slot.getName()) <= 0)) {
                return false;
            }
        }
        return true;
    }

    @Inject(method = "onBreathe", at = @At("HEAD"), cancellable = true, remap = false)
    private static void northstarCuriosCompat$onBreathe(LivingBreatheEvent event, CallbackInfo ci) {
        LivingEntity entity = event.getEntity();

        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }

        if (NorthstarTags.NorthstarEntityTags.DOESNT_REQUIRE_OXYGEN.matches(entity)) {
            return;
        }

        Level level = entity.level();
        NorthstarOxygen oxygen = NorthstarOxygen.getDimension(level);

        if (oxygen.hasOxygen() && event.canBreathe()) {
            return;
        }

        if (oxygen.getSealer(entity.getEyePosition()) != null) {
            return;
        }

        if (!isSealed(entity)) {
            return;
        }

        ItemStack tank = NorthstarOxygen.getOxygenTank(entity);
        boolean shouldConsume = !level.isClientSide() && level.getGameTime() % 20L == 19L;
        if (!tank.isEmpty() && NorthstarOxygen.depleteOxygen(tank, shouldConsume)) {
            event.setCanBreathe(true);
            event.setCanRefillAir(true);
            ci.cancel();
        }
    }

    @Inject(method = "getOxygenTank", at = @At("HEAD"), cancellable = true, remap = false)
    private static void northstarCuriosCompat$preferCuriosTank(LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack tank = getUsableCuriosOxygenTank(entity);

        if (!tank.isEmpty()) {
            cir.setReturnValue(tank);
            return;
        }

        for (ItemStack armor : entity.getArmorSlots()) {
            if (isAnyOxygenSource(armor) && canProvideOxygenWithoutConsuming(armor)) {
                cir.setReturnValue(armor);
                return;
            }
        }
    }

    @WrapOperation(
        method = "onBreathe",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z",
            ordinal = 0,
            remap = true
        ),
        remap = false
    )
    private static boolean northstarCuriosCompat$allowEmptyArmorWithFullSeal(
            ItemStack stack, Operation<Boolean> original, LivingBreatheEvent event) {
        return original.call(stack) && !hasFullSeal(event.getEntity());
    }

    @WrapOperation(
        method = "onBreathe",
        at = @At(
            value = "INVOKE",
            target = "Lcom/lightning/northstar/content/NorthstarTags$NorthstarItemTags;matches(Lnet/minecraft/world/item/ItemStack;)Z"
        ),
        remap = false
    )
    private static boolean northstarCuriosCompat$checkEquipment(
            NorthstarTags.NorthstarItemTags tag, ItemStack stack, Operation<Boolean> original, LivingBreatheEvent event) {
        if (original.call(tag, stack)) {
            return true;
        }
        if (tag == NorthstarTags.NorthstarItemTags.OXYGEN_SOURCES) {
            return isAnyOxygenSource(stack);
        }
        if (tag == NorthstarTags.NorthstarItemTags.OXYGEN_SEALING) {
            LivingEntity entity = event.getEntity();
            return hasFullSeal(entity)
                    || EquipmentChecks.query("oxygen_sealing", stack, entity, EquipmentChecks.armorSlot(entity, stack)) > 0;
        }
        return false;
    }

    @Inject(method = "getTankCapacity", at = @At("HEAD"), cancellable = true, remap = false)
    private static void northstarCuriosCompat$expandTankCapacity(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (stack.is(NullSafety.nonNull(OXYGEN_SOURCE_TAG_2))) {
            cir.setReturnValue(EXPANDED_OXYGEN_CAPACITY);
        } else if (!stack.is(NullSafety.nonNull(OXYGEN_SOURCE_TAG))) {
            int capacity = EquipmentChecks.customOxygenCapacity(stack);
            if (capacity > 0) {
                cir.setReturnValue(capacity);
            }
        }
    }

    @Inject(method = "depleteOxygen", at = @At("HEAD"), cancellable = true, remap = false)
    private static void northstarCuriosCompat$expandOxygenCapacity(ItemStack stack, boolean consume, CallbackInfoReturnable<Boolean> cir) {
        CompoundTag tag = stack.getTag();

        if (tag == null || !tag.contains("Oxygen", Tag.TAG_INT)) {
            cir.setReturnValue(false);
            return;
        }

        int oxygen = tag.getInt("Oxygen");

        if (oxygen <= 0) {
            cir.setReturnValue(false);
            return;
        }

        if (consume) {
            int maxCapacity = stack.is(NullSafety.nonNull(OXYGEN_SOURCE_TAG_2)) ? EXPANDED_OXYGEN_CAPACITY : DEFAULT_OXYGEN_CAPACITY;
            if (!EquipmentChecks.isTaggedOxygenSource(stack)) {
                int capacity = EquipmentChecks.customOxygenCapacity(stack);
                if (capacity > 0) {
                    maxCapacity = capacity;
                }
            }
            tag.putInt("Oxygen", Math.min(oxygen - 1, maxCapacity));
        }

        cir.setReturnValue(true);
    }
}
