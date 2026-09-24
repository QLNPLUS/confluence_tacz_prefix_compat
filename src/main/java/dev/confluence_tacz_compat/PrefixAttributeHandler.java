package dev.confluence_tacz_compat;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import org.confluence.mod.common.component.prefix.PrefixComponent;
import org.confluence.mod.common.component.prefix.PrefixType;
import org.confluence.mod.util.PrefixUtils;

import java.util.Collection;
import java.util.Map;

/**
 * 词条属性替换。
 *
 * <p>Confluence 在 {@link ItemAttributeModifierEvent} 里把词条修饰符挂到物品上（默认优先级）。
 * 本监听器以 LOWEST 优先级在之后运行，按配置把枪械词条中指定来源属性的修饰符移动到目标属性；
 * 属性修饰符的 ID、数值和运算方式保持不变。</p>
 *
 * <p>只移动词条组件里记录的修饰符实例，因此不会影响枪械自身或其它模组添加的修饰符。
 * 由于该事件在客户端同样会触发（物品属性提示），提示中的数值与实际结算一致。</p>
 */
public final class PrefixAttributeHandler {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (!GunTags.isGun(stack)) return;

        PrefixComponent prefix = PrefixUtils.getPrefix(stack);
        if (prefix == null
                || prefix.type() == PrefixType.UNKNOWN
                || prefix.type() == PrefixType.ACCESSORY
                || prefix.modifiers().isEmpty()) {
            return;
        }

        GunAttributeMapping mapping = GunAttributeMapping.get();
        if (mapping == null) return;

        for (Map.Entry<Holder<Attribute>, Collection<AttributeModifier>> entry : prefix.modifiers().get().asMap().entrySet()) {
            Holder<Attribute> target = mapping.map(entry.getKey());
            if (target == null || target == entry.getKey()) continue;
            for (AttributeModifier modifier : entry.getValue()) {
                // 1.21.1 的事件按修饰符 id 移除
                if (event.removeModifier(entry.getKey(), modifier.id())) {
                    event.addModifier(target, modifier, EquipmentSlotGroup.MAINHAND);
                }
            }
        }
    }
}
