package com.createdelight.compat.northstarcurios.util;

import java.util.Objects;

import javax.annotation.Nonnull;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class NullSafety {

    private NullSafety() {
    }

    @Nonnull
    public static <T> T nonNull(T value) {
        return Objects.requireNonNull(value);
    }

    @Nonnull
    @SuppressWarnings("all")
    public static TagKey<Item> northstarItemTag(String path) {
        String safePath = Objects.requireNonNull(path);
        return Objects.requireNonNull(
                TagKey.create(
                        Objects.requireNonNull(Registries.ITEM),
                        Objects.requireNonNull(ResourceLocation.fromNamespaceAndPath("northstar", safePath))
                )
        );
    }
}