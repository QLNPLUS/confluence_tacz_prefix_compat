package dev.confluence_tacz_compat;

import com.tacz.guns.api.item.IAmmo;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.confluence.mod.common.attachment.ExtraInventory;

/** Shared lookup and extraction logic for TACZ ammunition in Confluence's ammo slots. */
public final class TaczAmmoSlots {
    public static final int MAX_STACK_SIZE = 9999;

    private TaczAmmoSlots() {}

    public static boolean isTaczAmmo(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof IAmmo;
    }

    public static boolean hasCompatibleAmmo(LivingEntity shooter, ItemStack gun) {
        if (!(shooter instanceof Player player)) return false;

        ExtraInventory inventory = ExtraInventory.of(player);
        for (int i = 0; i < ExtraInventory.SIZE_AMMO; i++) {
            ItemStack ammoStack = inventory.getAmmo(i);
            if (isCompatibleAmmo(gun, ammoStack)) return true;
        }
        return false;
    }

    /** Counts matching TACZ ammo stored in Confluence's dedicated ammo slots. */
    public static int countCompatibleAmmo(LivingEntity shooter, ItemStack gun) {
        if (!(shooter instanceof Player player)) return 0;

        ExtraInventory inventory = ExtraInventory.of(player);
        int count = 0;
        for (int i = 0; i < ExtraInventory.SIZE_AMMO; i++) {
            ItemStack ammoStack = inventory.getAmmo(i);
            if (isCompatibleAmmo(gun, ammoStack)) {
                count += ammoStack.getCount();
            }
        }
        return count;
    }

    /** Removes matching ammo slots first and returns the amount taken. */
    public static int consumeCompatibleAmmo(LivingEntity shooter, ItemStack gun, int amount) {
        if (amount <= 0 || !(shooter instanceof Player player)) return 0;

        ExtraInventory inventory = ExtraInventory.of(player);
        int remaining = amount;
        for (int i = 0; i < ExtraInventory.SIZE_AMMO && remaining > 0; i++) {
            ItemStack ammoStack = inventory.getAmmo(i);
            if (!isCompatibleAmmo(gun, ammoStack)) continue;

            int extracted = Math.min(remaining, ammoStack.getCount());
            ammoStack.shrink(extracted);
            inventory.setAmmo(i, ammoStack.isEmpty() ? ItemStack.EMPTY : ammoStack);
            remaining -= extracted;
        }
        return amount - remaining;
    }

    private static boolean isCompatibleAmmo(ItemStack gun, ItemStack ammoStack) {
        return !ammoStack.isEmpty()
                && ammoStack.getItem() instanceof IAmmo ammo
                && ammo.isAmmoOfGun(gun, ammoStack);
    }
}
