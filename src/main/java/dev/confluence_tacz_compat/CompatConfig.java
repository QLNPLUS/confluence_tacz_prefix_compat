package dev.confluence_tacz_compat;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * 通用配置。所有选项都在服务端生效；COMMON 类型保证客户端也能读到同样的值，
 * 这样物品属性提示（ItemAttributeModifierEvent 在客户端也会触发）与服务器一致。
 */
public final class CompatConfig {
    public static final ModConfigSpec SPEC;

    private static final ModConfigSpec.BooleanValue ENABLE_AUTO_PREFIX;
    private static final ModConfigSpec.DoubleValue AUTO_PREFIX_CHANCE;
    private static final ModConfigSpec.BooleanValue USE_GUNSMITHLIB_ATTRIBUTES;
    private static final ModConfigSpec.BooleanValue REPLACE_RANGED_DAMAGE;
    private static final ModConfigSpec.BooleanValue REPLACE_ATTACK_SPEED;
    private static final ModConfigSpec.BooleanValue REPLACE_RANGED_VELOCITY;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();

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

        builder.comment("词条属性映射：把 Confluence 的远程属性替换为 GunsmithLib 属性",
                        "GunsmithLib 未安装时，下面的开关自动失效，词条保持 Confluence 原属性。").push("gunsmithlib");
        USE_GUNSMITHLIB_ATTRIBUTES = builder
                .comment("总开关。关闭后枪械词条完全保留 Confluence 原属性（对枪械而言大多不生效）。")
                .define("useGunsmithLibAttributes", true);
        REPLACE_RANGED_DAMAGE = builder
                .comment("远程伤害 (confluence_magic_lib:generic.ranged_damage) -> gunsmithlib:bullet_damage。")
                .define("replaceRangedDamage", true);
        REPLACE_ATTACK_SPEED = builder
                .comment("攻击速度 (minecraft:attack_speed) -> gunsmithlib:rpm。")
                .define("replaceAttackSpeed", true);
        REPLACE_RANGED_VELOCITY = builder
                .comment("远程弹速 (confluence_magic_lib:generic.ranged_velocity) -> gunsmithlib:bullet_speed。")
                .define("replaceRangedVelocity", true);
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

    public static boolean useGunsmithLibAttributes() {
        return USE_GUNSMITHLIB_ATTRIBUTES.get();
    }

    public static boolean replaceRangedDamage() {
        return REPLACE_RANGED_DAMAGE.get();
    }

    public static boolean replaceAttackSpeed() {
        return REPLACE_ATTACK_SPEED.get();
    }

    public static boolean replaceRangedVelocity() {
        return REPLACE_RANGED_VELOCITY.get();
    }
}
