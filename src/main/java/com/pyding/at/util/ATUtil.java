package com.pyding.at.util;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.pyding.at.capability.PlayerCapabilityProviderVP;
import com.pyding.at.mixin.ATArmorMixin;
import com.pyding.at.mixin.ATItemMixin;
import com.pyding.at.mixin.ATSwordsMixin;
import com.pyding.at.network.PacketHandler;
import com.pyding.at.network.packets.HashMapClient;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ATUtil {
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


    public static List<Item> items = new ArrayList<>();

    public static List<Item> getItems(){
        if(items.isEmpty()){
            for(Item item: ForgeRegistries.ITEMS) {
                items.add(item);
                giveBonus(item);
            }
        }
        return items;
    }

    public static HashMap<String,Integer> itemTiers = new HashMap<>();
    public static HashMap<String,Integer> entityTiers = new HashMap<>();

    public static void initMaps(Player player){
        if(itemTiers.isEmpty()){
            if(player instanceof ServerPlayer serverPlayer) {
                initMap(ConfigHandler.COMMON.itemTiers.get().toString(), itemTiers);
                PacketHandler.sendToClient(new HashMapClient(ConfigHandler.COMMON.itemTiers.get().toString(), 1), serverPlayer);
            }
        }
        if(entityTiers.isEmpty()){
            if(player instanceof ServerPlayer serverPlayer) {
                initMap(ConfigHandler.COMMON.entityTiers.get().toString(), entityTiers);
                PacketHandler.sendToClient(new HashMapClient(ConfigHandler.COMMON.entityTiers.get().toString(), 2), serverPlayer);
            }
        }
    }

    public static void initMap(String input, HashMap<String,Integer> map){
        Pattern pattern = Pattern.compile("(\\d+)-([^,]+)");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            int tier = Integer.parseInt(matcher.group(1));
            String element = matcher.group(2);
            map.put(element, tier);
        }
    }

    public static List<String> getItemsWithTier(int targetTier) {
        List<String> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : itemTiers.entrySet()) {
            if (entry.getValue() == targetTier) {
                result.add(entry.getKey());
            }
        }
        return result;
    }

    public static int getTier(ItemStack stack){
        if(itemTiers.containsKey(stack.getDescriptionId()))
            return itemTiers.get(stack.getDescriptionId());
        return 0;
    }

    public static int getTier(Item stack){
        if(itemTiers.containsKey(stack.getDescriptionId()))
            return itemTiers.get(stack.getDescriptionId());
        return 0;
    }

    public static int getTier(Block block){
        if(itemTiers.containsKey(block.getDescriptionId()))
            return itemTiers.get(block.getDescriptionId());
        return 0;
    }

    public static int getTier(LivingEntity entity){
        if(entityTiers.containsKey(entity.getType().getDescriptionId()))
            return entityTiers.get(entity.getType().getDescriptionId());
        return 0;
    }

    public static BlockPos getRayBlock(Player player, double reachDistance) {
        Vec3 eyePosition = player.getEyePosition(1.0F);
        Vec3 lookVector = player.getLookAngle();
        Vec3 reachVector = eyePosition.add(lookVector.x * reachDistance, lookVector.y * reachDistance, lookVector.z * reachDistance);

        BlockHitResult blockHitResult = player.getCommandSenderWorld().clip(new net.minecraft.world.level.ClipContext(eyePosition, reachVector, net.minecraft.world.level.ClipContext.Block.OUTLINE, net.minecraft.world.level.ClipContext.Fluid.NONE, player));

        if (blockHitResult.getType() == HitResult.Type.BLOCK) {
            return blockHitResult.getBlockPos();
        }

        return null;
    }

    public static List<ItemStack> getAllItems(Player player){
        List<ItemStack> list = new ArrayList<>(player.getInventory().items);
        return list;
    }

    public static void addTierToPlayer(Player player){
        player.getCapability(PlayerCapabilityProviderVP.playerCap).ifPresent(cap -> {
           cap.addTier(player);
        });
    }

    public static void removeItemConfig(String remove,Player player){
        String add = "";
        for(String element : ConfigHandler.COMMON.itemTiers.get().toString().split(",")){
            if(!element.equals(remove))
                add += element + ",";
        }
        ConfigHandler.COMMON.itemTiers.set(add);
        itemTiers.clear();
        initMaps(player);
    }

    public static void addItemConfig(String add,Player player){
        if(add.contains("block.minecraft.air"))
            return;
        ConfigHandler.COMMON.itemTiers.set(ConfigHandler.COMMON.itemTiers.get()+add);
        itemTiers.clear();
        initMaps(player);
    }

    public static void removeEntityConfig(String remove,Player player){
        String add = "";
        for(String element : ConfigHandler.COMMON.entityTiers.get().toString().split(",")){
            if(!element.equals(remove))
                add += element + ",";
        }
        ConfigHandler.COMMON.entityTiers.set(add);
        entityTiers.clear();
        initMaps(player);
    }

    public static void addEntityConfig(String add,Player player){
        ConfigHandler.COMMON.entityTiers.set(ConfigHandler.COMMON.entityTiers.get()+add);
        entityTiers.clear();
        initMaps(player);
    }

    public static float calculateBonus(float damage,int playerTier, int creatureTier,boolean isPlayerAttacking) {
        int tierDifference;
        if(isPlayerAttacking)
            tierDifference = playerTier - creatureTier;
        else tierDifference = creatureTier - playerTier;
        double bonusPerTier = (double) ConfigHandler.COMMON.damagePercentBonus.get() /  ConfigHandler.COMMON.maxTier.get();
        return (float) Math.max(ConfigHandler.COMMON.minimumDamage.get(), damage*(1+(tierDifference*bonusPerTier)/100));
    }

    public static float calculateToolBonus(int toolTier, int maxBonus) {
        int max = 10;
        if(ConfigHandler.COMMON_SPEC.isLoaded())
            max = ConfigHandler.COMMON.maxTier.get();
        int tierDifference = max - (max - toolTier);
        double bonusPerTier = (double) maxBonus /  max;
        return (float) (1+(tierDifference*bonusPerTier)/100);
    }

    public static void giveBonus(Item item){
        int tier = getTier(item);
        if(tier > 0){
            int attack = 200;
            int armorB = 150;
            int durability = 150;
            if(ConfigHandler.COMMON_SPEC.isLoaded()){
                attack = ConfigHandler.COMMON.attackPercentBonus.get();
                armorB = ConfigHandler.COMMON.armorPercentBonus.get();
                durability = ConfigHandler.COMMON.durabilityPercentBonus.get();
            }
            if(item instanceof SwordItem sword) {
                ((ATSwordsMixin)item).setAttackDamage(sword.getDamage()*calculateToolBonus(tier, attack));
                ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
                builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF"), "Weapon modifier", sword.getDamage(), AttributeModifier.Operation.ADDITION));
                builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(UUID.fromString("FA233E1C-4180-4865-B01B-BCCE9785ACA3"), "Weapon modifier", getAttackSpeed(sword.getDefaultInstance()), AttributeModifier.Operation.ADDITION));
                ((ATSwordsMixin)item).setMap(builder.build());
            }
            if(item instanceof ArmorItem armor) {
                ((ATArmorMixin)item).setDefence((int) (armor.getDefense()*calculateToolBonus(tier, armorB)));
                ((ATArmorMixin)item).setToughness((armor.getToughness()*calculateToolBonus(tier, armorB)));
                ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
                UUID uuid = ((ATArmorMixin)item).getModifiers().get(armor.getType());
                builder.put(Attributes.ARMOR, new AttributeModifier(uuid, "Armor modifier", armor.getDefense(), AttributeModifier.Operation.ADDITION));
                builder.put(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(uuid, "Armor toughness", armor.getToughness(), AttributeModifier.Operation.ADDITION));
                ((ATArmorMixin)item).setMap(builder.build());
            }
            if (item.isDamageable(item.getDefaultInstance()))
                ((ATItemMixin)item).setDurability((int) (item.getMaxDamage()*calculateToolBonus(tier, durability)));
        }
    }

    public static double getAttackSpeed(ItemStack itemStack) {
        if (itemStack.getItem() instanceof TieredItem) {
            Multimap<Attribute, AttributeModifier> attributeModifiers = itemStack.getAttributeModifiers(EquipmentSlot.MAINHAND);
            Collection<AttributeModifier> attackSpeedModifiers = attributeModifiers.get(Attributes.ATTACK_SPEED);

            for (AttributeModifier modifier : attackSpeedModifiers) {
                if (modifier.getOperation() == AttributeModifier.Operation.ADDITION) {
                    return modifier.getAmount();
                }
            }
        }
        return 0.0;
    }

    public static ChatFormatting getColor(int tier){
        switch (tier){
            case 1 -> {
                return ChatFormatting.GREEN;
            }
            case 2 -> {
                return ChatFormatting.DARK_GREEN;
            }
            case 3 -> {
                return ChatFormatting.YELLOW;
            }
            case 4 -> {
                return ChatFormatting.GOLD;
            }
            case 5 -> {
                return ChatFormatting.BLUE;
            }
            case 6 -> {
                return ChatFormatting.DARK_BLUE;
            }
            case 7 -> {
                return ChatFormatting.RED;
            }
            case 8 -> {
                return ChatFormatting.DARK_RED;
            }
            case 9 -> {
                return ChatFormatting.LIGHT_PURPLE;
            }
            case 10 -> {
                return ChatFormatting.DARK_PURPLE;
            }
            default -> {
                return ChatFormatting.AQUA;
            }
        }
    }

    public static int getExpNext(int tier){
        int maxLevel = ConfigHandler.COMMON.maxTier.get();
        if (tier >= maxLevel) {
            return 0;
        }

        int totalExpForCurrentLevel = 0;

        for (int level = 1; level <= tier; level++) {
            totalExpForCurrentLevel += getItemsWithTier(level).size() * level;
        }

        double initialPercentage = ConfigHandler.COMMON.startingExpPercent.get();
        double incrementPercentage = (1 - initialPercentage) / (maxLevel - 1);
        double usagePercentage = initialPercentage + (tier - 1) * incrementPercentage;
        if (tier == maxLevel-1)
            return totalExpForCurrentLevel;
        return (int) Math.round(totalExpForCurrentLevel * usagePercentage);
    }

    public static boolean notContains(String list, String word){
        boolean notContains = true;
        if(list.isEmpty())
            return true;
        for (String element : list.split(",")) {
            if (element.equals(word)) {
                notContains = false;
                break;
            }
        }
        return notContains;
    }

    public static List<LivingEntity> ray(Player player, float range, int maxDist, boolean stopWhenFound) {
        Vector3 target = Vector3.fromEntityCenter(player);
        List<LivingEntity> entities = new ArrayList<>();

        for (int distance = 1; distance < maxDist; ++distance) {
            target = target.add(new Vector3(player.getLookAngle()).multiply(distance)).add(0.0, 0.5, 0.0);
            List<LivingEntity> list = player.getCommandSenderWorld().getEntitiesOfClass(LivingEntity.class, new AABB(target.x - range, target.y - range, target.z - range, target.x + range, target.y + range, target.z + range));
            list.removeIf(entity -> entity == player || !player.hasLineOfSight(entity));
            for(LivingEntity entity: list){
                if(!entities.contains(entity))
                    entities.add(entity);
            }

            if (stopWhenFound && entities.size() > 0) {
                break;
            }
        }

        return entities;
    }

    public static boolean notIgnored(ItemStack stack){
        return !stack.getOrCreateTag().getBoolean("ATIgnore");
    }
}
