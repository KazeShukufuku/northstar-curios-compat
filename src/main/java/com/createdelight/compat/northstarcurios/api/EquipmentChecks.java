package com.createdelight.compat.northstarcurios.api;

import com.createdelight.compat.northstarcurios.util.NullSafety;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

public final class EquipmentChecks {
    private static final TagKey<Item> OXYGEN_SOURCE = NullSafety.northstarItemTag("oxygen_sources");
    private static final TagKey<Item> OXYGEN_SOURCE_2 = NullSafety.northstarItemTag("oxygen_sources_2");
    private static final ThreadLocal<Boolean> CHECKING = ThreadLocal.withInitial(() -> false);

    private EquipmentChecks() {
    }

    public static int query(String type, ItemStack stack, @Nullable LivingEntity entity, String slot) {
        if (stack.isEmpty() || CHECKING.get()) {
            return 0;
        }
        CHECKING.set(true);
        try {
            EquipmentCheckEvent event = new EquipmentCheckEvent(type, stack, entity, slot);
            MinecraftForge.EVENT_BUS.post(event);
            return event.getValue();
        } finally {
            CHECKING.remove();
        }
    }

    public static boolean isTaggedOxygenSource(ItemStack stack) {
        return stack.is(OXYGEN_SOURCE) || stack.is(OXYGEN_SOURCE_2);
    }

    public static int customOxygenCapacity(ItemStack stack) {
        return query("oxygen_source", stack, null, "item");
    }

    public static boolean isOxygenSource(ItemStack stack) {
        return isTaggedOxygenSource(stack) || customOxygenCapacity(stack) > 0;
    }

    public static String armorSlot(LivingEntity entity, ItemStack stack) {
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.getType() == EquipmentSlot.Type.ARMOR && entity.getItemBySlot(slot) == stack) {
                return slot.getName();
            }
        }
        return "unknown";
    }
}
