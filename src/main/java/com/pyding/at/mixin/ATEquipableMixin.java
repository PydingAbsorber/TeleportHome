package com.pyding.at.mixin;

import com.pyding.at.capability.PlayerCapabilityProviderVP;
import com.pyding.at.util.ATUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

@Mixin(Equipable.class)
public abstract class ATEquipableMixin {


    @Inject(method = "swapWithEquipmentSlot*",at = @At("RETURN"),cancellable = true, require = 1)
    private void canUnequip(Item item, Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        int tier = ATUtil.getTier(item);
        player.getCapability(PlayerCapabilityProviderVP.playerCap).ifPresent(cap -> {
           if(tier > cap.getTier(player)) {
               cir.setReturnValue(InteractionResultHolder.fail(player.getItemInHand(hand)));
           }
        });
    }
}
