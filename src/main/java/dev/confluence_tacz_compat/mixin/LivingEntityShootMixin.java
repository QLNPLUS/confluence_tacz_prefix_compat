package dev.confluence_tacz_compat.mixin;

import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.entity.shooter.LivingEntityShoot;
import dev.confluence_tacz_compat.TaczAmmoSlots;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = LivingEntityShoot.class, remap = false)
public abstract class LivingEntityShootMixin {
    @Shadow @Final private LivingEntity shooter;

    @Inject(method = "consumeAmmoFromPlayer",
            at = @At("HEAD"), cancellable = true, remap = false)
    private void confluenceTaczCompat$consumeAmmoSlotsFirst(int neededAmount, ItemStack gun, boolean needCheckAmmo,
                                                             CallbackInfo ci) {
        if (!needCheckAmmo || !(gun.getItem() instanceof AbstractGunItem gunItem) || gunItem.useDummyAmmo(gun)) return;

        int fromAmmoSlots = TaczAmmoSlots.consumeCompatibleAmmo(shooter, gun, neededAmount);
        int remaining = neededAmount - fromAmmoSlots;
        if (remaining > 0) {
            shooter.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                    .ifPresent(cap -> gunItem.findAndExtractInventoryAmmo(cap, gun, remaining));
        }
        ci.cancel();
    }
}
