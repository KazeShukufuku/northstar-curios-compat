package com.createdelight.compat.northstarcurios.config;

import net.minecraft.world.entity.EquipmentSlot;
import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import com.createdelight.compat.northstarcurios.util.NullSafety;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class NorthstarCuriosCompatConfig {

    public static final ModConfigSpec SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ModConfigSpec> pair = new ModConfigSpec.Builder().configure(Common::new);
        COMMON = pair.getLeft();
        SPEC = pair.getRight();
    }

    private NorthstarCuriosCompatConfig() {
    }

    public static double durabilityPerSecond() {
        return COMMON.durabilityPerSecond.get();
    }

    public static boolean discoverable() {
        return COMMON.discoverable.get();
    }

    public static boolean tradeable() {
        return COMMON.tradeable.get();
    }

    public static EquipmentSlot[] allowedEquipmentSlots() {
        List<? extends String> names = COMMON.allowedSlots.get();
        List<EquipmentSlot> result = new ArrayList<>(names.size());
        for (String name : names) {
            switch (name.toLowerCase(Locale.ROOT)) {
                case "feet"  -> result.add(EquipmentSlot.FEET);
                case "legs"  -> result.add(EquipmentSlot.LEGS);
                case "chest" -> result.add(EquipmentSlot.CHEST);
                case "head"  -> result.add(EquipmentSlot.HEAD);
            }
        }
        return result.toArray(new EquipmentSlot[0]);
    }

    public static final class Common {
        public final ModConfigSpec.ConfigValue<List<? extends String>> allowedSlots;
        public final ModConfigSpec.BooleanValue discoverable;
        public final ModConfigSpec.BooleanValue tradeable;
        public final ModConfigSpec.DoubleValue durabilityPerSecond;

        Common(ModConfigSpec.Builder builder) {
            builder.push("space_walk");
            allowedSlots = builder
                    .comment("Equipment slots that can receive the Space Walk enchantment.",
                            "Valid values: feet, legs, chest, head")
                    .translation("northstar_curios_compat.config.space_walk.allowedSlots")
                    .defineListAllowEmpty("allowedSlots",
                            NullSafety.nonNull(List.of("feet")),
                            () -> "feet",
                            s -> s instanceof String str && isValidSlot(str));
            discoverable = builder
                    .comment("Whether the Space Walk enchantment can appear in the enchanting table.")
                    .translation("northstar_curios_compat.config.space_walk.discoverable")
                    .define("discoverable", false);
            tradeable = builder
                    .comment("Whether the Space Walk enchantment can be obtained through villager trades.")
                    .translation("northstar_curios_compat.config.space_walk.tradeable")
                    .define("tradeable", false);
            durabilityPerSecond = builder
                    .comment("Durability consumed per second while Space Walk actively enforces earth-like gravity.",
                            "Set to 0 to disable durability consumption.")
                    .translation("northstar_curios_compat.config.space_walk.durabilityPerSecond")
                    .defineInRange("durabilityPerSecond", 1.0D, 0.0D, 1000.0D);
            builder.pop();
        }
    }

    private static boolean isValidSlot(String s) {
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "feet", "legs", "chest", "head" -> true;
            default -> false;
        };
    }
}
