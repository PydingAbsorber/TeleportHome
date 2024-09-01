package com.pyding.at.compat;

import com.pyding.at.util.ATUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.ArrayList;
import java.util.List;

public class ATCurio {

    public static List<ItemStack> getCurioList(Player player){
        List<SlotResult> result = new ArrayList<>();
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            result.addAll(handler.findCurios(itemStack -> itemStack.getItem() instanceof ICurioItem));
        });
        List<ItemStack> stacks = new ArrayList<>();
        for(SlotResult hitResult: result){
            stacks.add(hitResult.stack());
        }
        return stacks;
    }

    public static void dropCurios(Player player, int tier){
        CuriosApi.getCuriosInventory(player).ifPresent(handler -> {
            for (SlotResult curio : handler.findCurios(itemStack -> itemStack.getItem() instanceof ICurioItem)) {
                if (ATUtil.getTier(curio.stack()) > tier && ATUtil.notIgnored(curio.stack())) {
                    ItemStack stack = new ItemStack(curio.stack().getItem());
                    player.drop(stack, true);
                    curio.stack().setCount(0);
                }
            }
        });
    }

}
