package dev.confluence_tacz_compat.mixin;

import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import dev.confluence_tacz_compat.AmmoSlotItemHandler;
import dev.confluence_tacz_compat.TaczAmmoSlots;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ModernKineticGunScriptAPI.class, remap = false)
public abstract class ModernKineticGunScriptAPIMixin {
    @Shadow private LivingEntity shooter;
    @Shadow private ItemStack itemStack;
    @Shadow private AbstractGunItem abstractGunItem;
    @Shadow public abstract boolean isReloadingNeedConsumeAmmo();

    /**
     * 把 TACZ 读取的玩家物品栏替换成“Confluence 弹药槽优先”的视图。
     *
     * <p>不取消原方法：原生消耗流程必须继续执行，否则 TACZ-addon 等模组在
     * {@code consumeAmmoFromPlayer} RETURN 处追加的弹药来源（精妙背包等）
     * 会被整个跳过，返回的消耗量也会与这些来源无关。</p>
     */
    @Redirect(method = "consumeAmmoFromPlayer(I)I",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;"),
            remap = false)
    private <T> LazyOptional<T> confluenceTaczCompat$ammoSlotsFirst(LivingEntity instance, Capability<T> capability,
                                                                   Direction facing) {
        if (capability != ForgeCapabilities.ITEM_HANDLER || !(instance instanceof Player player)) {
            return instance.getCapability(capability, facing);
        }

        IItemHandler vanilla = instance.getCapability(ForgeCapabilities.ITEM_HANDLER, facing).orElse(null);
        return ForgeCapabilities.ITEM_HANDLER.orEmpty(capability,
                LazyOptional.of(() -> AmmoSlotItemHandler.wrap(player, vanilla)));
    }

    @Inject(method = "hasAmmoToConsume", at = @At("RETURN"), cancellable = true, remap = false)
    private void confluenceTaczCompat$checkAmmoSlots(CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()
                && isReloadingNeedConsumeAmmo()
                && !abstractGunItem.useDummyAmmo(itemStack)
                && TaczAmmoSlots.hasCompatibleAmmo(shooter, itemStack)) {
            cir.setReturnValue(true);
        }
    }
}
