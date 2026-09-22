package dev.confluence_tacz_compat.mixin;

import dev.confluence_tacz_compat.CompatConfig;
import dev.confluence_tacz_compat.GunTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import org.confluence.mod.common.component.prefix.PrefixComponent;
import org.confluence.mod.common.component.prefix.PrefixType;
import org.confluence.mod.util.PrefixUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * 接管 TACZ 枪械的“获取时随机词条”判定。
 *
 * <p>Confluence 的获取流程（掉落、拾取、合成等）统一经过
 * {@link PrefixUtils#initPrefix(RandomSource, ItemStack)}，且没有任何事件钩子，
 * 因此只能用 Mixin 拦截。这里对枪械重写同样的逻辑，只是把概率换成配置值，
 * 并允许通过配置完全关闭。</p>
 *
 * <p>关闭时返回 null 而不写入 unknown 标记：标记会把物品永久标记为“已判定且无词条”，
 * 那样之后开启配置也无法再获得词条，不符合“开关”的预期。</p>
 */
@Mixin(value = PrefixUtils.class, remap = false)
public abstract class PrefixUtilsMixin {
    @Inject(method = "initPrefix", at = @At("HEAD"), cancellable = true, remap = false)
    private static void compat$rollGunPrefix(RandomSource random, ItemStack itemStack,
                                             CallbackInfoReturnable<PrefixComponent> cir) {
        if (!GunTags.isGun(itemStack)) return;

        if (!CompatConfig.enableAutoPrefix()) {
            cir.setReturnValue(null);
            return;
        }

        double chance = CompatConfig.autoPrefixChance();
        if (random.nextFloat() < chance) {
            PrefixType prefixType = PrefixUtils.getPrefixType(itemStack);
            if (prefixType != PrefixType.UNKNOWN) {
                cir.setReturnValue(PrefixUtils.createWithMercy(random, itemStack, prefixType));
                return;
            }
        } else {
            // 与 Confluence 一致：本次判定为“无词条”，写入 unknown 以免反复判定。
            PrefixUtils.unknown(itemStack);
        }
        cir.setReturnValue(null);
    }
}
