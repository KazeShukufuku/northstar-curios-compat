package com.createdelight.compat.northstarcurios.event;

import com.createdelight.compat.northstarcurios.NorthstarCuriosCompatMod;
import com.createdelight.compat.northstarcurios.config.NorthstarCuriosCompatConfig;
import com.createdelight.compat.northstarcurios.registry.NorthstarCuriosCompatEnchantments;
import com.createdelight.compat.northstarcurios.util.NullSafety;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Applies config-driven changes to the Space Walk enchantment at data-pack load time:
 *
 * <ul>
 *   <li>{@code allowedSlots} — rebuilds {@code supportedItems} and {@code slots} on the
 *       enchantment definition from the slot-specific enchantable item tags, so only items worn
 *       in the allowed slots can receive or benefit from the enchantment.</li>
 *   <li>{@code discoverable} — if {@code false}, removes the enchantment from the
 *       {@code minecraft:in_enchanting_table} tag and sets {@code primaryItems} to empty so the
 *       enchanting table never offers it. If {@code true}, clears {@code primaryItems} so it falls
 *       back to {@code supportedItems}.</li>
 *   <li>{@code tradeable} — if {@code false}, removes the enchantment from
 *       {@code minecraft:tradeable}.</li>
 * </ul>
 *
 * <p>All changes require a game restart to take effect.</p>
 */
