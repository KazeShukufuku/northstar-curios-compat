package com.createdelight.compat.northstarcurios.config;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public final class NorthstarCuriosCompatConfig {

    public static final ForgeConfigSpec SPEC;
    public static final Common COMMON;

    static {
        Pair<Common, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(Common::new);
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

    public static Set<EquipmentSlot> allowedEquipmentSlots() {
        return COMMON.allowedSlots.get().stream()
                .map(s -> switch (s.toLowerCase()) {
                    case "feet"  -> EquipmentSlot.FEET;
                    case "legs"  -> EquipmentSlot.LEGS;
                    case "chest" -> EquipmentSlot.CHEST;
                    case "head"  -> EquipmentSlot.HEAD;
                    default      -> null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static final class Common {
        public final ForgeConfigSpec.ConfigValue<List<? extends String>> allowedSlots;
        public final ForgeConfigSpec.BooleanValue discoverable;
        public final ForgeConfigSpec.BooleanValue tradeable;
        public final ForgeConfigSpec.DoubleValue durabilityPerSecond;

        private static final List<String> VALID_SLOTS = List.of("feet", "legs", "chest", "head");

        Common(ForgeConfigSpec.Builder builder) {
            builder.push("space_walk");
            allowedSlots = builder
                    .comment("Equipment slots the Space Walk enchantment can be applied to via the enchanting table.",
                            "Valid values: feet, legs, chest, head")
                    .translation("northstar_curios_compat.config.space_walk.allowedSlots")
                    .defineListAllowEmpty("allowedSlots", List.of("feet"),
                            e -> e instanceof String s && VALID_SLOTS.contains(s.toLowerCase()));
            discoverable = builder
                    .comment("Whether Space Walk can appear in the enchanting table.")
                    .translation("northstar_curios_compat.config.space_walk.discoverable")
                    .define("discoverable", false);
            tradeable = builder
                    .comment("Whether Space Walk can be obtained through villager trades.")
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
}
