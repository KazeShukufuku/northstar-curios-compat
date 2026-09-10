package com.createdelight.compat.northstarcurios.compat.kubejs;

import com.createdelight.compat.northstarcurios.api.EquipmentCheckEvent;
import dev.latvian.mods.kubejs.event.EventJS;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public final class EquipmentCheckEventJS extends EventJS {
    private final EquipmentCheckEvent event;

    public EquipmentCheckEventJS(EquipmentCheckEvent event) {
        this.event = event;
    }

    public String getType() {
        return event.getType();
    }

    public ItemStack getStack() {
        return event.getStack();
    }

    @Nullable
    public LivingEntity getEntity() {
        return event.getEntity();
    }

    @Nullable
    public Player getPlayer() {
        return event.getPlayer();
    }

    public String getSlot() {
        return event.getSlot();
    }

    public int getValue() {
        return event.getValue();
    }

    public void grant(int value) {
        event.grant(value);
    }
}
