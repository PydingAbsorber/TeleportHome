package com.pyding.at.mixin;

import com.pyding.at.util.ATUtil;
import com.pyding.at.util.ConfigHandler;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Items.class)
public abstract class ATItemsMixin {

    /*@Inject(method = "registerItem*",at = @At("RETURN"),cancellable = true, require = 1)
    private static void onRegister(ResourceKey<Item> resourceLocation, Item item, CallbackInfoReturnable<Item> cir) {
        //ATUtil.giveBonus(item);
    }*/
}
