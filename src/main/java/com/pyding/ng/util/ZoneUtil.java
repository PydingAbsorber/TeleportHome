package com.pyding.ng.util;

import com.pyding.ng.network.PacketHandler;
import com.pyding.ng.network.packets.ServerToClientSync;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ZoneUtil {

    public static List<Zone> zones = parseInput(ConfigHandler.COMMON.zones.get().toString());

    public static boolean canInteract(LivingEntity entity, BlockPos pos){
        int x = pos.getX();
        int z = pos.getZ();
        for (Zone zone : zones) {
            if((isBetween(x, zone.x1, zone.x2) && isBetween(z, zone.z1, zone.z2))) {
                return entity.getName().getString().equals(zone.owner) || zone.members.contains(entity.getName().getString());
            }
        }
        return true;
    }

    public static boolean canUse(LivingEntity entity, BlockPos pos){
        int x = pos.getX();
        int z = pos.getZ();
        for (Zone zone : zones) {
            if((isBetween(x, zone.x1, zone.x2) && isBetween(z, zone.z1, zone.z2)) && !zone.interact) {
                return entity.getName().getString().equals(zone.owner) || zone.members.contains(entity.getName().getString());
            }
        }
        return true;
    }

    public static boolean isInZone(BlockPos pos){
        int x = pos.getX();
        int z = pos.getZ();
        for (Zone zone : zones) {
            if((isBetween(x, zone.x1, zone.x2) && isBetween(z, zone.z1, zone.z2))) {
                return true;
            }
        }
        return false;
    }

    public static boolean isInStrictZone(BlockPos pos){
        int x = pos.getX();
        int z = pos.getZ();
        for (Zone zone : zones) {
            if((isBetween(x, zone.x1, zone.x2) && isBetween(z, zone.z1, zone.z2)) && zone.strict) {
                return true;
            }
        }
        return false;
    }

    public static boolean canSetBlock(BlockPos pos, BlockState state, Level level){
        int x = pos.getX();
        int z = pos.getZ();
        for (Zone zone : zones) {
            if((isBetween(x, zone.x1, zone.x2) && isBetween(z, zone.z1, zone.z2)) && zone.strict) {
                return zone.interact && level.getBlockState(pos).is(state.getBlock());
            }
        }
        return true;
    }

    public static boolean canSpawn(BlockPos pos){
        int x = pos.getX();
        int z = pos.getZ();
        for (Zone zone : zones) {
            if((isBetween(x, zone.x1, zone.x2) && isBetween(z, zone.z1, zone.z2)) && !zone.mobSpawn) {
                return false;
            }
        }
        return true;
    }

    public static boolean isBetween(int x, int x1, int x2) {
        int lowerBound = Math.min(x1, x2);
        int upperBound = Math.max(x1, x2);
        return x >= lowerBound && x <= upperBound;
    }

    public static Zone overlap(List<Zone> zones, int x1, int z1, int x2, int z2) {
        int rectXMin = Math.min(x1, x2);
        int rectXMax = Math.max(x1, x2);
        int rectZMin = Math.min(z1, z2);
        int rectZMax = Math.max(z1, z2);
        for(Zone zone : zones) {
            int zoneXMin = Math.min(zone.x1, zone.x2);
            int zoneXMax = Math.max(zone.x1, zone.x2);
            int zoneZMin = Math.min(zone.z1, zone.z2);
            int zoneZMax = Math.max(zone.z1, zone.z2);
            boolean xOverlap = (rectXMin <= zoneXMax) && (rectXMax >= zoneXMin);
            boolean zOverlap = (rectZMin <= zoneZMax) && (rectZMax >= zoneZMin);
            if(xOverlap && zOverlap) {
                return zone;
            }
        }
        return null;
    }

    public static double calculateDistance(int x1, int z1, int x2, int z2) {
        int deltaX = x2 - x1;
        int deltaZ = z2 - z1;
        return Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);
    }

    static class Zone {
        String name;
        String owner;
        List<String> members = new ArrayList<>();
        int x1, z1, x2, z2;
        boolean strict = false;
        boolean interact = false;
        boolean mobSpawn = true;
    }

    public static String addNewZone(String input, String name, Player owner, int x1, int z1, int x2, int z2) {
        List<Zone> zones = parseInput(input);
        boolean nameExists = zones.stream().anyMatch(e -> e.name.equals(name));
        int zonesCreated = 0;
        for(Zone zone: zones){
            if(owner.getName().getString().equals(zone.owner))
                zonesCreated++;
        }
        Zone overlap = overlap(zones,x1,z1,x2,z2);
        double range = calculateDistance(x1,z1,x2,z2);
        if(nameExists)
            owner.sendSystemMessage(Component.literal("Zone with that name already exists.").withStyle(ChatFormatting.RED));
        else if(zonesCreated >= ConfigHandler.COMMON.zonesPerPlayer.get() && !owner.isCreative())
            owner.sendSystemMessage(Component.literal("Cannot create more than" + ConfigHandler.COMMON.zonesPerPlayer.get() + "zone for you.").withStyle(ChatFormatting.RED));
        else if(overlap != null) {
            owner.sendSystemMessage(Component.literal("Zone overlap with another zone:").withStyle(ChatFormatting.RED));
            owner.sendSystemMessage(Component.literal("Zone name: " + overlap.name).withStyle(ChatFormatting.DARK_GRAY));
            owner.sendSystemMessage(Component.literal("Zone owner: " + overlap.owner).withStyle(ChatFormatting.DARK_GRAY));
            owner.sendSystemMessage(Component.literal("Zone cords: x1: " + overlap.x1 + " z1: " + overlap.z1 + " x2: " + overlap.x2 + " z2: " + overlap.z2).withStyle(ChatFormatting.DARK_GRAY));
        }
        else if(range > ConfigHandler.COMMON.zoneMaximum.get() && !owner.isCreative())
            owner.sendSystemMessage(Component.literal("Current Zone range: " + range + " exceeds maximum config's range: " + ConfigHandler.COMMON.zoneMaximum.get()).withStyle(ChatFormatting.RED));
        else {
            Zone newZone = new Zone();
            newZone.name = name;
            newZone.owner = owner.getName().getString();
            newZone.x1 = x1;
            newZone.z1 = z1;
            newZone.x2 = x2;
            newZone.z2 = z2;
            zones.add(newZone);
            owner.sendSystemMessage(Component.literal("Zone " + name + " was created successfully!").withStyle(ChatFormatting.GREEN));
        }

        return serializeZones(zones);
    }

    public static String modifyMember(String input, String name, Player owner, String member, boolean add) {
        List<Zone> entities = parseInput(input);

        entities.stream()
                .filter(e -> e.name.equals(name) && e.owner.equals(owner.getName().getString()))
                .findFirst()
                .ifPresent(e -> {
                    if (add && !e.members.contains(member)) {
                        e.members.add(member);
                        owner.sendSystemMessage(Component.literal("Player " + member + " was added to Zone " + name + " successfully!"));
                    } else if (!add) {
                        e.members.remove(member);
                        owner.sendSystemMessage(Component.literal("Player " + member + " was removed from Zone " + name + " successfully!"));
                    }
                });

        return serializeZones(entities);
    }

    public static String removeZone(String input, String name, Player owner) {
        List<Zone> zones = parseInput(input);
        for(Zone zone: zones){
            if(zone.name.equals(name) && zone.owner.equals(owner.getName().getString())) {
                zones.remove(zone);
                owner.sendSystemMessage(Component.literal("Zone " + name + " was deleted successfully!").withStyle(ChatFormatting.GREEN));
                return serializeZones(zones);
            }
        }
        owner.sendSystemMessage(Component.literal("There is no Zone with such name and your ownership.").withStyle(ChatFormatting.RED));
        return serializeZones(zones);
    }

    public static String setStrict(String input, String name, Player owner, boolean strict) {
        List<Zone> zones = parseInput(input);

        zones.stream()
                .filter(e -> e.name.equals(name) && e.owner.equals(owner.getName().getString()))
                .findFirst()
                .ifPresent(e -> {
                    e.strict = strict;
                    owner.sendSystemMessage(Component.literal("Strict Zone is now " + strict));
                });

        return serializeZones(zones);
    }

    public static String setInteract(String input, String name, Player owner, boolean interact) {
        List<Zone> zones = parseInput(input);

        zones.stream()
                .filter(e -> e.name.equals(name) && e.owner.equals(owner.getName().getString()))
                .findFirst()
                .ifPresent(e -> {
                    e.interact = interact;
                    owner.sendSystemMessage(Component.literal("Interact in Zone is now " + interact));
                });

        return serializeZones(zones);
    }

    public static String setMobSpawn(String input, String name, Player owner, boolean mobSpawn) {
        List<Zone> zones = parseInput(input);

        zones.stream()
                .filter(e -> e.name.equals(name) && e.owner.equals(owner.getName().getString()))
                .findFirst()
                .ifPresent(e -> {
                    e.mobSpawn = mobSpawn;
                    owner.sendSystemMessage(Component.literal("Mob Spawning in Zone is now " + mobSpawn));
                });

        return serializeZones(zones);
    }

    public static List<Zone> parseInput(String input) {
        List<Zone> zones = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\{(.*?)\\}").matcher(input);

        while (matcher.find()) {
            String obj = matcher.group(1);
            Zone e = new Zone();

            extractField(obj, "name", value -> e.name = value);
            extractField(obj, "owner", value -> e.owner = value);

            Matcher m = Pattern.compile("members\\(([^)]*)\\)").matcher(obj);
            if (m.find()) {
                e.members = Arrays.stream(m.group(1).split(","))
                        .filter(s -> !s.isEmpty())
                        .collect(Collectors.toList());
            }

            m = Pattern.compile("cords\\(([^)]+)\\)").matcher(obj);
            if (m.find()) {
                String[] cords = m.group(1).split(",");
                if (cords.length == 4) {
                    e.x1 = Integer.parseInt(cords[0].trim());
                    e.z1 = Integer.parseInt(cords[1].trim());
                    e.x2 = Integer.parseInt(cords[2].trim());
                    e.z2 = Integer.parseInt(cords[3].trim());
                }
            }

            m = Pattern.compile("strict\\(([^)]+)\\)").matcher(obj);
            if(m.find()){
                e.strict = Boolean.parseBoolean(m.group(1));
            }

            m = Pattern.compile("interact\\(([^)]+)\\)").matcher(obj);
            if(m.find()){
                e.interact = Boolean.parseBoolean(m.group(1));
            }

            m = Pattern.compile("mobSpawn\\(([^)]+)\\)").matcher(obj);
            if(m.find()){
                e.mobSpawn = Boolean.parseBoolean(m.group(1));
            }

            zones.add(e);
        }
        return zones;
    }

    private static String serializeZones(List<Zone> zones) {
        return zones.stream()
                .map(e -> String.format(
                        "{name(%s)owner(%s)members(%s)cords(%d,%d,%d,%d)strict(%b)interact(%b)mobSpawn(%b)}",
                        e.name,
                        e.owner,
                        String.join(",", e.members),
                        e.x1, e.z1, e.x2, e.z2,
                        e.strict,
                        e.interact,
                        e.mobSpawn
                ))
                .collect(Collectors.joining(","));
    }

    private static void extractField(String obj, String field, Consumer<String> setter) {
        Matcher m = Pattern.compile(field + "\\(([^)]+)\\)").matcher(obj);
        if (m.find()) setter.accept(m.group(1));
    }

    public static void print(Player owner){
        List<Zone> zones = parseInput(ConfigHandler.COMMON.zones.get().toString());
        for(Zone zone: zones){
            if(owner.getName().getString().equals(zone.owner)){
                owner.sendSystemMessage(Component.literal("===============================").withStyle(ChatFormatting.DARK_GRAY));
                owner.sendSystemMessage(Component.literal("Zone name: " + zone.name).withStyle(ChatFormatting.DARK_PURPLE));
                owner.sendSystemMessage(Component.literal("Zone owner: " + zone.owner).withStyle(ChatFormatting.DARK_GREEN));
                owner.sendSystemMessage(Component.literal("Zone members: " + zone.members).withStyle(ChatFormatting.DARK_GRAY));
                owner.sendSystemMessage(Component.literal("Zone cords: x1: " + zone.x1 + " z1: " + zone.z1 + " x2: " + zone.x2 + " z2: " + zone.z2).withStyle(ChatFormatting.GREEN));
                if(zone.strict)
                    owner.sendSystemMessage(Component.literal("Strict zone: " + zone.strict).withStyle(ChatFormatting.BLUE));
                else owner.sendSystemMessage(Component.literal("Strict zone: " + zone.strict).withStyle(ChatFormatting.RED));
                if(zone.interact)
                    owner.sendSystemMessage(Component.literal("Interact: " + zone.interact).withStyle(ChatFormatting.BLUE));
                else owner.sendSystemMessage(Component.literal("Interact: " + zone.interact).withStyle(ChatFormatting.RED));
                if(zone.mobSpawn)
                    owner.sendSystemMessage(Component.literal("Mob Spawn: " + zone.mobSpawn).withStyle(ChatFormatting.BLUE));
                else owner.sendSystemMessage(Component.literal("Mob Spawn: " + zone.mobSpawn).withStyle(ChatFormatting.RED));
                owner.sendSystemMessage(Component.literal("===============================").withStyle(ChatFormatting.DARK_GRAY));
            }
        }
    }

    public static void sync(){
        ZoneUtil.zones = ZoneUtil.parseInput(ConfigHandler.COMMON.zones.get().toString());
        PacketHandler.sendToClients(PacketDistributor.ALL.noArg(), new ServerToClientSync(ConfigHandler.COMMON.zones.get().toString()));
    }
}
