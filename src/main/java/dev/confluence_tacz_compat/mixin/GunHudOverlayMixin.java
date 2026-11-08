package dev.confluence_tacz_compat.mixin;

import com.mojang.logging.LogUtils;
import com.tacz.guns.api.item.IGun;
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
    private static long confluenceTaczCompat$handledTimestamp = Long.MIN_VALUE;

    @Shadow(remap = false) private static int cacheInventoryAmmoCount;
    @Shadow(remap = false) private static long checkAmmoTimestamp;

    /**
     * TACZ 1.1.x 及更早版本：HUD 备弹由 {@code handleInventoryAmmo} 统计。
     *
     * <p>开启 {@code hud.fixBackpackAmmoDoubleCount} 时，在 TACZ-addon 传入的虚拟物品栏
     * 基础上对非主物品栏部分按内容去重，再补上 Confluence 弹药槽，然后取消原方法，
     * 避免原生遍历覆盖结果。关闭后完全保留 TACZ-addon 原本的统计方式。</p>
     *
     * <p>TACZ 1.2.0 移除了 {@code handleInventoryAmmo}（改由 {@code ClientAmmoCount} 统计），
     * 这些注入用 {@code require = 0} 声明为可选，在新版本中静默跳过。</p>
     */
    @Inject(
            method = "handleInventoryAmmo",
            at = @At("HEAD"),
            cancellable = true,
            require = 0,
            remap = false
    )
    private static void confluenceTaczCompat$includeAmmoSlots(ItemStack gun, Inventory inventory,
                                                               CallbackInfo ci) {
        if (!CompatConfig.fixBackpackAmmoDoubleCount()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

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

    /** TACZ 1.1.x：开关关闭时保留原统计，仅补上 Confluence 弹药槽。 */
    @Inject(
            method = "handleInventoryAmmo",
            at = @At("RETURN"),
            require = 0,
            remap = false
    )
    private static void confluenceTaczCompat$appendAmmoSlotsLegacy(ItemStack gun, Inventory inventory,
                                                                    CallbackInfo ci) {
        confluenceTaczCompat$handledTimestamp = checkAmmoTimestamp;
        if (CompatConfig.fixBackpackAmmoDoubleCount()) return;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            cacheInventoryAmmoCount += TaczAmmoSlots.countCompatibleAmmo(player, gun, inventory);
        }
    }

    /**
     * 给 HUD 备弹补上 Confluence 弹药槽，覆盖 TACZ 1.2.0+ 的统计路径。
     *
     * <p>1.2.0 把备弹统计搬进 {@code ClientAmmoCount}，{@code handleInventoryAmmo} 不复存在，
     * 且默认只遍历玩家物品栏。这里在 {@code handleCacheCount} 返回时把弹药槽数量并入
     * 缓存值；该方法每 50 ms 才刷新一次，用 {@code checkAmmoTimestamp} 变化保证只追加一次。</p>
     */
    @Inject(
            method = "handleCacheCount",
            at = @At("RETURN"),
            remap = false
    )
    private static void confluenceTaczCompat$appendAmmoSlots(CallbackInfo ci) {
        if (confluenceTaczCompat$recounted) {
            confluenceTaczCompat$recounted = false;
            if (confluenceTaczCompat$lastTotal >= 0) {
                cacheInventoryAmmoCount = confluenceTaczCompat$lastTotal;
            }
            confluenceTaczCompat$handledTimestamp = checkAmmoTimestamp;
            return;
        }
        if (checkAmmoTimestamp == confluenceTaczCompat$handledTimestamp) return;
        confluenceTaczCompat$handledTimestamp = checkAmmoTimestamp;

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        ItemStack gun = player.getMainHandItem();
        if (!(gun.getItem() instanceof IGun iGun) || iGun.useDummyAmmo(gun)) return;
        // 创造模式 / 无限备弹（9999）不需要再叠加。
        if (cacheInventoryAmmoCount >= TaczAmmoSlots.CREATIVE_AMMO_COUNT) return;

        cacheInventoryAmmoCount += TaczAmmoSlots.countCompatibleAmmo(player, gun);
    }

    /**
     * 移除 TACZ 对备弹数的 9999 硬上限。
     *
     * <p>本模组允许 TACZ 弹药堆叠到 9999，弹药槽 + 背包 + 主物品栏的总备弹可能超过 9999；
     * 原生 {@code Math.min(ammoCount, MAX_AMMO_COUNT)} 会把真实数量截断成 9999。</p>
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
