package dev.confluence_tacz_compat;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

/**
 * 通用配置。所有选项都在服务端生效；COMMON 类型保证客户端也能读到同样的值，
 * 这样物品属性提示（ItemAttributeModifierEvent 在客户端也会触发）与服务器一致。
 */
public final class CompatConfig {
    public static final ForgeConfigSpec SPEC;

    private static final ForgeConfigSpec.BooleanValue ENABLE_AUTO_PREFIX;
    private static final ForgeConfigSpec.DoubleValue AUTO_PREFIX_CHANCE;
    private static final ForgeConfigSpec.BooleanValue ATTRIBUTE_REPLACEMENT_ENABLED;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ATTRIBUTE_MAPPINGS;
    private static final ForgeConfigSpec.BooleanValue CRITICAL_CHANCE_TOOLTIP_COMPAT;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("TACZ 枪械获取时随机词条").push("acquire");
        ENABLE_AUTO_PREFIX = builder
                .comment("关闭后，TACZ 枪械在获取（掉落、拾取、合成等）时不会自动获得 Confluence 词条。",
                        "注意：关闭只影响“自动获取”，哥布林工匠处的重铸仍然可用。")
                .define("enableAutoPrefix", true);
        AUTO_PREFIX_CHANCE = builder
                .comment("自动获得词条的概率，取值 [0,1]。",
                        "Confluence 默认值为 0.75；未获得词条时会写入 unknown 标记，因此同一件枪械不会被反复判定。",
                        "设为 0 等于关闭自动获取，但仍保留 unknown 标记行为。")
                .defineInRange("autoPrefixChance", 0.75D, 0.0D, 1.0D);
        builder.pop();

        builder.comment("词条属性映射：按配置把 Confluence 的词条属性替换为任意已注册属性。",
                        "GunsmithLib 未安装时，默认目标属性不会注册；对应映射会自动跳过。").push("gunsmithlib");
        ATTRIBUTE_REPLACEMENT_ENABLED = builder
                .comment("映射总开关。关闭后禁用下方全部映射。")
                .define("useGunsmithLibAttributes", true);
        ATTRIBUTE_MAPPINGS = builder
                .comment("属性替换表，每项格式为 source_namespace:source_path=target_namespace:target_path。",
                        "Confluence 远程词缀涉及：confluence_magic_lib:generic.ranged_damage、minecraft:generic.attack_speed、",
                        "confluence_magic_lib:generic.critical_chance、confluence_magic_lib:generic.ranged_velocity、minecraft:generic.attack_knockback。",
                        "未注册的来源或目标属性会被跳过；重复来源以后出现的配置为准。",
                        "暴击率和击退没有 GunsmithLib 默认目标，可在这里填写其它模组已注册且会被枪械逻辑读取的属性。")
                .defineList("attributeMappings", List.of(
                        "confluence_magic_lib:generic.ranged_damage=gunsmithlib:bullet_damage",
                        "minecraft:generic.attack_speed=gunsmithlib:rpm",
                        "confluence_magic_lib:generic.ranged_velocity=gunsmithlib:bullet_speed"
                ), CompatConfig::isAttributeMapping);
        builder.pop();

        builder.comment("暴击率 tooltip 兼容").push("tooltip");
        CRITICAL_CHANCE_TOOLTIP_COMPAT = builder
                .comment("开启后，会把纯文本暴击率行末尾的属性名按 attribute_display_style.json 的 name_color 重新着色，",
                        "用于修复 Confluence 把暴击率行压平成字面量后第三方 tooltip 模组无法识别属性名的问题。",
                        "关闭后完全不再改写任何 tooltip 行，保留其它模组或 KubeJS 原本设置的颜色。")
                .define("colorCriticalChanceName", true);
        builder.pop();

        SPEC = builder.build();
    }

    private CompatConfig() {}

    public static boolean enableAutoPrefix() {
        return ENABLE_AUTO_PREFIX.get();
    }

    public static double autoPrefixChance() {
        return AUTO_PREFIX_CHANCE.get();
    }

    public static boolean attributeReplacementEnabled() {
        return ATTRIBUTE_REPLACEMENT_ENABLED.get();
    }

    public static List<? extends String> attributeMappings() {
        return ATTRIBUTE_MAPPINGS.get();
    }

    public static boolean colorCriticalChanceName() {
        return CRITICAL_CHANCE_TOOLTIP_COMPAT.get();
    }

    private static boolean isAttributeMapping(Object value) {
        if (!(value instanceof String mapping)) return false;
        int separator = mapping.indexOf('=');
        if (separator <= 0 || separator != mapping.lastIndexOf('=')) return false;
        return isNamespacedId(mapping.substring(0, separator).trim())
                && isNamespacedId(mapping.substring(separator + 1).trim());
    }

    private static boolean isNamespacedId(String value) {
        return value.indexOf(':') > 0 && ResourceLocation.tryParse(value) != null;
    }
}
