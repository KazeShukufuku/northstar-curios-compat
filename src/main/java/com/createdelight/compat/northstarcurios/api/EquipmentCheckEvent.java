package com.createdelight.compat.northstarcurios.api;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

public final class EquipmentCheckEvent extends Event {
    private final String type;
    private final ItemStack stack;
    private final LivingEntity entity;
    private final String slot;
    private int value;

    public EquipmentCheckEvent(String type, ItemStack stack, @Nullable LivingEntity entity, String slot) {
        this.type = type;
        this.stack = stack;
        this.entity = entity;
        this.slot = slot;
    }

    public String getType() {
        return type;
    }

    public ItemStack getStack() {
        return stack;
    }

    @Nullable
    public LivingEntity getEntity() {
        return entity;
    }

    @Nullable
    public Player getPlayer() {
        return entity instanceof Player player ? player : null;
    }

    public String getSlot() {
        return slot;
    }

    public int getValue() {
        return value;
    }

    public void grant(int value) {
        int maximum = "oxygen_source".equals(type) ? Integer.MAX_VALUE
                : ("oxygen_sealing".equals(type) || "full_seal".equals(type)) ? 1 : 4;
        this.value = Math.max(this.value, Math.min(maximum, value));
    }
}
