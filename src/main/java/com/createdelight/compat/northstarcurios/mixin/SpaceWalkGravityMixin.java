package com.createdelight.compat.northstarcurios.mixin;

import com.createdelight.compat.northstarcurios.config.NorthstarCuriosCompatConfig;
import com.createdelight.compat.northstarcurios.registry.NorthstarCuriosCompatEnchantments;
import com.createdelight.compat.northstarcurios.util.NullSafety;
import com.lightning.northstar.world.dimension.NorthstarPlanets;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// priority > 1000 ensures we run AFTER Northstar's GravityStuffMixin (default 1000),
// so our correction is applied on top of Northstar's low-gravity addition.
@Mixin(value = LivingEntity.class, priority = 1500)
public class SpaceWalkGravityMixin {

    private static final double BASE_GRAVITY_CONSTANT = 0.08D;

    @Inject(method = "travel", at = @At("TAIL"), remap = false, require = 0)
    private void northstarCuriosCompat$restoreEarthGravity(Vec3 travelVector, CallbackInfo ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (!(entity instanceof Player player) || !hasSpaceWalkEquipped(player)) {
            return;
        }

        if (NorthstarPlanets.isInOrbit(entity.level().dimension())) {
            return;
        }

        double gravMultiplier = NorthstarPlanets.getGravMultiplier(entity.level().dimension());
        if (gravMultiplier >= 0.999D) {
            return;
        }

        if (entity.isNoGravity() || entity.isInWater() || entity.isInLava() || entity.hasEffect(NullSafety.nonNull(MobEffects.SLOW_FALLING))) {
            return;
        }
        if (entity.isFallFlying() || entity.isInFluidType()) {
            return;
        }

        Vec3 vel = entity.getDeltaMovement();
        double correction = BASE_GRAVITY_CONSTANT - (BASE_GRAVITY_CONSTANT * gravMultiplier);
        entity.setDeltaMovement(vel.x(), vel.y() - correction, vel.z());
    }

    private static boolean hasSpaceWalkEquipped(Player player) {
        var lookup = player.level().registryAccess().lookup(NullSafety.nonNull(Registries.ENCHANTMENT));
        if (lookup.isEmpty()) return false;
        var holderOpt = NullSafety.nonNull(lookup.get()).get(NullSafety.nonNull(NorthstarCuriosCompatEnchantments.SPACE_WALK));
        if (holderOpt.isEmpty()) return false;
        Holder<Enchantment> spaceWalk = NullSafety.nonNull(holderOpt.get());

        for (EquipmentSlot slot : NorthstarCuriosCompatConfig.allowedEquipmentSlots()) {
            ItemStack stack = player.getItemBySlot(NullSafety.nonNull(slot));
            if (!stack.isEmpty() && stack.getOrDefault(NullSafety.enchantmentsComponent(), NullSafety.emptyEnchantments()).getLevel(spaceWalk) > 0) {
                return true;
            }
        }

        return false;
    }
}
