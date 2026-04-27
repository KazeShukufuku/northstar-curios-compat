package com.createdelight.compat.northstarcurios.mixin;

import com.createdelight.compat.northstarcurios.registry.NorthstarCuriosCompatEnchantments;
import com.createdelight.compat.northstarcurios.util.NullSafety;
import com.lightning.northstar.world.dimension.NorthstarPlanets;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// priority > 1000 ensures we run AFTER Northstar's GravityStuffMixin (default 1000),
// so our correction is applied on top of Northstar's low-gravity addition.
// priority > 1000 so our TAIL fires AFTER Northstar's GravityStuffMixin (default 1000).
// Method name is the SRG name (m_7023_) to bypass refmap lookup entirely,
// which avoids the "No refMap loaded" crash caused by Gradle incremental build
// skipping recompilation when only resources (mixins.json) changed.
@Mixin(value = LivingEntity.class, priority = 1500)
public class SpaceWalkGravityMixin {

    private static final double BASE_GRAVITY_CONSTANT = 0.08D;

    @SuppressWarnings("InvalidMemberReference")
    @Inject(method = "m_7023_", at = @At("TAIL"), remap = false, require = 0)
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

        // Northstar added (0.08 - 0.08*gravMultiplier) to Y this tick.
        // Subtract that same amount to restore net-gravity to earth (0.08).
        // Do NOT set hurtMarked: forced velocity sync interferes with client-side
        // horizontal prediction and makes horizontal movement feel non-existent.
        Vec3 vel = entity.getDeltaMovement();
        double correction = BASE_GRAVITY_CONSTANT - (BASE_GRAVITY_CONSTANT * gravMultiplier);
        entity.setDeltaMovement(vel.x(), vel.y() - correction, vel.z());
    }

    private static boolean hasSpaceWalkEquipped(Player player) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
            ItemStack stack = player.getItemBySlot(NullSafety.nonNull(slot));
            if (stack.isEmpty()) {
                continue;
            }

            if (stack.getEnchantmentLevel(NullSafety.nonNull(NorthstarCuriosCompatEnchantments.SPACE_WALK.get())) > 0) {
                return true;
            }
        }

        return false;
    }
}
