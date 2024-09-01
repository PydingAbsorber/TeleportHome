package com.pyding.at.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;


@Mixin(AbstractContainerScreen.class)
public interface ATMenuMixin<T extends AbstractContainerMenu> {

    @Accessor("menu")
    @Mutable
    T getMenu();
}
