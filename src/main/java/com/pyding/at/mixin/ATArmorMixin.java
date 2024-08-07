package com.pyding.at.mixin;

import com.google.common.collect.Multimap;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.SwordItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.EnumMap;
import java.util.UUID;


@Mixin(ArmorItem.class)
public interface ATArmorMixin {

    @Accessor("toughness")
    @Mutable
    public void setToughness(float toughness);

    @Accessor("defense")
    @Mutable
    public void setDefence(int defence);

    @Accessor("ARMOR_MODIFIER_UUID_PER_TYPE")
    @Mutable
    public EnumMap<ArmorItem.Type, UUID> getModifiers();

    @Accessor("defaultModifiers")
    @Mutable
    void setMap(Multimap<Attribute, AttributeModifier> map);
}
