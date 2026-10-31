package dev.confluence_tacz_compat;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Applies attribute_display_style's configured name color to Confluence's plain-text crit row. */
@Mod.EventBusSubscriber(modid = ConfluenceTaczCompat.MOD_ID, value = Dist.CLIENT)
public final class CriticalChanceTooltipCompat {
    private static final ResourceLocation CRITICAL_CHANCE_ID =
            ResourceLocation.tryParse("confluence_magic_lib:generic.critical_chance");
    private static final Pattern PERCENTAGE_VALUE =
            Pattern.compile("([+\\-−])\\s*(\\d+(?:[.,]\\d+)?)\\s*%\\s*$");
    private static volatile DisplayConfigCache displayConfigCache;

    private CriticalChanceTooltipCompat() {}

    /**
     * Confluence can flatten the critical-chance row to literal text, so tooltip styling mods
     * cannot identify its attribute name. Keep the original row and its custom-font icons intact,
     * and color only the localized name using attribute_display_style's JSON configuration. This
     * applies to any item that contains the Confluence critical-chance row.
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (!CompatConfig.colorCriticalChanceName()) return;

        Attribute criticalChance = ForgeRegistries.ATTRIBUTES.getValue(CRITICAL_CHANCE_ID);
        if (criticalChance == null) return;

        Component name = Component.translatable(criticalChance.getDescriptionId());
        String localizedName = name.getString();
        if (localizedName.isBlank() || localizedName.equals(criticalChance.getDescriptionId())) return;

        List<Component> tooltip = event.getToolTip();
        for (int i = 0; i < tooltip.size(); i++) {
            Component line = tooltip.get(i);
            if (containsTranslatable(line)) continue;

            String text = line.getString().stripTrailing();
            if (!text.endsWith(localizedName) || !text.contains("%")) continue;

            String valueText = text.substring(0, text.length() - localizedName.length()).trim();
            Matcher valueMatcher = PERCENTAGE_VALUE.matcher(valueText);
            if (!valueMatcher.find()) continue;

            boolean positive = !valueMatcher.group(1).equals("-") && !valueMatcher.group(1).equals("−");
            DisplayColors colors = readDisplayColors();
            if (colors == null) continue;

            TextColor nameColor = colors.nameColor(positive);
            if (nameColor == null) continue;

            tooltip.set(i, colorAttributeName(line, localizedName,
                    text.length() - localizedName.length(), nameColor));
        }
    }

    private static Component colorAttributeName(Component line, String localizedName, int nameStart,
                                                TextColor nameColor) {
        List<StyledText> parts = new java.util.ArrayList<>();
        line.visit((style, text) -> {
            if (!text.isEmpty()) parts.add(new StyledText(style, text));
            return Optional.empty();
        }, line.getStyle());

        MutableComponent result = Component.empty().setStyle(line.getStyle());
        int offset = 0;
        int nameEnd = nameStart + localizedName.length();
        for (StyledText part : parts) {
            String segment = part.text();
            int end = offset + segment.length();

            append(result, segment, offset, Math.min(end, nameStart), offset, part.style());

            int coloredStart = Math.max(offset, nameStart);
            int coloredEnd = Math.min(end, nameEnd);
            if (coloredStart < coloredEnd) {
                append(result, segment, coloredStart, coloredEnd, offset,
                        part.style().withColor(nameColor));
            }

            append(result, segment, Math.max(offset, nameEnd), end, offset, part.style());
            offset = end;
        }
        return result;
    }

    private static void append(MutableComponent target, String text, int from, int to, int segmentStart,
                               Style style) {
        if (from < to) {
            target.append(Component.literal(text.substring(from - segmentStart, to - segmentStart))
                    .setStyle(style));
        }
    }

    private static boolean containsTranslatable(Component component) {
        if (component.getContents() instanceof TranslatableContents) {
            return true;
        }
        for (Component sibling : component.getSiblings()) {
            if (containsTranslatable(sibling)) return true;
        }
        return false;
    }

    private static DisplayColors readDisplayColors() {
        Path configPath = FMLPaths.CONFIGDIR.get().resolve("attribute_display_style.json");
        try {
            long modified = Files.getLastModifiedTime(configPath).toMillis();
            DisplayConfigCache cached = displayConfigCache;
            if (cached != null && cached.lastModified() == modified) return cached.colors();

            DisplayColors colors;
            try (Reader reader = Files.newBufferedReader(configPath, StandardCharsets.UTF_8)) {
                JsonElement element = JsonParser.parseReader(reader);
                if (!element.isJsonObject()) return null;

                JsonObject root = element.getAsJsonObject();
                boolean expandOnShift = root.has("expand_on_shift")
                        && root.get("expand_on_shift").getAsBoolean();
                colors = new DisplayColors(readNameColor(root, "positive_single"),
                        readNameColor(root, "negative_single"),
                        readNameColor(root, "positive_expanded"),
                        readNameColor(root, "negative_expanded"),
                        expandOnShift);
            }
            displayConfigCache = new DisplayConfigCache(modified, colors);
            return colors;
        } catch (Exception ignored) {
            displayConfigCache = null;
            return null;
        }
    }

    private static TextColor readNameColor(JsonObject root, String styleName) {
        JsonElement styleElement = root.get(styleName);
        if (styleElement == null || !styleElement.isJsonObject()) return null;

        JsonElement colorElement = styleElement.getAsJsonObject().get("name_color");
        if (colorElement == null || !colorElement.isJsonPrimitive()) return null;

        String hex = colorElement.getAsString().trim();
        if (hex.startsWith("#")) hex = hex.substring(1);
        if (hex.startsWith("0x") || hex.startsWith("0X")) hex = hex.substring(2);
        if (hex.length() == 8) hex = hex.substring(2);
        if (hex.length() != 6) return null;

        try {
            return TextColor.fromRgb(Integer.parseInt(hex, 16));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private record StyledText(Style style, String text) {}

    private record DisplayColors(TextColor positiveSingle, TextColor negativeSingle,
                                 TextColor positiveExpanded, TextColor negativeExpanded,
                                 boolean expandOnShift) {
        private TextColor nameColor(boolean positive) {
            if (expandOnShift && Screen.hasShiftDown()) {
                TextColor expanded = positive ? positiveExpanded : negativeExpanded;
                if (expanded != null) return expanded;
            }
            return positive ? positiveSingle : negativeSingle;
        }
    }

    private record DisplayConfigCache(long lastModified, DisplayColors colors) {}
}
