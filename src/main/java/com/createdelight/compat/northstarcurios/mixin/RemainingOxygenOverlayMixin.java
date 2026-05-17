package com.createdelight.compat.northstarcurios.mixin;

import com.lightning.northstar.client.renderer.RemainingOxygenOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(value = RemainingOxygenOverlay.class, remap = false)
public class RemainingOxygenOverlayMixin {

    private static int northstarCuriosCompat$lastDisplayedOxygen = -1;
    private static long northstarCuriosCompat$lastDisplayTick = Long.MIN_VALUE;
    private static UUID northstarCuriosCompat$lastPlayerId;

    /**
     * Northstar Redux 1.21.1 uses DataComponents (ItemStack#get) instead of CompoundTag#getInt.
     * Redirect the oxygen DataComponent read to stabilize the displayed value and prevent
     * flicker when the oxygen counter briefly ticks up by 1 within a 2-tick window.
     *
     * Off-by-one fix is no longer needed here: the 1.21.1 source already applies
     * {@code Math.max(0, remainingTime - 1)} before display.
     */
    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;get(Lnet/minecraft/core/component/DataComponentType;)Ljava/lang/Object;"),
            remap = false
    )
    @Nullable
    @SuppressWarnings({"unchecked", "null"})
    private <T> T northstarCuriosCompat$stabilizeArmorOverlayOxygen(ItemStack tank, DataComponentType<T> type) {
        T rawValue = tank.get(type);
        if (!(rawValue instanceof Integer rawInt)) return rawValue;

        int observed = rawInt;
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;

        if (player == null || player.level() == null) {
            northstarCuriosCompat$lastDisplayedOxygen = observed;
            northstarCuriosCompat$lastDisplayTick = Long.MIN_VALUE;
            northstarCuriosCompat$lastPlayerId = null;
            return rawValue;
        }

        UUID playerId = player.getUUID();
        long gameTime = player.level().getGameTime();

        if (northstarCuriosCompat$lastPlayerId == null || !northstarCuriosCompat$lastPlayerId.equals(playerId)
                || gameTime < northstarCuriosCompat$lastDisplayTick) {
            northstarCuriosCompat$lastDisplayedOxygen = observed;
            northstarCuriosCompat$lastDisplayTick = gameTime;
            northstarCuriosCompat$lastPlayerId = playerId;
            return rawValue;
        }

        if (northstarCuriosCompat$lastDisplayedOxygen < 0) {
            northstarCuriosCompat$lastDisplayedOxygen = observed;
            northstarCuriosCompat$lastDisplayTick = gameTime;
            northstarCuriosCompat$lastPlayerId = playerId;
            return rawValue;
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
        //noinspection unchecked
        return (T) (Integer) observed;
    }

    /**
     * Northstar Redux 1.21.1 applies {@code Math.max(0, remainingTime - 1)} before formatting
     * the display string. This subtracts one from the apparent remaining time. Undo the -1 so
     * the overlay shows the time that corresponds to the actual current oxygen level.
     */
    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"),
            remap = false
    )
    private int northstarCuriosCompat$fixOxygenOverlayOffByOne(int left, int right) {
        // Original call: Math.max(0, remainingTime - 1)  →  left=0, right=remainingTime-1
        // Return Math.max(0, remainingTime) instead.
        return Math.max(left, right + 1);
    }
}
