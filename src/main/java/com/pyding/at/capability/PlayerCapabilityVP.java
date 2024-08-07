package com.pyding.at.capability;

import com.pyding.at.network.PacketHandler;
import com.pyding.at.network.packets.SendPlayerCapaToClient;
import com.pyding.at.util.ATUtil;
import com.pyding.at.util.ConfigHandler;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.AutoRegisterCapability;

@AutoRegisterCapability
public class PlayerCapabilityVP {
    private int tier = 1;
    private int exp = 0;
    private String items = "";

    public void addItem(ItemStack stack, Player player){
        int rank = ATUtil.getTier(stack);
        if(rank > 0) {
            String name = stack.getDescriptionId();
            if (ATUtil.notContains(items, name)) {
                items += name + ",";
                exp += rank;
                sync(player);
            }
        }
    }

    public String getItems(){
        return items;
    }

    public void addTier(Player player){
        tier = Math.min(ConfigHandler.COMMON.maxTier.get(),tier+1);
        if(!player.getCommandSenderWorld().isClientSide)
            player.sendSystemMessage(Component.translatable("at.give",tier));
        sync(player);
    }

    public int getTier(Player player){
        if(player.isCreative())
            return ConfigHandler.COMMON.maxTier.get();
        return tier;
    }

    public void setTier(Player player, int amount){
        tier = amount;
        sync(player);
    }

    public void addExp(Player player){
        exp += 1;
        sync(player);
    }

    public int getExp(){
        return exp;
    }

    public void setExp(Player player, int amount){
        exp = amount;
        sync(player);
    }

    public void copyNBT(PlayerCapabilityVP source){
        tier = source.tier;
        exp = source.exp;
        items = source.items;
    }

    public void loadNBT(CompoundTag nbt){
        tier = nbt.getInt("ATTiers");
        exp = nbt.getInt("ATExp");
        items = nbt.getString("ATItems");
    }

    public void saveNBT(CompoundTag nbt){
        nbt.putInt("ATTiers",tier);
        nbt.putInt("ATExp",exp);
        nbt.putString("ATItems",items);
    }

    public CompoundTag getNbt(){
        CompoundTag nbt = new CompoundTag();
        nbt.putInt("ATTiers",tier);
        nbt.putInt("ATExp",exp);
        nbt.putString("ATItems",items);
        return nbt;
    }

    public void sync(Player player){
        if(player.getCommandSenderWorld().isClientSide)
            return;
        ServerPlayer serverPlayer = (ServerPlayer) player;
        PacketHandler.sendToClient(new SendPlayerCapaToClient(this.getNbt()),serverPlayer);
    }
}
