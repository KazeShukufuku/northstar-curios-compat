package com.createdelight.compat.northstarcurios.util;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.lightning.northstar.content.NorthstarDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

public final class NullSafety {

    private NullSafety() {
    }

    @Nonnull
    public static <T> T nonNull(T value) {
        return Objects.requireNonNull(value);
    }

    @Nonnull
    @SuppressWarnings("all")
    public static TagKey<Item> itemTag(String namespace, String path) {
        return Objects.requireNonNull(
                TagKey.create(
                        Objects.requireNonNull(Registries.ITEM),
                        Objects.requireNonNull(ResourceLocation.fromNamespaceAndPath(
                                Objects.requireNonNull(namespace),
                                Objects.requireNonNull(path)))
                )
        );
    }

    @Nonnull
    @SuppressWarnings("all")
    public static TagKey<Item> northstarItemTag(String path) {
        return itemTag("northstar", path);
    }

    @Nonnull
    @SuppressWarnings("all")
    public static TagKey<Enchantment> enchantmentTag(String namespace, String path) {
        return Objects.requireNonNull(
                TagKey.create(
                        Objects.requireNonNull(Registries.ENCHANTMENT),
                        Objects.requireNonNull(ResourceLocation.fromNamespaceAndPath(
                                Objects.requireNonNull(namespace),
                                Objects.requireNonNull(path)))
                )
        );
    }

    // -------------------------------------------------------------------------
    // Enchantment DataComponent helpers
    // DataComponents.ENCHANTMENTS and ItemEnchantments.EMPTY have no @Nonnull;
    // centralise them here to avoid per-call nonNull() noise.
    // -------------------------------------------------------------------------

    @Nonnull
    @SuppressWarnings("all")
    public static DataComponentType<ItemEnchantments> enchantmentsComponent() {
        return Objects.requireNonNull(DataComponents.ENCHANTMENTS);
    }

    @Nonnull
    @SuppressWarnings("all")
    public static ItemEnchantments emptyEnchantments() {
        return Objects.requireNonNull(ItemEnchantments.EMPTY);
    }

    // -------------------------------------------------------------------------
    // Oxygen DataComponent helpers
    // NorthstarDataComponents.OXYGEN has no @Nonnull annotation; wrap once here
    // so all callers get a @Nonnull DataComponentType<Integer>.
    // -------------------------------------------------------------------------

    @Nonnull
    public static DataComponentType<Integer> oxygenType() {
        return Objects.requireNonNull(NorthstarDataComponents.OXYGEN);
    }

    public static int getOxygen(@Nonnull ItemStack stack) {
        return stack.getOrDefault(oxygenType(), 0);
    }

    public static boolean hasOxygen(@Nonnull ItemStack stack) {
        return stack.has(oxygenType());
    }

    public static void setOxygen(@Nonnull ItemStack stack, int value) {
        stack.set(oxygenType(), value);
    }
}
