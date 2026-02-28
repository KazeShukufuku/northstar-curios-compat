package com.createdelight.compat.northstarcurios.mixin;

import com.lightning.northstar.client.renderer.RemainingOxygenOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(value = RemainingOxygenOverlay.class, remap = false)
public class RemainingOxygenOverlayMixin {

    private static int northstarCuriosCompat$lastDisplayedOxygen = -1;
    private static long northstarCuriosCompat$lastDisplayTick = Long.MIN_VALUE;
    private static UUID northstarCuriosCompat$lastPlayerId;

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;m_128451_(Ljava/lang/String;)I"),
            remap = false
    )
    private int northstarCuriosCompat$stabilizeArmorOverlayOxygen(CompoundTag tag, String key) {
        int observed = tag.getInt(key);
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null || player.level() == null) {
            northstarCuriosCompat$lastDisplayedOxygen = observed;
            northstarCuriosCompat$lastDisplayTick = Long.MIN_VALUE;
            northstarCuriosCompat$lastPlayerId = null;
            return observed;
        }

        UUID playerId = player.getUUID();
        long gameTime = player.level().getGameTime();

        if (northstarCuriosCompat$lastPlayerId == null || !northstarCuriosCompat$lastPlayerId.equals(playerId)
                || gameTime < northstarCuriosCompat$lastDisplayTick) {
            northstarCuriosCompat$lastDisplayedOxygen = observed;
            northstarCuriosCompat$lastDisplayTick = gameTime;
            northstarCuriosCompat$lastPlayerId = playerId;
            return observed;
        }

        if (northstarCuriosCompat$lastDisplayedOxygen < 0) {
            northstarCuriosCompat$lastDisplayedOxygen = observed;
            northstarCuriosCompat$lastDisplayTick = gameTime;
            northstarCuriosCompat$lastPlayerId = playerId;
            return observed;
        }

        long tickDelta = gameTime - northstarCuriosCompat$lastDisplayTick;

        if (tickDelta <= 2L
                && observed > northstarCuriosCompat$lastDisplayedOxygen
                && observed <= northstarCuriosCompat$lastDisplayedOxygen + 1) {
            observed = northstarCuriosCompat$lastDisplayedOxygen;
        } else {
            northstarCuriosCompat$lastDisplayedOxygen = observed;
        }

        northstarCuriosCompat$lastDisplayTick = gameTime;
        northstarCuriosCompat$lastPlayerId = playerId;
        return observed;
    }

    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"),
            remap = false
    )
    private int northstarCuriosCompat$fixOxygenOverlayOffByOne(int left, int right) {
        return Math.max(left, right + 1);
    }
}
