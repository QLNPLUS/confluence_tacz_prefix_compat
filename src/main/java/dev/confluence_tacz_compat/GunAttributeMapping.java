package dev.confluence_tacz_compat;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.fml.ModList;
import org.confluence.lib.common.LibAttributes;
import org.jetbrains.annotations.Nullable;

/**
 * Confluence 词条属性 -> GunsmithLib 属性 的映射。
 *
 * <p>基于 GunsmithLib 的属性结算方式 {@code GsHelper.getAttributeValueWithBase(holder, attribute, base)}：
 * 它会把 base 临时设为枪械自身数值，再套用属性修饰符。因此 Confluence 词条使用的
 * {@code ADD_MULTIPLY_TOTAL}（百分比）语义可以原样平移，无需换算。</p>
 *
 * <p>属性 id 通过注册表按名解析，GunsmithLib 因此是软依赖：未安装时映射整体失效，
 * 词条保留 Confluence 原本的属性。</p>
 */
public final class GunAttributeMapping {
    public static final String GUNSMITHLIB_MOD_ID = "gunsmithlib";

    private static final ResourceLocation BULLET_DAMAGE_ID = ResourceLocation.fromNamespaceAndPath(GUNSMITHLIB_MOD_ID, "bullet_damage");
    private static final ResourceLocation RPM_ID = ResourceLocation.fromNamespaceAndPath(GUNSMITHLIB_MOD_ID, "rpm");
    private static final ResourceLocation BULLET_SPEED_ID = ResourceLocation.fromNamespaceAndPath(GUNSMITHLIB_MOD_ID, "bullet_speed");

    private static GunAttributeMapping cached;

    private final Holder<Attribute> bulletDamage;
    private final Holder<Attribute> rpm;
    private final Holder<Attribute> bulletSpeed;

    private GunAttributeMapping(Holder<Attribute> bulletDamage, Holder<Attribute> rpm, Holder<Attribute> bulletSpeed) {
        this.bulletDamage = bulletDamage;
        this.rpm = rpm;
        this.bulletSpeed = bulletSpeed;
    }

    /** 返回可用的映射；GunsmithLib 缺失或配置关闭时返回 null。 */
    public static @Nullable GunAttributeMapping get() {
        if (!CompatConfig.useGunsmithLibAttributes()) return null;
        GunAttributeMapping mapping = cached;
        if (mapping != null) return mapping;
        if (!ModList.get().isLoaded(GUNSMITHLIB_MOD_ID)) return null;
        Holder<Attribute> damage = resolve(BULLET_DAMAGE_ID);
        Holder<Attribute> rate = resolve(RPM_ID);
        Holder<Attribute> speed = resolve(BULLET_SPEED_ID);
        if (damage == null || rate == null || speed == null) return null;
        mapping = new GunAttributeMapping(damage, rate, speed);
        cached = mapping;
        return mapping;
    }

    private static @Nullable Holder<Attribute> resolve(ResourceLocation id) {
        return BuiltInRegistries.ATTRIBUTE.getHolder(id).orElse(null);
    }

    /**
     * @return 目标属性；该属性无需替换时返回 null
     */
    public @Nullable Holder<Attribute> map(Holder<Attribute> source) {
        if (CompatConfig.replaceRangedDamage() && source.value() == LibAttributes.getRangedDamage().value()) {
            return bulletDamage;
        }
        if (CompatConfig.replaceAttackSpeed() && source.value() == Attributes.ATTACK_SPEED.value()) {
            return rpm;
        }
        if (CompatConfig.replaceRangedVelocity() && source.value() == LibAttributes.getRangedVelocity().value()) {
            return bulletSpeed;
        }
        // 暴击率、击退等没有对应的 GunsmithLib 属性，保持 Confluence 原样。
        return null;
    }
}
