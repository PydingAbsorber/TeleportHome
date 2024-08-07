package com.pyding.at.mixin;

import com.pyding.at.capability.PlayerCapabilityProviderVP;
import com.pyding.at.util.ATUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(IForgeItemStack.class)
public abstract class ATEquipMixin {


    @Shadow protected abstract ItemStack self();

    @Inject(method = "canEquip",at = @At("RETURN"),cancellable = true, require = 1, remap = false)
    private void canUnequip(EquipmentSlot armorType, Entity entity, CallbackInfoReturnable<Boolean> cir) {
        int tier = ATUtil.getTier(this.self());
        if(entity instanceof Player player) {
            player.getCapability(PlayerCapabilityProviderVP.playerCap).ifPresent(cap -> {
                if (tier > cap.getTier(player)) {
                    cir.setReturnValue(false);
                }
            });
        }
    }
}
