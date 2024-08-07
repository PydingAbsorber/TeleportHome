package com.pyding.at.mixin;

import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(Item.class)
public interface ATItemMixin {

    @Accessor("maxDamage")
    @Mutable
    public void setDurability(int toughness);

}
