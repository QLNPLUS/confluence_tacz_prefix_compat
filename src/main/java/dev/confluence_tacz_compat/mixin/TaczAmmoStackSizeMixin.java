package dev.confluence_tacz_compat.mixin;

import dev.confluence_tacz_compat.TaczAmmoSlots;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public abstract class TaczAmmoStackSizeMixin {
    @Inject(method = "getMaxStackSize", at = @At("HEAD"), cancellable = true)
    private void confluenceTaczCompat$raiseTaczAmmoStackLimit(CallbackInfoReturnable<Integer> cir) {
        if (TaczAmmoSlots.isTaczAmmo((ItemStack) (Object) this)) {
            cir.setReturnValue(TaczAmmoSlots.MAX_STACK_SIZE);
        }
    }
}
