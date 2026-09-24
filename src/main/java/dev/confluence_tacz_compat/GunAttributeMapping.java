package dev.confluence_tacz_compat;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves the configured source-to-target attribute replacements from the live attribute registry.
 */
public final class GunAttributeMapping {
    private final Map<ResourceLocation, Holder<Attribute>> replacements;

    private GunAttributeMapping(Map<ResourceLocation, Holder<Attribute>> replacements) {
        this.replacements = Map.copyOf(replacements);
    }

    /** Returns the configured mappings, skipping attribute ids that are not registered. */
    public static @Nullable GunAttributeMapping get() {
        if (!CompatConfig.attributeReplacementEnabled()) return null;
        Map<ResourceLocation, Holder<Attribute>> replacements = new HashMap<>();
        for (String entry : CompatConfig.attributeMappings()) {
            int separator = entry.indexOf('=');
            if (separator <= 0 || separator != entry.lastIndexOf('=')) continue;

            ResourceLocation sourceId = ResourceLocation.tryParse(entry.substring(0, separator).trim());
            ResourceLocation targetId = ResourceLocation.tryParse(entry.substring(separator + 1).trim());
            if (sourceId == null || targetId == null || !CompatConfig.isLegacyMappingEnabled(sourceId.toString())) continue;

            Holder<Attribute> source = resolve(sourceId);
            Holder<Attribute> target = resolve(targetId);
            if (source == null || target == null || source.value() == target.value()) continue;
            replacements.put(sourceId, target);
        }
        return replacements.isEmpty() ? null : new GunAttributeMapping(replacements);
    }

    private static @Nullable Holder<Attribute> resolve(ResourceLocation id) {
        return BuiltInRegistries.ATTRIBUTE.getHolder(id).orElse(null);
    }

    /** Returns the replacement holder, or null when the source attribute is not configured. */
    public @Nullable Holder<Attribute> map(Holder<Attribute> source) {
        ResourceLocation sourceId = BuiltInRegistries.ATTRIBUTE.getKey(source.value());
        return sourceId == null ? null : replacements.get(sourceId);
    }
}
