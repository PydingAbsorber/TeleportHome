package com.pyding.ng.util;

import com.pyding.ng.network.PacketHandler;
import com.pyding.ng.network.packets.ServerToClientSync;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
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

    public static boolean isBetween(int x, int x1, int x2) {
        int lowerBound = Math.min(x1, x2);
        int upperBound = Math.max(x1, x2);
        return x >= lowerBound && x <= upperBound;
    }

    static class Zone {
        String name;
        String owner;
        List<String> members = new ArrayList<>();
        int x1, z1, x2, z2;
        boolean strict = false;
    }

    public static String addNewZone(String input, String name, Player owner, int x1, int z1, int x2, int z2) {
        List<Zone> entities = parseInput(input);

        boolean nameExists = entities.stream().anyMatch(e -> e.name.equals(name));
        boolean ownerExists = entities.stream().anyMatch(e -> e.owner.equals(owner.getName().getString()));
        if(nameExists)
            owner.sendSystemMessage(Component.literal("Zone with that name already exists."));
        if(ownerExists)
            owner.sendSystemMessage(Component.literal("Cannot create more than 1 zone for you."));
        if (!nameExists && !ownerExists) {
            Zone newZone = new Zone();
            newZone.name = name;
            newZone.owner = owner.getName().getString();
            newZone.x1 = x1;
            newZone.z1 = z1;
            newZone.x2 = x2;
            newZone.z2 = z2;
            entities.add(newZone);
            owner.sendSystemMessage(Component.literal("Zone " + name + " was created successfully!"));
        }

        return serializeEntities(entities);
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

        return serializeEntities(entities);
    }

    public static String removeZone(String input, String name, Player owner) {
        List<Zone> entities = parseInput(input);
        entities.removeIf(e -> e.name.equals(name) && e.owner.equals(owner.getName().getString()));
        return serializeEntities(entities);
    }

    public static String setStrict(String input, String name, Player owner, boolean strict) {
        List<Zone> entities = parseInput(input);

        entities.stream()
                .filter(e -> e.name.equals(name) && e.owner.equals(owner.getName().getString()))
                .findFirst()
                .ifPresent(e -> {
                    e.strict = strict;
                    owner.sendSystemMessage(Component.literal("Strict Zone is now " + strict));
                });

        return serializeEntities(entities);
    }

    public static List<Zone> parseInput(String input) {
        List<Zone> entities = new ArrayList<>();
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

            entities.add(e);
        }
        return entities;
    }

    private static String serializeEntities(List<Zone> entities) {
        return entities.stream()
                .map(e -> String.format(
                        "{name(%s)owner(%s)members(%s)cords(%d,%d,%d,%d)strict(%b)}",
                        e.name,
                        e.owner,
                        String.join(",", e.members),
                        e.x1, e.z1, e.x2, e.z2,
                        e.strict
                ))
                .collect(Collectors.joining(","));
    }

    private static void extractField(String obj, String field, Consumer<String> setter) {
        Matcher m = Pattern.compile(field + "\\(([^)]+)\\)").matcher(obj);
        if (m.find()) setter.accept(m.group(1));
    }

    public static void print(Player owner){
        List<Zone> entities = parseInput(ConfigHandler.COMMON.zones.get().toString());
        entities.stream()
                .filter(e -> e.owner.equals(owner.getName().getString()))
                .findFirst()
                .ifPresent(e -> {
                    owner.sendSystemMessage(Component.literal("Zone name: " + e.name));
                    owner.sendSystemMessage(Component.literal("Zone owner: " + e.owner));
                    owner.sendSystemMessage(Component.literal("Zone members: " + e.members));
                    owner.sendSystemMessage(Component.literal("Zone cords: x1: " + e.x1 + " z1: " + e.z1 + " x2: " + e.x2 + " z2: " + e.z2));
                    owner.sendSystemMessage(Component.literal("Strict zone: " + e.strict));
                });
    }

    public static void sync(){
        ZoneUtil.zones = ZoneUtil.parseInput(ConfigHandler.COMMON.zones.get().toString());
        PacketHandler.sendToClients(PacketDistributor.ALL.noArg(), new ServerToClientSync(ConfigHandler.COMMON.zones.get().toString()));
    }
}
