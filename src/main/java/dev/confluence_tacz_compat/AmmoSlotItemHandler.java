package dev.confluence_tacz_compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.confluence.mod.common.attachment.ExtraInventory;

import javax.annotation.Nullable;

/**
 * 把 Confluence 的专用弹药槽排在玩家物品栏之前，供 TACZ 自身的
 * {@code findAndExtractInventoryAmmo} 遍历逻辑读取。
 *
 * <p>这里只替换 TACZ 读取到的物品栏视图，不接管
 * {@code consumeAmmoFromPlayer} 本身。TACZ 的原生消耗流程因此保持完整，
 * 其它模组（例如 TACZ-addon 的精妙背包弹药支持）在该方法 RETURN 处追加的
 * 弹药来源不会被跳过；它补充的也只是我们扣完之后仍然缺少的数量。</p>
 */
public final class AmmoSlotItemHandler implements IItemHandler {
    private final ExtraInventory extraInventory;
    private final IItemHandler delegate;

    private AmmoSlotItemHandler(ExtraInventory extraInventory, @Nullable IItemHandler delegate) {
        this.extraInventory = extraInventory;
        this.delegate = delegate;
    }

    /**
     * 包装玩家的物品栏视图。非玩家实体保持原样。
     */
    @Nullable
    public static IItemHandler wrap(LivingEntity entity, @Nullable IItemHandler delegate) {
        if (!(entity instanceof Player player)) return delegate;
        return new AmmoSlotItemHandler(ExtraInventory.of(player), delegate);
    }

    @Override
    public int getSlots() {
        return ExtraInventory.SIZE_AMMO + (delegate == null ? 0 : delegate.getSlots());
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        if (isAmmoSlot(slot)) return extraInventory.getAmmo(slot);
        if (delegate == null) return ItemStack.EMPTY;
        return delegate.getStackInSlot(toDelegateSlot(slot));
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (isAmmoSlot(slot) || delegate == null) return stack;
        return delegate.insertItem(toDelegateSlot(slot), stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (isAmmoSlot(slot)) {
            return extractAmmoSlot(slot, amount, simulate);
        }
        if (delegate == null) return ItemStack.EMPTY;
        return delegate.extractItem(toDelegateSlot(slot), amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        if (isAmmoSlot(slot)) return TaczAmmoSlots.MAX_STACK_SIZE;
        if (delegate == null) return 0;
        return delegate.getSlotLimit(toDelegateSlot(slot));
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        if (isAmmoSlot(slot)) return TaczAmmoSlots.isTaczAmmo(stack);
        return delegate != null && delegate.isItemValid(toDelegateSlot(slot), stack);
    }

    private ItemStack extractAmmoSlot(int slot, int amount, boolean simulate) {
        if (amount <= 0) return ItemStack.EMPTY;

        ItemStack current = extraInventory.getAmmo(slot);
        if (current.isEmpty()) return ItemStack.EMPTY;

        int extracted = Math.min(amount, current.getCount());
        ItemStack result = current.copyWithCount(extracted);
        if (!simulate) {
            int remaining = current.getCount() - extracted;
            extraInventory.setAmmo(slot, remaining <= 0 ? ItemStack.EMPTY : current.copyWithCount(remaining));
        }
        return result;
    }

    private static boolean isAmmoSlot(int slot) {
        return slot >= 0 && slot < ExtraInventory.SIZE_AMMO;
    }

    private static int toDelegateSlot(int slot) {
        return slot - ExtraInventory.SIZE_AMMO;
    }
}
