package dev.confluence_tacz_compat.mixin;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import dev.confluence_tacz_compat.TaczAmmoSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GunAnimationStateContext.class, remap = false)
public abstract class GunAnimationStateContextMixin {
    @Shadow private ItemStack currentGunItem;
    @Shadow private IGun iGun;

    @Inject(method = "hasAmmoToConsume", at = @At("RETURN"), cancellable = true, remap = false)
    private void confluenceTaczCompat$checkAmmoSlots(CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue() || currentGunItem == null || iGun == null
                || iGun.useDummyAmmo(currentGunItem)
                || !(Minecraft.getInstance().cameraEntity instanceof LivingEntity shooter)) {
            return;
        }
        if (TaczAmmoSlots.hasCompatibleAmmo(shooter, currentGunItem)) {
            cir.setReturnValue(true);
        }
    }
}
