package com.pyding.tp.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.pyding.tp.util.ConfigHandler;
import com.pyding.tp.util.TPUtil;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class TPCommands {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("sethome")
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ConfigHandler.COMMON.homes.set(TPUtil.addHome(ConfigHandler.COMMON.homes.get().toString(),name,player));
                            TPUtil.sync();
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
        dispatcher.register(Commands.literal("home")
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            TPUtil.teleportHome(ConfigHandler.COMMON.homes.get().toString(),player,name);
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
        dispatcher.register(Commands.literal("homes")
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    TPUtil.print(player);
                    TPUtil.sync();
                    return Command.SINGLE_SUCCESS;
                })
        );
        dispatcher.register(Commands.literal("removehome")
                .then(Commands.argument("name", StringArgumentType.string())
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            ServerPlayer player = context.getSource().getPlayerOrException();
                            ConfigHandler.COMMON.homes.set(TPUtil.removeHome(ConfigHandler.COMMON.homes.get().toString(),name,player));
                            TPUtil.sync();
                            return Command.SINGLE_SUCCESS;
                        })
                )
        );
        dispatcher.register(Commands.literal("spawn")
            .executes(context -> {
                ServerPlayer player = context.getSource().getPlayerOrException();
                TPUtil.teleportSpawn(player);
                return Command.SINGLE_SUCCESS;
            })
        );
        dispatcher.register(Commands.literal("setSpawn").requires(sender -> sender.hasPermission(2))
                .executes(context -> {
                    ServerPlayer player = context.getSource().getPlayerOrException();
                    TPUtil.setSpawnPos(player);
                    TPUtil.sync();
                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}
