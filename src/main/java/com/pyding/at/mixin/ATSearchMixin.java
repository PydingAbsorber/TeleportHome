package com.pyding.at.mixin;

import com.pyding.at.util.ATUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
@OnlyIn(Dist.CLIENT)
@Mixin(CreativeModeInventoryScreen.class)
public abstract class ATSearchMixin {

    @Shadow private static CreativeModeTab selectedTab;

    @Shadow @Final private Set<TagKey<Item>> visibleTags;

    @Shadow private EditBox searchBox;

    @Shadow private float scrollOffs;

    @Inject(method = "refreshSearchResults",at = @At("HEAD"),cancellable = true, require = 1)
    private void refreshSearchResults(CallbackInfo cir) {
        if (!selectedTab.hasSearchBar()) return;
        String s = searchBox.getValue();
        String filter = Component.translatable("at.tier.lang").getString();
        String discovered = Component.translatable("at.discovered.lang").getString();
        if(s.contains(filter)) {
            int tier = ATUtil.extractTier(s,filter);
            visibleTags.clear();
            if (((ATMenuMixin) this).getMenu() instanceof CreativeModeInventoryScreen.ItemPickerMenu menu) {
                menu.items.clear();
                menu.items.addAll(ATUtil.getStacksWithTier(tier));
                scrollOffs = 0.0F;
                menu.scrollTo(0.0F);
                cir.cancel();
            }
        }
        else if(s.contains(discovered) && Minecraft.getInstance().player != null) {
            int tier = ATUtil.extractTier(s,discovered);
            visibleTags.clear();
            if (((ATMenuMixin) this).getMenu() instanceof CreativeModeInventoryScreen.ItemPickerMenu menu) {
                menu.items.clear();
                menu.items.addAll(ATUtil.getStacksWithTierLeft(tier, Minecraft.getInstance().player));
                scrollOffs = 0.0F;
                menu.scrollTo(0.0F);
                cir.cancel();
            }
        }
    }
}
