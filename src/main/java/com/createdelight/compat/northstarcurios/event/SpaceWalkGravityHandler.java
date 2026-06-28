package com.createdelight.compat.northstarcurios.event;

import com.createdelight.compat.northstarcurios.NorthstarCuriosCompatMod;
import com.createdelight.compat.northstarcurios.config.NorthstarCuriosCompatConfig;
import com.createdelight.compat.northstarcurios.registry.NorthstarCuriosCompatEnchantments;
import com.createdelight.compat.northstarcurios.util.NullSafety;
import com.lightning.northstar.accessor.NorthstarLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = NorthstarCuriosCompatMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class SpaceWalkGravityHandler {

    private static final String SPACE_WALK_DURABILITY_PROGRESS_KEY =
            "northstar_curios_compat_space_walk_durability_progress";

    private SpaceWalkGravityHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!player.isAlive()) {
            return;
        }

        EquippedSpaceWalkSource source = findSpaceWalkSource(player);
        if (source == null) {
            clearDurabilityProgress(player);
            return;
        }

        double gravMultiplier = ((NorthstarLevel) player.level()).northstar$gravityScale();
        if (gravMultiplier >= 0.999D) {
            clearDurabilityProgress(player);
            return;
        }

        if (player.isNoGravity() || player.isInWater() || player.isInLava() || player.hasEffect(NullSafety.nonNull(MobEffects.SLOW_FALLING))) {
            return;
        }
        if (player.isFallFlying() || player.isInFluidType()) {
            return;
        }

        if (!player.level().isClientSide()) {
            consumeDurability(player, source);
        }
    }

    private static void consumeDurability(Player player, EquippedSpaceWalkSource source) {
        double durabilityPerSecond = NorthstarCuriosCompatConfig.durabilityPerSecond();
        if (durabilityPerSecond <= 0 || player.isCreative() || !source.stack().isDamageableItem()) {
            clearDurabilityProgress(player);
            return;
        }

        CompoundTag persistent = player.getPersistentData();
        double progress = persistent.getDouble(SPACE_WALK_DURABILITY_PROGRESS_KEY);
        progress += durabilityPerSecond / 20.0D;

        while (progress >= 1.0D) {
            progress -= 1.0D;

            if (source.stack().isEmpty()) {
                progress = 0.0D;
                break;
            }

            source.stack().hurtAndBreak(1, player, NullSafety.nonNull(source.slot()));
            if (source.stack().isEmpty()) {
                progress = 0.0D;
                break;
            }
        }

        if (progress <= 0.0D) {
            persistent.remove(SPACE_WALK_DURABILITY_PROGRESS_KEY);
        } else {
            persistent.putDouble(SPACE_WALK_DURABILITY_PROGRESS_KEY, progress);
        }
    }

    private static void clearDurabilityProgress(Player player) {
        player.getPersistentData().remove(SPACE_WALK_DURABILITY_PROGRESS_KEY);
    }

    private static EquippedSpaceWalkSource findSpaceWalkSource(Player player) {
        var lookup = player.level().registryAccess().lookup(NullSafety.nonNull(Registries.ENCHANTMENT));
        if (lookup.isEmpty()) return null;
        var holderOpt = lookup.get().get(NullSafety.nonNull(NorthstarCuriosCompatEnchantments.SPACE_WALK));
        if (holderOpt.isEmpty()) return null;
        Holder<Enchantment> spaceWalk = NullSafety.nonNull(holderOpt.get());

        for (EquipmentSlot slot : NorthstarCuriosCompatConfig.allowedEquipmentSlots()) {
            ItemStack stack = player.getItemBySlot(NullSafety.nonNull(slot));
            if (stack.isEmpty()) continue;
            if (stack.getOrDefault(NullSafety.enchantmentsComponent(), NullSafety.emptyEnchantments()).getLevel(spaceWalk) > 0) {
                return new EquippedSpaceWalkSource(slot, stack);
            }
        }

        return null;
    }

    private record EquippedSpaceWalkSource(EquipmentSlot slot, ItemStack stack) {
    }
}
