package dev.confluence_tacz_compat;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.confluence.mod.common.attachment.ExtraInventory;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Set;

/** Shared lookup logic for TACZ ammunition in Confluence's ammo slots. */
public final class TaczAmmoSlots {
    public static final int MAX_STACK_SIZE = 9999;
    public static final int CREATIVE_AMMO_COUNT = 9999;

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
        return countCompatibleAmmo(shooter, gun, null);
    }

    /**
     * Counts matching TACZ ammo stored in Confluence's dedicated ammo slots,
     * skipping any stack that {@code alreadyCounted} already contains by identity.
     *
     * <p>其它模组（例如 TACZ-addon）可能把弹药槽的 stack 实例并进自己的 HUD 视图；
     * 按实例去重可以避免同一摞弹药被统计两次。</p>
     */
    public static int countCompatibleAmmo(LivingEntity shooter, ItemStack gun,
                                          @Nullable Inventory alreadyCounted) {
        if (!(shooter instanceof Player player)) return 0;

        ExtraInventory inventory = ExtraInventory.of(player);
        int count = 0;
        for (int i = 0; i < ExtraInventory.SIZE_AMMO; i++) {
            ItemStack ammoStack = inventory.getAmmo(i);
            if (!isCompatibleAmmo(gun, ammoStack)) continue;
            if (alreadyCounted != null && containsSameInstance(alreadyCounted, ammoStack)) continue;
            count += ammoStack.getCount();
        }
        return count;
    }

    private static boolean containsSameInstance(Inventory inventory, ItemStack target) {
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            if (inventory.getItem(i) == target) return true;
        }
        return false;
    }

    /**
     * 统计 TACZ-addon 传入的虚拟物品栏中的备弹，并对非主物品栏部分按内容去重。
     *
     * <p>TACZ-addon 可能把同一背包同时经 Create Storage 和 Sophisticated Backpacks
     * 两个兼容层各统计一次，导致 HUD 显示两倍。主物品栏的 stack 是原对象引用，
     * 保持原样累加；其余来源（背包 / Curios 的副本）用 item + NBT + 数量作为键，
     * 同一内容只计一次。</p>
     *
     * <p>返回 {@link #CREATIVE_AMMO_COUNT} 表示发现创造弹药箱（TACZ 的无限弹药语义）。</p>
     */
    public static int countDistinctVirtualAmmo(Player player, Inventory virtual, ItemStack gun) {
        Set<ItemStack> mainReferences = Collections.newSetFromMap(new IdentityHashMap<>());
        mainReferences.addAll(player.getInventory().items);

        Set<AmmoKey> distinct = new HashSet<>();
        int total = 0;

        for (int i = 0; i < virtual.getContainerSize(); i++) {
            ItemStack stack = virtual.getItem(i);
            if (stack.isEmpty()) continue;

            int amount = countAmmo(stack, gun);
            if (amount < 0) return CREATIVE_AMMO_COUNT;
            if (amount == 0) continue;

            if (mainReferences.contains(stack) || distinct.add(new AmmoKey(stack, amount))) {
                total += amount;
            }
        }
        return total;
    }

    /** @return 匹配的弹药数量；{@code -1} 表示创造弹药箱。 */
    private static int countAmmo(ItemStack stack, ItemStack gun) {
        if (stack.isEmpty()) return 0;

        if (stack.getItem() instanceof IAmmo ammo && ammo.isAmmoOfGun(gun, stack)) {
            return stack.getCount();
        }
        if (stack.getItem() instanceof IAmmoBox box && box.isAmmoBoxOfGun(gun, stack)) {
            if (box.isAllTypeCreative(stack) || box.isCreative(stack)) return -1;
            return Math.max(0, box.getAmmoCount(stack));
        }
        return 0;
    }

    private record AmmoKey(Item item, @Nullable CompoundTag tag, int amount) {
        private AmmoKey(ItemStack stack, int amount) {
            this(stack.getItem(), stack.getTag(), amount);
        }
    }

    private static boolean isCompatibleAmmo(ItemStack gun, ItemStack ammoStack) {
        return !ammoStack.isEmpty()
                && ammoStack.getItem() instanceof IAmmo ammo
                && ammo.isAmmoOfGun(gun, ammoStack);
    }
}
