package dev.confluence_tacz_compat;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 枪械判定。
 *
 * <p>TACZ 的所有枪械共用同一个物品 {@code tacz:modern_kinetic_gun}，且 TACZ 自身不提供
 * “全部枪械”的物品标签；因此这里使用本模组自己的标签，默认包含该物品，
 * 整合包可以通过数据包追加自定义枪械物品。</p>
 */
public final class GunTags {
    public static final TagKey<Item> GUNS =
            TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(ConfluenceTaczCompat.MOD_ID, "guns"));

    private GunTags() {}

    public static boolean isGun(ItemStack stack) {
        return !stack.isEmpty() && stack.is(GUNS);
    }
}
