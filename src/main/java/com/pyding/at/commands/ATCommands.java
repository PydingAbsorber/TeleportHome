package com.pyding.at.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.pyding.at.capability.PlayerCapabilityProviderVP;
import com.pyding.at.util.ATUtil;
import com.pyding.at.util.ConfigHandler;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.synchronization.SuggestionProviders;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.registries.VanillaRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.registries.ForgeRegistries;

import static com.pyding.at.util.ATUtil.*;
import static net.minecraft.commands.Commands.createValidationContext;

public class ATCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tiers")
                .then(Commands.literal("addItem").requires(sender -> sender.hasPermission(2))
                        .then(Commands.literal("hand")
                                .then(Commands.argument("tier", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int tier = IntegerArgumentType.getInteger(context, "tier");
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            ATUtil.giveBonus(player.getMainHandItem().getItem());
                                            String element = tier+"-"+player.getMainHandItem().getDescriptionId()+",";
                                            ATUtil.addItemConfig(element,player);
                                            player.sendSystemMessage(Component.literal("Tier " + tier + " has been set to item in hands"));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("chest")
                                .then(Commands.argument("tier", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int tier = IntegerArgumentType.getInteger(context, "tier");
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            BlockPos pos = ATUtil.getRayBlock(player,3);
                                            if(pos != null && player.getCommandSenderWorld().getBlockEntity(pos) != null && player.getCommandSenderWorld().getBlockEntity(pos) instanceof ChestBlockEntity chest){
                                                for(int i = 0; i < chest.getContainerSize(); i++){
                                                    ItemStack stack = chest.getItem(i);
                                                    ATUtil.giveBonus(stack.getItem());
                                                    String element = tier+"-"+stack.getDescriptionId()+",";
                                                    ATUtil.addItemConfig(element,player);
                                                }
                                                player.sendSystemMessage(Component.literal("Tier " + tier + " has been set to all items in this chest"));
                                            } else player.sendSystemMessage(Component.literal("No chest found :((("));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("removeItem").requires(sender -> sender.hasPermission(2))
                        .then(Commands.literal("hand")
                                .then(Commands.argument("tier", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int tier = IntegerArgumentType.getInteger(context, "tier");
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            String item = tier+"-"+player.getMainHandItem().getDescriptionId();
                                            ATUtil.removeItemConfig(item,player);
                                            player.sendSystemMessage(Component.literal("Tier " + tier + " has been removed from item in hands"));
                                            player.sendSystemMessage(Component.literal("Restart to update item stats from Tiers"));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("chest")
                                .then(Commands.argument("tier", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int tier = IntegerArgumentType.getInteger(context, "tier");
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            BlockPos pos = ATUtil.getRayBlock(player,3);
                                            if(pos != null && player.getCommandSenderWorld().getBlockEntity(pos) != null && player.getCommandSenderWorld().getBlockEntity(pos) instanceof ChestBlockEntity chest){
                                                for(int i = 0; i < chest.getContainerSize(); i++){
                                                    ItemStack stack = chest.getItem(i);
                                                    String item = tier+"-"+stack.getDescriptionId();
                                                    ATUtil.removeItemConfig(item,player);
                                                }
                                                player.sendSystemMessage(Component.literal("Tier " + tier + " has been removed from all items in this chest"));
                                            } else player.sendSystemMessage(Component.literal("No chest found :((("));
                                            player.sendSystemMessage(Component.literal("Restart to update item stats from Tiers"));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("all")
                                .then(Commands.argument("tier", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int tier = IntegerArgumentType.getInteger(context, "tier");
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            for(String element: ATUtil.getItemsWithTier(tier)){
                                                String item = tier+"-"+element;
                                                ATUtil.removeItemConfig(item,player);
                                            }
                                            player.sendSystemMessage(Component.literal("Tier " + tier + " has been removed from item in hands"));
                                            player.sendSystemMessage(Component.literal("Restart to update item stats from Tiers"));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("addTierPlayer").requires(sender -> sender.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> {
                                    ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                    player.getCapability(PlayerCapabilityProviderVP.playerCap).ifPresent(cap -> {
                                        cap.addTier(player);
                                        cap.setExp(player,ATUtil.getMaxExp(cap.getTier(player)-1));
                                    });
                                    player.sendSystemMessage(Component.literal("Your Tier increased"));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("setTierPlayer").requires(sender -> sender.hasPermission(2))
                        .then(Commands.argument("player", EntityArgument.player())
                                .then(Commands.argument("tier", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int tier = IntegerArgumentType.getInteger(context, "tier");
                                            ServerPlayer player = EntityArgument.getPlayer(context, "player");
                                            player.getCapability(PlayerCapabilityProviderVP.playerCap).ifPresent(cap -> {
                                                cap.setTier(player,tier);
                                                cap.setExp(player,ATUtil.getMaxExp(tier-1));
                                            });
                                            player.sendSystemMessage(Component.literal("You have " + tier + " Tier now"));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("addEntity").requires(sender -> sender.hasPermission(2))
                        .then(Commands.literal("byRaytrace")
                                .then(Commands.argument("tier", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            int tier = IntegerArgumentType.getInteger(context, "tier");
                                            boolean found = false;
                                            for(LivingEntity entity: ATUtil.ray(player,3,60,true)){
                                                String name = tier+"-"+entity.getType().getDescriptionId()+",";
                                                ATUtil.addEntityConfig(name,player);
                                                found = true;
                                                player.sendSystemMessage(Component.literal("Entity "+ entity + "is now Tier: " + tier));
                                            }
                                            if(!found) {
                                                player.sendSystemMessage(Component.literal("No creatures found. You should look at creature."));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("eggInHand")
                                .then(Commands.argument("tier", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int tier = IntegerArgumentType.getInteger(context, "tier");
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            ItemStack stack = player.getMainHandItem();
                                            if(stack.getItem() instanceof SpawnEggItem egg){
                                                String element = tier+"-"+egg.getType(egg.getShareTag(stack))+",";
                                                ATUtil.addEntityConfig(element,player);
                                                player.sendSystemMessage(Component.literal("Tier " + tier + " has been set to " +egg.getType(egg.getShareTag(stack))));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("chest")
                                .then(Commands.argument("tier", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int tier = IntegerArgumentType.getInteger(context, "tier");
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            BlockPos pos = ATUtil.getRayBlock(player,3);
                                            if(pos != null && player.getCommandSenderWorld().getBlockEntity(pos) != null && player.getCommandSenderWorld().getBlockEntity(pos) instanceof ChestBlockEntity chest){
                                                for(int i = 0; i < chest.getContainerSize(); i++){
                                                    ItemStack stack = chest.getItem(i);
                                                    if(stack.getItem() instanceof SpawnEggItem egg){
                                                        String element = tier+"-"+egg.getType(egg.getShareTag(stack))+",";
                                                        ATUtil.addEntityConfig(element,player);
                                                        player.sendSystemMessage(Component.literal("Tier " + tier + " has been set to " +egg.getType(egg.getShareTag(stack))));
                                                    }
                                                }
                                                player.sendSystemMessage(Component.literal("Tier " + tier + " has been set to all items in this chest"));
                                            } else player.sendSystemMessage(Component.literal("No chest found :((("));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("auto")
                                .then(Commands.argument("maxHealth", IntegerArgumentType.integer())
                                    .executes(context -> {
                                        int health = IntegerArgumentType.getInteger(context, "tier");
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        for(EntityType<?> type: ForgeRegistries.ENTITY_TYPES.getValues()){
                                            Entity entity = type.create(player.getCommandSenderWorld());
                                            if (entity instanceof LivingEntity livingEntity) {
                                                int tier = 10;
                                                while (tier > 0){
                                                    if(livingEntity.getMaxHealth() < health) {
                                                        health -= 20;
                                                        tier -= 1;
                                                    } else {
                                                        String element = tier+"-"+type+",";
                                                        ATUtil.addEntityConfig(element,player);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                        player.sendSystemMessage(Component.literal("I hope all worked fine..."));
                                        return Command.SINGLE_SUCCESS;
                                    })
                                )
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    for(EntityType<?> type: ForgeRegistries.ENTITY_TYPES.getValues()){
                                        Entity entity = type.create(player.getCommandSenderWorld());
                                        if (entity instanceof LivingEntity livingEntity) {
                                            int health = 180;
                                            int tier = 10;
                                            while (tier > 0){
                                                if(livingEntity.getMaxHealth() < health) {
                                                    health -= 20;
                                                    tier -= 1;
                                                } else {
                                                    String element = tier+"-"+type+",";
                                                    ATUtil.addEntityConfig(element,player);
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    player.sendSystemMessage(Component.literal("I hope all worked fine..."));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("removeEntity").requires(sender -> sender.hasPermission(2))
                        .then(Commands.literal("byRaytrace")
                                .then(Commands.argument("tier", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            int tier = IntegerArgumentType.getInteger(context, "tier");
                                            boolean found = false;
                                            for(LivingEntity entity: ATUtil.ray(player,3,60,true)){
                                                String name = tier+"-"+entity.getType().getDescriptionId()+",";
                                                ATUtil.removeEntityConfig(name,player);
                                                found = true;
                                                player.sendSystemMessage(Component.literal("Entity "+ entity + "is removed from Tier: " + tier));
                                            }
                                            if(!found) {
                                                player.sendSystemMessage(Component.literal("No creatures found. You should look at creature."));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("eggInHand")
                                .then(Commands.argument("tier", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int tier = IntegerArgumentType.getInteger(context, "tier");
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            ItemStack stack = player.getMainHandItem();
                                            if(stack.getItem() instanceof SpawnEggItem egg){
                                                String element = tier+"-"+egg.getType(egg.getShareTag(stack))+",";
                                                ATUtil.removeEntityConfig(element,player);
                                                player.sendSystemMessage(Component.literal("Tier " + tier + " removed from " +egg.getType(egg.getShareTag(stack))));
                                            }
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                        .then(Commands.literal("chest")
                                .then(Commands.argument("tier", IntegerArgumentType.integer())
                                        .executes(context -> {
                                            int tier = IntegerArgumentType.getInteger(context, "tier");
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            BlockPos pos = ATUtil.getRayBlock(player,3);
                                            if(pos != null && player.getCommandSenderWorld().getBlockEntity(pos) != null && player.getCommandSenderWorld().getBlockEntity(pos) instanceof ChestBlockEntity chest){
                                                for(int i = 0; i < chest.getContainerSize(); i++){
                                                    ItemStack stack = chest.getItem(i);
                                                    if(stack.getItem() instanceof SpawnEggItem egg){
                                                        String element = tier+"-"+egg.getType(egg.getShareTag(stack))+",";
                                                        ATUtil.removeEntityConfig(element,player);
                                                        player.sendSystemMessage(Component.literal("Tier " + tier + " removed from " +egg.getType(egg.getShareTag(stack))));
                                                    }
                                                }
                                                player.sendSystemMessage(Component.literal("Tier " + tier + " has been removed from all items in this chest"));
                                            } else player.sendSystemMessage(Component.literal("No chest found :((("));
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("getIdStats")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ItemStack stack = player.getMainHandItem();
                            player.sendSystemMessage(Component.literal("damage "+stack.getMaxDamage()));
                            if(stack.getItem() instanceof ArmorItem armor){
                                player.sendSystemMessage(Component.literal("defence "+armor.getDefense()));
                                player.sendSystemMessage(Component.literal("toughness "+armor.getToughness()));
                            }
                            if(stack.getItem() instanceof SwordItem sword){
                                player.sendSystemMessage(Component.literal("attack "+sword.getDamage()));
                                player.sendSystemMessage(Component.literal("speed "+ATUtil.getAttackSpeed(stack)));
                            }
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("makeIgnore").requires(sender -> sender.hasPermission(2))
                        .then(Commands.literal("hand")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ItemStack stack = player.getMainHandItem();
                                    stack.getOrCreateTag().putBoolean("ATIgnore",true);
                                    player.sendSystemMessage(Component.literal("Stonks time!"));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                        .then(Commands.literal("chest")
                                .executes(context -> {
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    BlockPos pos = ATUtil.getRayBlock(player,3);
                                    if(pos != null && player.getCommandSenderWorld().getBlockEntity(pos) != null && player.getCommandSenderWorld().getBlockEntity(pos) instanceof ChestBlockEntity chest){
                                        for(int i = 0; i < chest.getContainerSize(); i++){
                                            ItemStack stack = chest.getItem(i);
                                            stack.getOrCreateTag().putBoolean("ATIgnore",true);
                                            player.sendSystemMessage(Component.literal("Stonks time!"));
                                        }
                                    } else player.sendSystemMessage(Component.literal("No chest found :((("));
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("autoTiersItems")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ATUtil.addItemConfig(ATUtil.getBaseTiers(),player);
                            player.sendSystemMessage(Component.literal("Tiers for a lot of Items applied automatically"));
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("clearItemTiers")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ConfigHandler.COMMON.itemTiers.set("");
                            itemTiers.clear();
                            initMaps(player);
                            player.sendSystemMessage(Component.literal("Item Tiers cleared"));
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("clearEntityTiers")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ConfigHandler.COMMON.entityTiers.set("");
                            entityTiers.clear();
                            initMaps(player);
                            player.sendSystemMessage(Component.literal("Item Tiers cleared"));
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }
}
