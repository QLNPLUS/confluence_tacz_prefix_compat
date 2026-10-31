package dev.confluence_tacz_compat.mixin;

import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import dev.confluence_tacz_compat.TaczAmmoSlots;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GunHudOverlay.class, remap = false)
public abstract class GunHudOverlayMixin {
    @Shadow(remap = false) private static int cacheInventoryAmmoCount;

    @Inject(
            method = "handleInventoryAmmo",
            at = @At("RETURN"),
            remap = false
    )
    private static void confluenceTaczCompat$includeAmmoSlots(ItemStack gun, Inventory inventory,
                                                               CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            cacheInventoryAmmoCount += TaczAmmoSlots.countCompatibleAmmo(player, gun);
        }
    }
}