@EventBusSubscriber(modid = NorthstarCuriosCompatMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class SpaceWalkEnchantmentTagHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpaceWalkEnchantmentTagHandler.class);

    private static final TagKey<Enchantment> IN_ENCHANTING_TABLE = NullSafety.enchantmentTag("minecraft", "in_enchanting_table");
    private static final TagKey<Enchantment> TRADEABLE             = NullSafety.enchantmentTag("minecraft", "tradeable");

    /** Maps slot config name → the corresponding {@code #minecraft:enchantable/xxx_armor} item tag. */
    private static final Map<String, TagKey<Item>> SLOT_TO_ENCHANTABLE_TAG = Map.of(
            "feet",  NullSafety.itemTag("minecraft", "enchantable/foot_armor"),
            "legs",  NullSafety.itemTag("minecraft", "enchantable/leg_armor"),
            "chest", NullSafety.itemTag("minecraft", "enchantable/chest_armor"),
            "head",  NullSafety.itemTag("minecraft", "enchantable/head_armor"));

    private SpaceWalkEnchantmentTagHandler() {
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        RegistryAccess registryAccess = event.getRegistryAccess();

        registryAccess.lookup(NullSafety.nonNull(Registries.ENCHANTMENT)).ifPresent(enchLookup ->
                enchLookup.get(NullSafety.nonNull(NorthstarCuriosCompatEnchantments.SPACE_WALK)).ifPresent(holder -> {
                    // Always update supportedItems / slots / primaryItems from config
                    updateEnchantmentDefinition(holder, registryAccess);

                    // Remove from enchantment tags when config says disabled
                    boolean discoverable = NorthstarCuriosCompatConfig.discoverable();
                    boolean tradeable    = NorthstarCuriosCompatConfig.tradeable();
                    if (!discoverable || !tradeable) {
                        Set<TagKey<Enchantment>> tags = holder.tags()
                                .collect(Collectors.toCollection(HashSet::new));
                        boolean changed = false;
                        if (!discoverable) changed |= tags.remove(IN_ENCHANTING_TABLE);
                        if (!tradeable)    changed |= tags.remove(TRADEABLE);
                        if (changed) bindTags(holder, tags);
                    }
                })
        );
    }

    // -------------------------------------------------------------------------
    // Enchantment definition patching
    // -------------------------------------------------------------------------

    private static void updateEnchantmentDefinition(Holder.Reference<Enchantment> holder,
                                                     RegistryAccess registryAccess) {
        List<? extends String> slotNames = NorthstarCuriosCompatConfig.COMMON.allowedSlots.get();

        // Collect items from each allowed slot's enchantable tag (dedup via LinkedHashSet)
        Set<Holder<Item>> allowedItemSet = new LinkedHashSet<>();
        registryAccess.lookup(NullSafety.nonNull(Registries.ITEM)).ifPresent(itemLookup -> {
            for (String slotName : slotNames) {
                TagKey<Item> tag = SLOT_TO_ENCHANTABLE_TAG.get(slotName.toLowerCase(Locale.ROOT));
                if (tag != null) {
                    itemLookup.get(tag).ifPresent(hs -> hs.forEach(allowedItemSet::add));
                }
            }
        });

        if (allowedItemSet.isEmpty()) {
            LOGGER.warn("[NorthstarCuriosCompat] allowedSlots yields no items — skipping supportedItems patch.");
            return;
        }

        HolderSet<Item> newSupportedItems = HolderSet.direct(new ArrayList<>(allowedItemSet));
        List<EquipmentSlot> newSlots = Arrays.asList(NorthstarCuriosCompatConfig.allowedEquipmentSlots());

        // primaryItems: Optional.empty() when discoverable (falls back to supportedItems),
        // Optional.of(emptySet) when not discoverable (nothing triggers table offer).
        boolean discoverable = NorthstarCuriosCompatConfig.discoverable();
        Optional<?> newPrimaryItems = discoverable
                ? Optional.empty()
                : Optional.of(NullSafety.nonNull(HolderSet.<Item>empty()));

        Enchantment enchantment = holder.value();

        // In MC 1.21.1 the definition data may live inside a nested "definition" field,
        // or directly as fields on Enchantment itself — try both.
        Object defTarget = enchantment;
        Field defField = findField(Enchantment.class, "definition");
        if (defField != null) {
            try {
                defField.setAccessible(true);
                Object def = defField.get(enchantment);
                if (def != null) defTarget = def;
            } catch (ReflectiveOperationException ignored) {}
        }

        setFieldReflectively(defTarget, "supportedItems", newSupportedItems);
        setFieldReflectively(defTarget, "slots", newSlots);
        setFieldReflectively(defTarget, "primaryItems", newPrimaryItems);
    }

    private static void setFieldReflectively(Object target, String fieldName, Object value) {
        Field field = findField(target.getClass(), fieldName);
        if (field == null) {
            LOGGER.warn("[NorthstarCuriosCompat] Cannot find field '{}' on {} — patch will not take effect.",
                    fieldName, target.getClass().getName());
            return;
        }
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("[NorthstarCuriosCompat] Failed to patch Space Walk field '{}': {}", fieldName, e.getMessage());
        }
    }

    /** Searches {@code cls} and its superclasses for a field with the given name. */
    private static Field findField(Class<?> cls, String name) {
        for (Class<?> c = cls; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                if (f.getName().equals(name)) return f;
            }
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Tag binding
    // -------------------------------------------------------------------------

    private static <T> void bindTags(Holder<T> holder, Collection<TagKey<T>> tags) {
        try {
            Method method = holder.getClass().getMethod("bindTags", Collection.class);
            method.invoke(holder, tags);
        } catch (NoSuchMethodException e) {
            Method method = findBindTags(holder.getClass());
            if (method != null) {
                try {
                    method.setAccessible(true);
                    method.invoke(holder, tags);
                } catch (ReflectiveOperationException ex) {
                    LOGGER.warn("[NorthstarCuriosCompat] Failed to update Space Walk enchantment tags", ex);
                }
            } else {
                LOGGER.warn("[NorthstarCuriosCompat] bindTags not found on {}; "
                        + "discoverable/tradeable config will not take effect.", holder.getClass().getName());
            }
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("[NorthstarCuriosCompat] Failed to update Space Walk enchantment tags", e);
        }
    }

    private static Method findBindTags(Class<?> cls) {
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals("bindTags") && m.getParameterCount() == 1
                        && Collection.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    return m;
                }
            }
        }
        return null;
    }
}
