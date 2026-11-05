package dev.confluence_tacz_compat.mixin;

import com.mojang.logging.LogUtils;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import dev.confluence_tacz_compat.CompatConfig;
import dev.confluence_tacz_compat.TaczAmmoSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GunHudOverlay.class, remap = false)
public abstract class GunHudOverlayMixin {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean confluenceTaczCompat$loggedRecount;
    private static int confluenceTaczCompat$lastTotal = -1;
    private static boolean confluenceTaczCompat$recounted;

    @Shadow(remap = false) private static int cacheInventoryAmmoCount;

    /**
     * 接管 HUD 备弹统计。
     *
     * <p>开启 {@code hud.fixBackpackAmmoDoubleCount} 时，在 TACZ-addon 传入的虚拟物品栏
     * 基础上对非主物品栏部分按内容去重，再补上 Confluence 弹药槽，然后取消原方法，
     * 避免原生遍历覆盖结果。关闭后完全保留 TACZ-addon 原本的统计方式。</p>
     */
    @Inject(
            method = "handleInventoryAmmo",
            at = @At("HEAD"),
            cancellable = true,
            remap = false
    )
    private static void confluenceTaczCompat$includeAmmoSlots(ItemStack gun, Inventory inventory,
                                                               CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null || !CompatConfig.fixBackpackAmmoDoubleCount()) return;

        int total = TaczAmmoSlots.countDistinctVirtualAmmo(player, inventory, gun);
        if (total != TaczAmmoSlots.CREATIVE_AMMO_COUNT) {
            total += TaczAmmoSlots.countCompatibleAmmo(player, gun, inventory);
        }
        cacheInventoryAmmoCount = total;
        confluenceTaczCompat$lastTotal = total;
        confluenceTaczCompat$recounted = true;
        if (!confluenceTaczCompat$loggedRecount) {
            confluenceTaczCompat$loggedRecount = true;
            LOGGER.info("[confluence_tacz_compat] HUD ammo recount active: inventory={}, total={}",
                    inventory.getClass().getName(), total);
        }
        ci.cancel();
    }

    /**
     * 在 {@code handleCacheCount} 返回时写回最终值。
     *
     * <p>实测 {@code curios_for_ammo_box} 等模组会在 {@code handleInventoryAmmo}
     * 里再次追加背包弹药，覆盖我们算好的结果。这里位于调用链外层，最后写一次，
     * 保证 HUD 使用的就是去重后的数量。</p>
     */
    @Inject(
            method = "handleCacheCount",
            at = @At("RETURN"),
            remap = false
    )
    private static void confluenceTaczCompat$restoreRecount(CallbackInfo ci) {
        if (!CompatConfig.fixBackpackAmmoDoubleCount()) return;
        if (!confluenceTaczCompat$recounted) return;

        confluenceTaczCompat$recounted = false;
        if (confluenceTaczCompat$lastTotal >= 0) {
            cacheInventoryAmmoCount = confluenceTaczCompat$lastTotal;
        }
    }

    /** 开关关闭时保留原统计，仅补上 Confluence 弹药槽。 */
    @Inject(
            method = "handleInventoryAmmo",
            at = @At("RETURN"),
            remap = false
    )
    private static void confluenceTaczCompat$appendAmmoSlots(ItemStack gun, Inventory inventory,
                                                              CallbackInfo ci) {
        if (CompatConfig.fixBackpackAmmoDoubleCount()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            cacheInventoryAmmoCount += TaczAmmoSlots.countCompatibleAmmo(player, gun, inventory);
        }
    }

    /**
     * 移除 TACZ 对背包直读弹药数的 9999 硬上限。
     *
     * <p>本模组允许 TACZ 弹药堆叠到 9999，弹药槽 + 背包 + 主物品栏的总备弹可能超过 9999；
     * 原生 {@code Math.min(ammoCount, 9999)} 会把真实数量截断成 9999。</p>
     */
    @Redirect(
            method = "render",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"),
            remap = false
    )
    private int confluenceTaczCompat$uncapHudAmmoCount(int left, int right) {
        return left;
    }
}
