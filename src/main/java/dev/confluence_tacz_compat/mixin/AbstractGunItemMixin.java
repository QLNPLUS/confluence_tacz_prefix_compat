package dev.confluence_tacz_compat.mixin;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.util.AttachmentDataUtils;
import dev.confluence_tacz_compat.TaczAmmoSlots;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractGunItem.class, remap = false)
public abstract class AbstractGunItemMixin {
    @Inject(method = "hasInventoryAmmo", at = @At("RETURN"), cancellable = true, remap = false)
    private void confluenceTaczCompat$checkAmmoSlotsForInventoryFeed(LivingEntity shooter, ItemStack gun,
                                                                      boolean needCheckAmmo,
                                                                      CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() || !(shooter instanceof Player)
                || !needCheckAmmo
                || !isInventoryFeed(gun)
                || usesDummyAmmo(gun)) {
            return;
        }
        if (TaczAmmoSlots.hasCompatibleAmmo(shooter, gun)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "canReload", at = @At("RETURN"), cancellable = true, remap = false)
    private void confluenceTaczCompat$checkAmmoSlotsForReload(LivingEntity shooter, ItemStack gun,
                                                               CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() || !(shooter instanceof Player)
                || isInventoryFeed(gun)
                || usesDummyAmmo(gun)
                || !TaczAmmoSlots.hasCompatibleAmmo(shooter, gun)) {
            return;
        }

        AbstractGunItem gunItem = (AbstractGunItem) (Object) this;
        CommonGunIndex gunIndex = TimelessAPI.getCommonGunIndex(gunItem.getGunId(gun)).orElse(null);
        if (gunIndex == null) return;

        int maxAmmo = AttachmentDataUtils.getAmmoCountWithAttachment(gun, gunIndex.getGunData());
        if (gunItem.getCurrentAmmoCount(gun) < maxAmmo) {
            cir.setReturnValue(true);
        }
    }

    private boolean isInventoryFeed(ItemStack gun) {
        return ((AbstractGunItem) (Object) this).useInventoryAmmo(gun);
    }

    private boolean usesDummyAmmo(ItemStack gun) {
        return ((AbstractGunItem) (Object) this).useDummyAmmo(gun);
    }
}
