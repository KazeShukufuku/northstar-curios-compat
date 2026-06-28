package com.createdelight.compat.northstarcurios.mixin;

import com.createdelight.compat.northstarcurios.util.NullSafety;
import com.lightning.northstar.compat.jei.NorthstarJEI;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

@Mixin(value = NorthstarJEI.class, remap = false)
public class NorthstarJEIMixin {

    private static final TagKey<Item> OXYGEN_SOURCE_TAG = NullSafety.northstarItemTag("oxygen_sources");
    private static final TagKey<Item> OXYGEN_SOURCE_TAG_T2 = NullSafety.northstarItemTag("oxygen_sources_t2");

    @Redirect(
            method = "registerRecipes",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/core/Registry;getTagOrEmpty(Lnet/minecraft/tags/TagKey;)Ljava/lang/Iterable;",
                    remap = false
            ),
            remap = false
    )
    private Iterable<Holder<Item>> northstarCuriosCompat$includeT2OxygenSourceTag(
            Registry<Item> registry,
            TagKey<Item> tag
    ) {
        if (!OXYGEN_SOURCE_TAG.equals(tag)) {
            return registry.getTagOrEmpty(tag);
        }

        List<Holder<Item>> holders = new ArrayList<>();
        Set<Item> seenItems = Collections.newSetFromMap(new IdentityHashMap<>());

        northstarCuriosCompat$addUniqueTagEntries(registry, tag, holders, seenItems);
        northstarCuriosCompat$addUniqueTagEntries(registry, NullSafety.nonNull(OXYGEN_SOURCE_TAG_T2), holders, seenItems);

        return holders;
    }

    private static void northstarCuriosCompat$addUniqueTagEntries(
            Registry<Item> registry,
            TagKey<Item> tag,
            List<Holder<Item>> holders,
            Set<Item> seenItems
    ) {
        for (Holder<Item> holder : registry.getTagOrEmpty(tag)) {
            if (seenItems.add(holder.value())) {
                holders.add(holder);
            }
        }
    }
}
