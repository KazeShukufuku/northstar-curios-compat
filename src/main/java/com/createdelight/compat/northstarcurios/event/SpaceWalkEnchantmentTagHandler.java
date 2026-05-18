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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.TagsUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Applies config-driven changes to Space Walk at data-pack load time.
 *
 * <ul>
 *   <li>{@code allowedSlots} updates the backing item tag used by Space Walk's
 *       {@code supported_items} field, so vanilla/NeoForge enchantment checks see
 *       the configured armor slots without a mixin.</li>
 *   <li>{@code discoverable} removes the enchantment from
 *       {@code minecraft:in_enchanting_table} when disabled.</li>
 *   <li>{@code tradeable} removes the enchantment from {@code minecraft:tradeable}
 *       when disabled.</li>
 * </ul>
 */
@EventBusSubscriber(modid = NorthstarCuriosCompatMod.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class SpaceWalkEnchantmentTagHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SpaceWalkEnchantmentTagHandler.class);

    private static final TagKey<Enchantment> IN_ENCHANTING_TABLE =
            NullSafety.enchantmentTag("minecraft", "in_enchanting_table");
    private static final TagKey<Enchantment> TRADEABLE =
            NullSafety.enchantmentTag("minecraft", "tradeable");
    private static final TagKey<Item> SPACE_WALK_SUPPORTED_ITEMS =
            NullSafety.itemTag(NorthstarCuriosCompatMod.MOD_ID, "enchantable/space_walk");

    /** Maps slot config name to the corresponding {@code #minecraft:enchantable/xxx_armor} item tag. */
    private static final Map<String, TagKey<Item>> SLOT_TO_ENCHANTABLE_TAG = Map.of(
            "feet", NullSafety.itemTag("minecraft", "enchantable/foot_armor"),
            "legs", NullSafety.itemTag("minecraft", "enchantable/leg_armor"),
            "chest", NullSafety.itemTag("minecraft", "enchantable/chest_armor"),
            "head", NullSafety.itemTag("minecraft", "enchantable/head_armor"));

    private SpaceWalkEnchantmentTagHandler() {
    }

    @SubscribeEvent
    public static void onTagsUpdated(TagsUpdatedEvent event) {
        RegistryAccess registryAccess = event.getRegistryAccess();
        updateSupportedItemTag(registryAccess);

        registryAccess.lookup(NullSafety.nonNull(Registries.ENCHANTMENT)).ifPresent(enchLookup ->
                enchLookup.get(NullSafety.nonNull(NorthstarCuriosCompatEnchantments.SPACE_WALK)).ifPresent(holder -> {
                    boolean discoverable = NorthstarCuriosCompatConfig.discoverable();
                    boolean tradeable = NorthstarCuriosCompatConfig.tradeable();
                    if (!discoverable || !tradeable) {
                        Set<TagKey<Enchantment>> tags = holder.tags()
                                .collect(Collectors.toCollection(HashSet::new));
                        boolean changed = false;
                        if (!discoverable) changed |= tags.remove(IN_ENCHANTING_TABLE);
                        if (!tradeable) changed |= tags.remove(TRADEABLE);
                        if (changed) bindTags(holder, tags, "Space Walk enchantment");
                    }
                })
        );
    }

    private static void updateSupportedItemTag(RegistryAccess registryAccess) {
        registryAccess.lookup(NullSafety.nonNull(Registries.ITEM)).ifPresent(itemLookup -> {
            Set<TagKey<Item>> allowedSlotTags = configuredSlotTags();
            List<Holder<Item>> supportedItems = new ArrayList<>();

            itemLookup.listElements().forEach(holder -> {
                boolean supported = allowedSlotTags.stream().anyMatch(holder::is);
                Set<TagKey<Item>> tags = holder.tags().collect(Collectors.toCollection(HashSet::new));
                boolean changed = tags.remove(SPACE_WALK_SUPPORTED_ITEMS);

                if (supported) {
                    supportedItems.add(holder);
                    changed |= tags.add(SPACE_WALK_SUPPORTED_ITEMS);
                }

                if (changed) {
                    bindTags(holder, tags, "Space Walk supported item");
                }
            });

            itemLookup.listTags()
                    .filter(tag -> SPACE_WALK_SUPPORTED_ITEMS.equals(tag.key()))
                    .findFirst()
                    .ifPresent(tag -> bindNamedTag(tag, supportedItems));
        });
    }

    private static Set<TagKey<Item>> configuredSlotTags() {
        Set<TagKey<Item>> tags = new HashSet<>();
        for (String slotName : NorthstarCuriosCompatConfig.COMMON.allowedSlots.get()) {
            TagKey<Item> tag = SLOT_TO_ENCHANTABLE_TAG.get(slotName.toLowerCase(Locale.ROOT));
            if (tag != null) {
                tags.add(tag);
            }
        }
        return tags;
    }

    // -------------------------------------------------------------------------
    // Tag binding
    // -------------------------------------------------------------------------

    private static <T> void bindTags(Holder<T> holder, Collection<TagKey<T>> tags, String context) {
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
                    LOGGER.warn("[NorthstarCuriosCompat] Failed to update {} tags", context, ex);
                }
            } else {
                LOGGER.warn("[NorthstarCuriosCompat] bindTags not found on {}; {} tag config will not take effect.",
                        holder.getClass().getName(), context);
            }
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("[NorthstarCuriosCompat] Failed to update {} tags", context, e);
        }
    }

    private static <T> void bindNamedTag(HolderSet.Named<T> tag, List<Holder<T>> contents) {
        Method method = findBindNamedTag(tag.getClass());
        if (method == null) {
            LOGGER.warn("[NorthstarCuriosCompat] bind not found on {}; Space Walk supported item tag contents may be stale.",
                    tag.getClass().getName());
            return;
        }

        try {
            method.setAccessible(true);
            method.invoke(tag, contents);
        } catch (ReflectiveOperationException e) {
            LOGGER.warn("[NorthstarCuriosCompat] Failed to update Space Walk supported item tag contents", e);
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

    private static Method findBindNamedTag(Class<?> cls) {
        for (Class<?> c = cls; c != null; c = c.getSuperclass()) {
            for (Method m : c.getDeclaredMethods()) {
                if (m.getName().equals("bind") && m.getParameterCount() == 1
                        && List.class.isAssignableFrom(m.getParameterTypes()[0])) {
                    return m;
                }
            }
        }
        return null;
    }
}
