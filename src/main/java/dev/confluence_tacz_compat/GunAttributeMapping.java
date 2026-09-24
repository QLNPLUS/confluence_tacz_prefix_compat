package dev.confluence_tacz_compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * Resolves the configured source-to-target attribute replacements from the Forge attribute registry.
 */
public final class GunAttributeMapping {
    private final Map<ResourceLocation, Attribute> replacements;

    private GunAttributeMapping(Map<ResourceLocation, Attribute> replacements) {
        this.replacements = Map.copyOf(replacements);
    }

    /** Returns the configured mappings, skipping attribute ids that are not registered. */
    public static @Nullable GunAttributeMapping get() {
        if (!CompatConfig.attributeReplacementEnabled()) return null;
        Map<ResourceLocation, Attribute> replacements = new HashMap<>();
        for (String entry : CompatConfig.attributeMappings()) {
            int separator = entry.indexOf('=');
            if (separator <= 0 || separator != entry.lastIndexOf('=')) continue;

            ResourceLocation sourceId = ResourceLocation.tryParse(entry.substring(0, separator).trim());
            ResourceLocation targetId = ResourceLocation.tryParse(entry.substring(separator + 1).trim());
            if (sourceId == null || targetId == null || !CompatConfig.isLegacyMappingEnabled(sourceId.toString())) continue;

            Attribute source = ForgeRegistries.ATTRIBUTES.getValue(sourceId);
            Attribute target = ForgeRegistries.ATTRIBUTES.getValue(targetId);
            if (source == null || target == null || source == target) continue;
            replacements.put(sourceId, target);
        }
        return replacements.isEmpty() ? null : new GunAttributeMapping(replacements);
    }

    /** Returns the replacement attribute, or null when the source attribute is not configured. */
    public @Nullable Attribute map(Attribute source) {
        ResourceLocation sourceId = ForgeRegistries.ATTRIBUTES.getKey(source);
        return sourceId == null ? null : replacements.get(sourceId);
    }
}
