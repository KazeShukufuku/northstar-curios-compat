package com.createdelight.compat.northstarcurios.mixin;

import com.createdelight.compat.northstarcurios.config.NorthstarCuriosCompatConfig;
import com.createdelight.compat.northstarcurios.registry.NorthstarCuriosCompatEnchantments;
import com.createdelight.compat.northstarcurios.util.NullSafety;
import com.lightning.northstar.accessor.NorthstarLevel;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Priority 500 < Northstar default 1000.
// For HEAD injections, lower priority is applied earlier to bytecode,
// meaning at runtime our HEAD code fires AFTER Northstar's HEAD code.
// This lets us override the reduced fall damage Northstar already set.
@Mixin(value = LivingEntity.class, priority = 500)
public class SpaceWalkFallDamageMixin {

    @Inject(method = "calculateFallDamage", at = @At("HEAD"), remap = false, require = 0, cancellable = true)
    private void northstarCuriosCompat$restoreEarthFallDamage(
            float pFallDistance, float pDamageMultiplier, CallbackInfoReturnable<Integer> ci) {
        LivingEntity entity = (LivingEntity) (Object) this;

        if (!(entity instanceof Player player)) {
            return;
        }

        if (((NorthstarLevel) entity.level()).northstar$gravityScale() >= 0.999D) {
            return;
        }

        // Space Walk I: restore earth fall damage
        // Space Walk II: keep Northstar's reduced fall damage (do nothing)
        int spaceWalkLevel = getSpaceWalkLevel(player);
        if (spaceWalkLevel != 1) {
            return;
        }

        // Vanilla / earth fall damage formula
        MobEffectInstance jumpEffect = entity.getEffect(NullSafety.nonNull(MobEffects.JUMP));
        float jumpBonus = jumpEffect == null ? 0.0F : (float) (jumpEffect.getAmplifier() + 1);
        ci.setReturnValue(Mth.ceil((pFallDistance - 3.0F - jumpBonus) * pDamageMultiplier));
    }

    private static int getSpaceWalkLevel(Player player) {
        var lookup = player.level().registryAccess().lookup(NullSafety.nonNull(Registries.ENCHANTMENT));
        if (lookup.isEmpty()) return 0;
        var holderOpt = lookup.get().get(NullSafety.nonNull(NorthstarCuriosCompatEnchantments.SPACE_WALK));
        if (holderOpt.isEmpty()) return 0;
        Holder<Enchantment> spaceWalk = NullSafety.nonNull(holderOpt.get());

        for (EquipmentSlot slot : NorthstarCuriosCompatConfig.allowedEquipmentSlots()) {
            ItemStack stack = player.getItemBySlot(NullSafety.nonNull(slot));
            if (stack.isEmpty()) continue;
            int level = stack.getOrDefault(NullSafety.enchantmentsComponent(), NullSafety.emptyEnchantments()).getLevel(spaceWalk);
            if (level > 0) return level;
        }
        return 0;
    }
}
