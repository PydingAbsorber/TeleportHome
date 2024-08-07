package com.pyding.at.mixin;

import com.pyding.at.capability.PlayerCapabilityProviderVP;
import com.pyding.at.util.ATUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

@Mixin(ICurioItem.class)
public abstract class ATCurioMixin {


    @Inject(method = "canEquip",at = @At("RETURN"),cancellable = true, require = 1, remap = false)
    private void canEquip(String identifier, LivingEntity livingEntity, ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
        int tier = ATUtil.getTier(stack);
        if(livingEntity instanceof Player player){
            player.getCapability(PlayerCapabilityProviderVP.playerCap).ifPresent(cap -> {
               if(tier > cap.getTier(player)) {
                   ci.setReturnValue(false);
               }
            });
        }
    }

    @Inject(method = "curioTick*",at = @At("HEAD"), require = 1, remap = false)
    private void tick(SlotContext slotContext, ItemStack stack, CallbackInfoReturnable ci) {
        int tier = ATUtil.getTier(stack);
        if(slotContext.entity() instanceof Player player){
            player.getCapability(PlayerCapabilityProviderVP.playerCap).ifPresent(cap -> {
                if(tier > cap.getTier(player)) {
                    ci.cancel();
                }
            });
        }
    }

    @Inject(method = "canEquipFromUse",at = @At("HEAD"),cancellable = true, require = 1, remap = false)
    private void clickEquip(SlotContext slotContext, ItemStack stack, CallbackInfoReturnable<Boolean> ci) {
        int tier = ATUtil.getTier(stack);
        if(slotContext.entity() instanceof Player player){
            player.getCapability(PlayerCapabilityProviderVP.playerCap).ifPresent(cap -> {
                if(tier > cap.getTier(player)) {
                    ci.setReturnValue(false);
                }
            });
        }
    }
}
