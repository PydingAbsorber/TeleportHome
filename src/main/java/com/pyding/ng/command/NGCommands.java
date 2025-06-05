package com.pyding.ng.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pyding.ng.event.EventHandler;
import com.pyding.ng.util.ConfigHandler;
import com.pyding.ng.util.ZoneUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class NGCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("nogrief")
                .then(Commands.literal("createZone")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    String name = StringArgumentType.getString(context, "name");
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    BlockPos pos1 = EventHandler.zoneMarks.get(player).get(0);
                                    BlockPos pos2 = EventHandler.zoneMarks.get(player).get(1);
                                    if(pos1 == null){
                                        player.sendSystemMessage(Component.literal("Fist position wasn't set."));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    if(pos2 == null){
                                        player.sendSystemMessage(Component.literal("Second position wasn't set."));
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    ConfigHandler.COMMON.zones.set(ZoneUtil.addNewZone(ConfigHandler.COMMON.zones.get().toString(),name,player, pos1.getX(), pos1.getZ(), pos2.getX(), pos2.getZ()));
                                    ZoneUtil.sync();
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("deleteZone")
                        .then(Commands.argument("name", StringArgumentType.string())
                                .executes(context -> {
                                    String name  =StringArgumentType.getString(context, "name");
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ConfigHandler.COMMON.zones.set(ZoneUtil.removeZone(ConfigHandler.COMMON.zones.get().toString(),name,player));
                                    ZoneUtil.sync();
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("addMember")
                        .then(Commands.argument("zoneName", StringArgumentType.string())
                                .then(Commands.argument("playerName", StringArgumentType.string())
                                    .executes(context -> {
                                        String zoneName = StringArgumentType.getString(context, "zoneName");
                                        String playerName =StringArgumentType.getString(context, "playerName");
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        ConfigHandler.COMMON.zones.set(ZoneUtil.modifyMember(ConfigHandler.COMMON.zones.get().toString(),zoneName,player,playerName,true));
                                        ZoneUtil.sync();
                                        return Command.SINGLE_SUCCESS;
                                    })
                                )
                        )
                )
                .then(Commands.literal("removeMember")
                        .then(Commands.argument("zoneName", StringArgumentType.string())
                                .then(Commands.argument("playerName", StringArgumentType.string())
                                        .executes(context -> {
                                            String zoneName = StringArgumentType.getString(context, "zoneName");
                                            String playerName =StringArgumentType.getString(context, "playerName");
                                            ServerPlayer player = context.getSource().getPlayerOrException();
                                            ConfigHandler.COMMON.zones.set(ZoneUtil.modifyMember(ConfigHandler.COMMON.zones.get().toString(),zoneName,player,playerName,false));
                                            ZoneUtil.sync();
                                            return Command.SINGLE_SUCCESS;
                                        })
                                )
                        )
                )
                .then(Commands.literal("myZones")
                        .executes(context -> {
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ZoneUtil.print(player);
                            return Command.SINGLE_SUCCESS;
                        })
                )
                .then(Commands.literal("enableStrict")
                        .then(Commands.argument("zoneName", StringArgumentType.string())
                                .executes(context -> {
                                    String zoneName = StringArgumentType.getString(context, "zoneName");
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ConfigHandler.COMMON.zones.set(ZoneUtil.setStrict(ConfigHandler.COMMON.zones.get().toString(),zoneName,player,true));
                                    ZoneUtil.sync();
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("disableStrict")
                        .then(Commands.argument("zoneName", StringArgumentType.string())
                                .executes(context -> {
                                    String zoneName = StringArgumentType.getString(context, "zoneName");
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ConfigHandler.COMMON.zones.set(ZoneUtil.setStrict(ConfigHandler.COMMON.zones.get().toString(),zoneName,player,false));
                                    ZoneUtil.sync();
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("enableInteract")
                        .then(Commands.argument("zoneName", StringArgumentType.string())
                                .executes(context -> {
                                    String zoneName = StringArgumentType.getString(context, "zoneName");
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ConfigHandler.COMMON.zones.set(ZoneUtil.setInteract(ConfigHandler.COMMON.zones.get().toString(),zoneName,player,true));
                                    ZoneUtil.sync();
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("disableInteract")
                        .then(Commands.argument("zoneName", StringArgumentType.string())
                                .executes(context -> {
                                    String zoneName = StringArgumentType.getString(context, "zoneName");
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ConfigHandler.COMMON.zones.set(ZoneUtil.setInteract(ConfigHandler.COMMON.zones.get().toString(),zoneName,player,false));
                                    ZoneUtil.sync();
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("enableMobSpawn")
                        .then(Commands.argument("zoneName", StringArgumentType.string())
                                .executes(context -> {
                                    String zoneName = StringArgumentType.getString(context, "zoneName");
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ConfigHandler.COMMON.zones.set(ZoneUtil.setMobSpawn(ConfigHandler.COMMON.zones.get().toString(),zoneName,player,true));
                                    ZoneUtil.sync();
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("disableMobSpawn")
                        .then(Commands.argument("zoneName", StringArgumentType.string())
                                .executes(context -> {
                                    String zoneName = StringArgumentType.getString(context, "zoneName");
                                    ServerPlayer player = context.getSource().getPlayerOrException();
                                    ConfigHandler.COMMON.zones.set(ZoneUtil.setMobSpawn(ConfigHandler.COMMON.zones.get().toString(),zoneName,player,false));
                                    ZoneUtil.sync();
                                    return Command.SINGLE_SUCCESS;
                                })
                        )
                )
                .then(Commands.literal("sync").requires(sender -> sender.hasPermission(2))
                        .executes(context -> {
                            ZoneUtil.sync();
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
    }
}
