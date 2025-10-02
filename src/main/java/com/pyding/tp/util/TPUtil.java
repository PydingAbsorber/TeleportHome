package com.pyding.tp.util;

import com.pyding.tp.network.PacketHandler;
import com.pyding.tp.network.packets.ServerToClientSync;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TPUtil {

    public static List<Home> homes = parseInput(ConfigHandler.COMMON.homes.get().toString());

    static class Home {
        String name, owner, path, key;
        int x, y, z;
    }

    public static String addHome(String input, String name, Player owner) {
        List<Home> homes = parseInput(input);
        int homesCreated = 0;
        for(Home home : homes){
            if(owner.getName().getString().equals(home.owner))
                homesCreated++;
        }
        if(homesCreated >= ConfigHandler.COMMON.homesPerPlayer.get() && !owner.isCreative())
            owner.sendSystemMessage(Component.literal("Cannot create more than " + ConfigHandler.COMMON.homesPerPlayer.get() + " homes for you.").withStyle(ChatFormatting.RED));
        else {
            Home newHome = new Home();
            newHome.name = name;
            newHome.owner = owner.getName().getString();
            newHome.path = owner.getCommandSenderWorld().dimension().location().getPath();
            newHome.key = owner.getCommandSenderWorld().dimension().location().getNamespace();
            newHome.x = (int)owner.getX();
            newHome.y = (int)owner.getY();
            newHome.z = (int)owner.getZ();
            homes.add(newHome);
            owner.sendSystemMessage(Component.literal("Home " + name + " was created successfully!").withStyle(ChatFormatting.GREEN));
        }
        return serializehomes(homes);
    }

    public static void teleportHome(String input,ServerPlayer serverPlayer, String name){
        if(ConfigHandler.COMMON.shouldNoHit.get() && wasHitRecently(serverPlayer)) {
            serverPlayer.sendSystemMessage(Component.literal("You shouldn't be hit in " + ConfigHandler.COMMON.hitTime.get()/1000 + " seconds before teleportation.").withStyle(ChatFormatting.RED));
            return;
        }
        List<Home> homes = parseInput(input);
        boolean found = false;
        for(Home home : homes){
            if(home.name.equals(name) && home.owner.equals(serverPlayer.getName().getString())) {
                found = true;
                ServerLevel serverLevel = serverPlayer.getCommandSenderWorld().getServer().getLevel(getWorldKey(home.path,home.key));
                serverPlayer.teleportTo(serverLevel,home.x,home.y,home.z,0,0);
            }
        }
        if(!found)
            serverPlayer.sendSystemMessage(Component.literal("There is no Home with such name and your ownership.").withStyle(ChatFormatting.RED));
    }

    public static String removeHome(String input, String name, Player owner) {
        List<Home> homes = parseInput(input);
        for(Home home : homes){
            if(home.name.equals(name) && home.owner.equals(owner.getName().getString())) {
                homes.remove(home);
                owner.sendSystemMessage(Component.literal("Home " + name + " was deleted successfully!").withStyle(ChatFormatting.GREEN));
                return serializehomes(homes);
            }
        }
        owner.sendSystemMessage(Component.literal("There is no Home with such name and your ownership.").withStyle(ChatFormatting.RED));
        return serializehomes(homes);
    }

    public static List<Home> parseInput(String input) {
        List<Home> homes = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\{(.*?)\\}").matcher(input);

        while (matcher.find()) {
            String obj = matcher.group(1);
            Home e = new Home();
            extractField(obj, "name", value -> e.name = value);
            extractField(obj, "owner", value -> e.owner = value);
            extractField(obj, "path", value -> e.path = value);
            extractField(obj, "key", value -> e.key = value);
            Matcher m = Pattern.compile("cords\\(([^)]+)\\)").matcher(obj);
            if (m.find()) {
                String[] cords = m.group(1).split(",");
                if (cords.length == 3) {
                    e.x = Integer.parseInt(cords[0].trim());
                    e.y = Integer.parseInt(cords[1].trim());
                    e.z = Integer.parseInt(cords[2].trim());
                }
            }
            homes.add(e);
        }
        return homes;
    }

    private static String serializehomes(List<Home> homes) {
        return homes.stream()
                .map(e -> String.format(
                        "{name(%s)owner(%s)path(%s)key(%s)cords(%d,%d,%d)}",
                        e.name,
                        e.owner,
                        e.path,
                        e.key,
                        e.x, e.y, e.z
                ))
                .collect(Collectors.joining(","));
    }

    private static void extractField(String obj, String field, Consumer<String> setter) {
        Matcher m = Pattern.compile(field + "\\(([^)]+)\\)").matcher(obj);
        if (m.find()) setter.accept(m.group(1));
    }

    public static void print(Player owner){
        List<Home> homes = parseInput(ConfigHandler.COMMON.homes.get().toString());
        boolean found = false;
        for(Home home : homes){
            if(owner.getName().getString().equals(home.owner)){
                found = true;
                owner.sendSystemMessage(Component.literal("===============================").withStyle(ChatFormatting.DARK_GRAY));
                owner.sendSystemMessage(Component.literal("Home name: " + home.name).withStyle(ChatFormatting.DARK_PURPLE));
                owner.sendSystemMessage(Component.literal("Home owner: " + home.owner).withStyle(ChatFormatting.DARK_GREEN));
                owner.sendSystemMessage(Component.literal("Home world: " + home.path+":"+home.key).withStyle(ChatFormatting.DARK_GRAY));
                owner.sendSystemMessage(Component.literal("Home cords: x: " + home.x +" y: " + home.y + " z: " + home.z).withStyle(ChatFormatting.GREEN));
                owner.sendSystemMessage(Component.literal("===============================").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
        if(!found)
            owner.sendSystemMessage(Component.literal("You have no homes.").withStyle(ChatFormatting.RED));
    }

    public static void sync(){
        TPUtil.homes = TPUtil.parseInput(ConfigHandler.COMMON.homes.get().toString());
        PacketHandler.sendToClients(PacketDistributor.ALL.noArg(), new ServerToClientSync(ConfigHandler.COMMON.homes.get().toString()));
    }

    public static ResourceKey<Level> getWorldKey(String path, String directory){
        ResourceKey<Level> key = ResourceKey.create(Registries.DIMENSION, new ResourceLocation(directory,path));
        return key;
    }

    public static void teleportSpawn(ServerPlayer serverPlayer){
        String path = "";
        String namespace = "";
        double x = 0,y = 0,z = 0;
        int count = 0;
        for(String element: ConfigHandler.COMMON.spawn.get().toString().split(",")){
            if(count == 0)
                path = element;
            else if(count == 1)
                namespace = element;
            else if(count == 2)
                x = Double.parseDouble(element);
            else if(count == 3)
                y = Double.parseDouble(element);
            else if(count == 4)
                z = Double.parseDouble(element);
            count++;
        }
        ServerLevel serverLevel = serverPlayer.getCommandSenderWorld().getServer().getLevel(getWorldKey(path, namespace));
        serverPlayer.teleportTo(serverLevel,x,y,z,0,0);
    }

    public static void setSpawnPos(Player player){
        String path = player.getCommandSenderWorld().dimension().location().getPath();
        String namespace = player.getCommandSenderWorld().dimension().location().getNamespace();
        ConfigHandler.COMMON.spawn.set(path+","+namespace+","+player.getX()+","+player.getY()+","+player.getZ()+",");
        player.sendSystemMessage(Component.literal("Spawn point was created successfully!").withStyle(ChatFormatting.GREEN));
    }

    public static boolean wasHitRecently(Player player){
        return player.getPersistentData().getLong("TPHit") > System.currentTimeMillis();
    }
}
