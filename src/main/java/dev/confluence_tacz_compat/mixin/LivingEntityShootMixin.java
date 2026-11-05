package dev.confluence_tacz_compat.mixin;

import com.tacz.guns.entity.shooter.LivingEntityShoot;
import dev.confluence_tacz_compat.AmmoSlotItemHandler;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = LivingEntityShoot.class, remap = false)
public abstract class LivingEntityShootMixin {
    /**
     * 只替换物品栏视图，让 TACZ 的 {@code consumeAmmoFromPlayer} 完整跑完原生流程：
     * Confluence 弹药槽排在玩家物品栏之前，其它模组在原生流程之后的补充逻辑不受影响。
     */
    @Redirect(method = "consumeAmmoFromPlayer",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/entity/LivingEntity;getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;"),
            remap = false)
    private <T> LazyOptional<T> confluenceTaczCompat$ammoSlotsFirst(LivingEntity instance, Capability<T> capability,
                                                                   Direction facing) {
        if (capability != ForgeCapabilities.ITEM_HANDLER || !(instance instanceof Player player)) {
            return instance.getCapability(capability, facing);
        }

        IItemHandler vanilla = instance.getCapability(ForgeCapabilities.ITEM_HANDLER, facing).orElse(null);
        return ForgeCapabilities.ITEM_HANDLER.orEmpty(capability,
                LazyOptional.of(() -> AmmoSlotItemHandler.wrap(player, vanilla)));
    }
}
