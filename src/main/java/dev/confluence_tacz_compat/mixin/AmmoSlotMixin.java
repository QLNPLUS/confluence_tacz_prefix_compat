package dev.confluence_tacz_compat.mixin;

import dev.confluence_tacz_compat.TaczAmmoSlots;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.confluence.mod.common.attachment.ExtraInventory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Slot.class)
public abstract class AmmoSlotMixin {
    @Shadow @Final public Container container;
    @Shadow @Final public int index;
    @Shadow public abstract ItemStack getItem();

    @Inject(method = "mayPlace", at = @At("HEAD"), cancellable = true)
    private void confluenceTaczCompat$allowTaczAmmo(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (isAmmoSlot() && TaczAmmoSlots.isTaczAmmo(stack)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getMaxStackSize()I", at = @At("HEAD"), cancellable = true)
    private void confluenceTaczCompat$allowLargeStoredAmmo(CallbackInfoReturnable<Integer> cir) {
        if (isAmmoSlot() && TaczAmmoSlots.isTaczAmmo(getItem())) {
            cir.setReturnValue(TaczAmmoSlots.MAX_STACK_SIZE);
        }
    }

    @Inject(method = "getMaxStackSize(Lnet/minecraft/world/item/ItemStack;)I", at = @At("HEAD"), cancellable = true)
    private void confluenceTaczCompat$allowLargeIncomingAmmo(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        if (isAmmoSlot() && TaczAmmoSlots.isTaczAmmo(stack)) {
            cir.setReturnValue(TaczAmmoSlots.MAX_STACK_SIZE);
        }
    }

    private boolean isAmmoSlot() {
        return container instanceof ExtraInventory
                && index >= ExtraInventory.AMMO_START
                && index < ExtraInventory.AMMO_START + ExtraInventory.SIZE_AMMO;
    }
}
