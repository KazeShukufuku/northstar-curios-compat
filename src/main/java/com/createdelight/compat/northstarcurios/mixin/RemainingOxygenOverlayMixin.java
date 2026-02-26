package com.createdelight.compat.northstarcurios.mixin;

import com.lightning.northstar.client.renderer.RemainingOxygenOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = RemainingOxygenOverlay.class, remap = false)
public class RemainingOxygenOverlayMixin {

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"),
            remap = false
    )
    private int northstarCuriosCompat$fixOxygenOverlayOffByOne(int left, int right) {
        return Math.max(left, right + 1);
    }
}
