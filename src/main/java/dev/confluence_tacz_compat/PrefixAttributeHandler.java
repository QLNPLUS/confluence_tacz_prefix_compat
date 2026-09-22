package dev.confluence_tacz_compat;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.confluence.mod.common.component.prefix.PrefixComponent;
import org.confluence.mod.common.component.prefix.PrefixType;
import org.confluence.mod.util.PrefixUtils;

import java.util.Collection;
import java.util.Map;

/**
 * 词条属性替换。
 *
 * <p>Confluence 在 {@link ItemAttributeModifierEvent} 里把词条修饰符挂到物品上（默认优先级）。
 * 本监听器以 LOWEST 优先级在之后运行，把枪械词条中“远程伤害 / 攻击速度 / 远程弹速”三项
 * 的修饰符原样移动到 GunsmithLib 的对应属性上，其余（暴击率、击退）保持不变。</p>
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

        for (Map.Entry<Attribute, Collection<AttributeModifier>> entry : prefix.modifiers().get().asMap().entrySet()) {
            Attribute target = mapping.map(entry.getKey());
            if (target == null || target == entry.getKey()) continue;
            for (AttributeModifier modifier : entry.getValue()) {
                if (event.removeModifier(entry.getKey(), modifier)) {
                    event.addModifier(target, modifier);
                }
            }
        }
    }
}
