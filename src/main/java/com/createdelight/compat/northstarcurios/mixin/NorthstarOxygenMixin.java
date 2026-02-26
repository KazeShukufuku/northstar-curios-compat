package com.createdelight.compat.northstarcurios.mixin;

import com.lightning.northstar.world.oxygen.NorthstarOxygen;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
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

    private static final TagKey<Item> OXYGEN_SOURCE_TAG = TagKey.create(
            Registries.ITEM,
            new ResourceLocation("northstar", "oxygen_sources")
    );

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

    private static ItemStack getCuriosOxygenTank(LivingEntity entity) {
        var inventoryOptional = CuriosApi.getCuriosInventory(entity).resolve();

        if (inventoryOptional.isEmpty()) {
            return ItemStack.EMPTY;
        }

        ICuriosItemHandler inventory = inventoryOptional.get();

        for (SlotResult slotResult : inventory.findCurios(stack -> stack.is(OXYGEN_SOURCE_TAG))) {
            ItemStack liveStack = resolveLiveCuriosStack(inventory, slotResult);

            if (!liveStack.isEmpty() && liveStack.is(OXYGEN_SOURCE_TAG)) {
                return liveStack;
            }
        }

        return ItemStack.EMPTY;
    }

    @Inject(method = "onBreathe", at = @At("HEAD"), cancellable = true, remap = false)
    private static void northstarCuriosCompat$onBreathe(LivingBreatheEvent event, CallbackInfo ci) {
        LivingEntity entity = event.getEntity();

        if (entity instanceof Player player && (player.isCreative() || player.isSpectator())) {
            return;
        }

        Level level = entity.level();
        NorthstarOxygen oxygen = NorthstarOxygen.getDimension(level);

        if (oxygen.hasOxygen() && event.canBreathe()) {
            return;
        }

        if (oxygen.getSealer(entity.position()) != null) {
            return;
        }

        ItemStack tank = getCuriosOxygenTank(entity);

        if (tank.isEmpty()) {
            return;
        }

        boolean shouldConsume = level.getGameTime() % 20L == 0L;

        if (NorthstarOxygen.depleteOxygen(tank, shouldConsume)) {
            event.setCanBreathe(true);
            event.setCanRefillAir(true);
            ci.cancel();
        }
    }

    @Inject(method = "getOxygenTank", at = @At("HEAD"), cancellable = true, remap = false)
    private static void northstarCuriosCompat$preferCuriosTank(LivingEntity entity, CallbackInfoReturnable<ItemStack> cir) {
        ItemStack tank = getCuriosOxygenTank(entity);

        if (!tank.isEmpty()) {
            cir.setReturnValue(tank);
        }
    }
}
