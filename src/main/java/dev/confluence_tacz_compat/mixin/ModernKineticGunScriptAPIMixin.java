package dev.confluence_tacz_compat.mixin;

import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import dev.confluence_tacz_compat.TaczAmmoSlots;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ModernKineticGunScriptAPI.class, remap = false)
public abstract class ModernKineticGunScriptAPIMixin {
    @Shadow private LivingEntity shooter;
    @Shadow private ItemStack itemStack;
    @Shadow private AbstractGunItem abstractGunItem;
    @Shadow public abstract boolean isReloadingNeedConsumeAmmo();

    @Inject(method = "consumeAmmoFromPlayer", at = @At("HEAD"), cancellable = true, remap = false)
    private void confluenceTaczCompat$consumeAmmoSlotsFirst(int neededAmount,
                                                             CallbackInfoReturnable<Integer> cir) {
        if (abstractGunItem.useInventoryAmmo(itemStack) && !isReloadingNeedConsumeAmmo()) {
            cir.setReturnValue(neededAmount);
            return;
        }
        if (abstractGunItem.useDummyAmmo(itemStack)) {
            cir.setReturnValue(abstractGunItem.findAndExtractDummyAmmo(itemStack, neededAmount));
            return;
        }

        int fromAmmoSlots = TaczAmmoSlots.consumeCompatibleAmmo(shooter, itemStack, neededAmount);
        int remaining = neededAmount - fromAmmoSlots;
        int fromInventory = remaining == 0 ? 0 : shooter.getCapability(ForgeCapabilities.ITEM_HANDLER, null)
                .map(cap -> abstractGunItem.findAndExtractInventoryAmmo(cap, itemStack, remaining))
                .orElse(0);
        cir.setReturnValue(fromAmmoSlots + fromInventory);
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
