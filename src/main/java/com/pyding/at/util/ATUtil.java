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

    public static int getMaxExp(int tier){
        int totalExpForCurrentLevel = 0;

        for (int level = 1; level <= tier; level++) {
            totalExpForCurrentLevel += getItemsWithTier(level).size() * level;
        }
        return totalExpForCurrentLevel;
    }

    public static int getExpNext(int tier){
        int maxLevel = ConfigHandler.COMMON.maxTier.get();
        if (tier >= maxLevel) {
            return 0;
        }

        double initialPercentage = ConfigHandler.COMMON.startingExpPercent.get();
        double incrementPercentage = (1 - initialPercentage) / (maxLevel - 1);
        double usagePercentage = initialPercentage + (tier - 1) * incrementPercentage;
        if (tier == maxLevel-1)
            return getMaxExp(tier);
        return (int) Math.round(getMaxExp(tier) * usagePercentage);
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

    public static String getBaseItems(){
        return "3-item.aquaculture.neptunium_fishing_rod,4-item.botania.mining_ring,1-item.irons_spellbooks.silver_ring,1-item.eidolon.silver_helmet,1-item.eidolon.silver_chestplate,1-item.eidolon.silver_leggings,1-item.eidolon.silver_boots,1-item.eidolon.silver_sword,1-item.eidolon.silver_pickaxe,1-item.eidolon.silver_axe,1-item.eidolon.silver_shovel,1-item.eidolon.silver_hoe,1-item.aether.holystone_sword,1-item.aether.holystone_shovel,1-item.aether.holystone_pickaxe,1-item.aether.holystone_axe,1-item.aether.holystone_hoe,1-item.alexscaves.diving_helmet,1-item.alexscaves.diving_chestplate,1-item.alexscaves.diving_leggings,1-item.alexscaves.diving_boots,1-item.minecraft.diamond,1-item.farmersdelight.flint_knife,1-item.farmersdelight.golden_knife,1-item.botania.manasteel_boots,1-item.botania.manasteel_leggings,1-item.botania.manasteel_chestplate,1-item.botania.manasteel_helmet,1-item.botania.manasteel_shears,1-item.botania.manasteel_sword,1-item.botania.manasteel_axe,1-item.botania.manasteel_shovel,1-item.botania.manasteel_pick,1-item.botania.manaweave_chestplate,1-item.botania.manaweave_leggings,1-item.botania.manaweave_boots,1-item.botania.manaweave_helmet,1-item.the_bumblezone.stinger_spear,1-item.alexscaves.limestone_spear,1-item.grimoireofgaia.metal_dagger,1-item.minecraft.fishing_rod,1-item.minecraft.diamond_sword,1-item.minecraft.diamond_helmet,1-item.minecraft.diamond_chestplate,1-item.minecraft.diamond_leggings,1-item.minecraft.diamond_boots,1-item.botania.manasteel_ingot,1-item.farmersdelight.iron_knife,1-item.farmersdelight.diamond_knife,1-item.minecraft.iron_sword,1-item.minecraft.iron_shovel,1-item.minecraft.iron_axe,1-item.minecraft.iron_hoe,1-item.minecraft.iron_pickaxe,1-item.minecraft.iron_helmet,1-item.minecraft.iron_chestplate,1-item.minecraft.iron_leggings,1-item.minecraft.iron_boots,1-item.minecraft.chainmail_helmet,1-item.minecraft.chainmail_chestplate,1-item.minecraft.chainmail_leggings,1-item.minecraft.chainmail_boots,1-item.minecraft.golden_helmet,1-item.minecraft.golden_chestplate,1-item.minecraft.golden_leggings,1-item.minecraft.golden_boots,1-item.minecraft.golden_axe,1-item.minecraft.golden_sword,1-item.minecraft.golden_shovel,1-item.minecraft.golden_pickaxe,1-item.minecraft.golden_axe,1-item.minecraft.golden_hoe,1-item.minecraft.leather_helmet,1-item.minecraft.leather_chestplate,1-item.minecraft.leather_boots,1-item.minecraft.leather_leggings,2-item.minecraft.netherite_ingot,2-item.botania.ender_dagger,2-item.aquamirae.remnants_saber,2-item.aquamirae.poisoned_blade,2-item.aether.zanite_sword,2-item.aether.zanite_shovel,2-item.aether.zanite_pickaxe,2-item.aether.zanite_axe,2-item.aether.zanite_hoe,2-item.aether.gravitite_sword,2-item.aether.gravitite_shovel,2-item.aether.gravitite_pickaxe,2-item.aether.gravitite_axe,2-item.aether.gravitite_hoe,2-item.twilightforest.phantom_helmet,2-item.twilightforest.phantom_chestplate,2-item.twilightforest.naga_chestplate,2-item.twilightforest.naga_leggings,2-item.aether.flaming_sword,2-item.alexscaves.hazmat_mask,2-item.alexscaves.hazmat_chestplate,2-item.alexscaves.hazmat_leggings,2-item.alexscaves.hazmat_boots,2-item.enigmaticlegacy.the_acknowledgment,2-item.irons_spellbooks.heavy_chain_necklace,2-item.twilightforest.steeleaf_ingot,2-item.twilightforest.steeleaf_helmet,2-item.twilightforest.steeleaf_chestplate,2-item.twilightforest.steeleaf_leggings,2-item.twilightforest.steeleaf_boots,2-item.twilightforest.steeleaf_sword,2-item.twilightforest.steeleaf_shovel,2-item.twilightforest.steeleaf_pickaxe,2-item.twilightforest.steeleaf_axe,2-item.twilightforest.steeleaf_hoe,2-item.call_of_yucutan.obsidian_spear,2-item.call_of_yucutan.obsidian_tecpatl,2-item.call_of_yucutan.flint_spear,2-item.call_of_yucutan.silex_tecpatl,2-item.call_of_yucutan.wooden_spear,2-item.call_of_yucutan.wooden_tecpatl,2-item.call_of_yucutan.macuahuitl,2-item.twilightforest.arctic_helmet,2-item.twilightforest.arctic_chestplate,2-item.twilightforest.arctic_leggings,2-item.twilightforest.arctic_boots,2-item.mowziesmobs.naga_fang_dagger,2-item.aquaculture.iron_fishing_rod,2-item.twilightforest.ironwood_ingot,2-item.farmersdelight.netherite_knife,2-item.minecraft.netherite_shovel,2-item.minecraft.netherite_pickaxe,2-item.minecraft.netherite_axe,2-item.minecraft.netherite_hoe,2-item.minecraft.turtle_helmet,2-item.the_bumblezone.bumble_bee_chestplate_1,2-item.the_bumblezone.bumble_bee_chestplate_2,2-item.the_bumblezone.bumble_bee_chestplate_trans_1,2-item.the_bumblezone.bumble_bee_chestplate_trans_2,2-item.the_bumblezone.honey_bee_leggings_1,2-item.the_bumblezone.carpenter_bee_boots_1,2-item.the_bumblezone.carpenter_bee_boots_2,2-item.the_bumblezone.honey_bee_leggings_2,3-item.botania.mana_gun,3-item.botania.elementium_ingot,3-item.aquamirae.three_bolt_helmet,3-item.aquamirae.three_bolt_suit,3-item.aquamirae.three_bolt_leggings,3-item.aquamirae.three_bolt_boots,3-item.aether.valkyrie_hoe,3-item.aether.valkyrie_axe,3-item.aether.valkyrie_pickaxe,3-item.aether.valkyrie_shovel,3-item.aether.valkyrie_lance,3-item.twilightforest.fiery_helmet,3-item.twilightforest.fiery_chestplate,3-item.twilightforest.fiery_leggings,3-item.twilightforest.fiery_boots,3-item.twilightforest.fiery_sword,3-item.twilightforest.fiery_pickaxe,3-item.aether.lightning_sword,3-item.unusualfishmod.depth_scythe,3-item.irons_spellbooks.keeper_flamberge,3-block.botania.endoflame,3-item.eidolon.reaper_scythe,3-item.eidolon.cleaving_axe,3-item.irons_spellbooks.cryomancer_helmet,3-item.irons_spellbooks.cryomancer_chestplate,3-item.irons_spellbooks.cryomancer_leggings,3-item.irons_spellbooks.cryomancer_boots,3-item.irons_spellbooks.priest_helmet,3-item.irons_spellbooks.priest_chestplate,3-item.irons_spellbooks.priest_leggings,3-item.irons_spellbooks.priest_boots,3-item.irons_spellbooks.plagued_helmet,3-item.irons_spellbooks.plagued_chestplate,3-item.irons_spellbooks.plagued_leggings,3-item.irons_spellbooks.plagued_boots,3-item.irons_spellbooks.copper_spell_book,3-item.cataclysm.coral_spear,3-item.aquaculture.gold_fishing_rod,3-item.twilightforest.fiery_ingot,3-item.irons_spellbooks.pumpkin_boots,3-item.irons_spellbooks.pumpkin_leggings,3-item.irons_spellbooks.pumpkin_chestplate,3-item.irons_spellbooks.pumpkin_helmet,3-item.irons_spellbooks.wandering_magician_boots,3-item.irons_spellbooks.wandering_magician_leggings,3-item.irons_spellbooks.wandering_magician_chestplate,3-item.irons_spellbooks.wandering_magician_helmet,3-item.irons_spellbooks.cultist_boots,3-item.irons_spellbooks.cultist_leggings,3-item.irons_spellbooks.cultist_chestplate,3-item.irons_spellbooks.cultist_helmet,3-item.irons_spellbooks.archevoker_boots,3-item.irons_spellbooks.archevoker_leggings,3-item.irons_spellbooks.electromancer_leggings,3-item.irons_spellbooks.archevoker_chestplate,3-item.irons_spellbooks.pyromancer_helmet,3-item.irons_spellbooks.pyromancer_chestplate,3-item.irons_spellbooks.pyromancer_leggings,3-item.irons_spellbooks.pyromancer_boots,3-item.irons_spellbooks.electromancer_helmet,3-item.irons_spellbooks.electromancer_chestplate,3-item.irons_spellbooks.electromancer_boots,3-item.irons_spellbooks.archevoker_helmet,4-item.aquamirae.fin_cutter,4-item.aquamirae.terrible_helmet,4-item.aquamirae.terrible_chestplate,4-item.aquamirae.terrible_leggings,4-item.aquamirae.terrible_boots,4-item.call_of_yucutan.jades_helmet,4-item.call_of_yucutan.jades_chestplate,4-item.call_of_yucutan.jades_leggings,4-item.call_of_yucutan.jades_boots,4-item.eidolon.reversal_pick,4-item.eidolon.sapping_sword,4-item.twilightforest.yeti_helmet,4-item.twilightforest.yeti_chestplate,4-item.twilightforest.yeti_leggings,4-item.twilightforest.yeti_boots,4-item.aether.vampire_blade,4-item.enigmaticlegacy.animal_guidebook,4-item.enigmaticlegacy.hunter_guidebook,4-item.irons_spellbooks.magehunter,4-item.irons_spellbooks.cast_time_ring,4-item.irons_spellbooks.iron_spell_book,4-item.rats.plague_scythe,4-item.aquamirae.dagger_of_greed,4-item.bosses_of_mass_destruction.earthdive_spear,4-item.aquaculture.diamond_fishing_rod,4-item.minecraft.end_crystal,4-block.relics.researching_table,4-item.call_of_yucutan.jade_sword,4-item.call_of_yucutan.jade_pickaxe,4-item.call_of_yucutan.jade_axe,4-item.call_of_yucutan.jade_shovel,4-item.call_of_yucutan.jade_hoe,4-block.minecraft.smithing_table,4-block.minecraft.anvil,4-block.irons_spellbooks.inscription_table,4-item.botania.magnet_ring,4-item.botania.aura_ring,4-item.botania.mining_ring,5-item.bloodmagic.soulsword,5-item.bloodmagic.soulpickaxe,5-item.bloodmagic.soulaxe,5-item.bloodmagic.soulshovel,5-item.bloodmagic.soulscythe,5-item.eidolon.bonelord_helm,5-item.eidolon.bonelord_chestplate,5-item.eidolon.bonelord_greaves,5-item.eidolon.deathbringer_scythe,5-item.alexscaves.primitive_club,5-item.enigmaticlegacy.the_twist,5-item.enigmaticlegacy.enigmatic_elytra,5-item.enigmaticlegacy.infinimeal,5-item.enigmaticlegacy.forbidden_fruit,5-item.irons_spellbooks.lightning_rod,5-item.irons_spellbooks.artificer_cane,5-item.irons_spellbooks.ice_staff,5-item.irons_spellbooks.graybeard_staff,5-item.irons_spellbooks.blood_staff,5-item.irons_spellbooks.gold_spell_book,5-item.irons_spellbooks.rotten_spell_book,5-block.botania.hydroangeas,5-item.mowziesmobs.wrought_axe,5-item.twilightforest.charm_of_keeping_1,5-item.eidolon.prestigious_palm,5-item.eidolon.warlock_boots,5-item.eidolon.warlock_cloak,5-item.eidolon.warlock_hat,5-item.minecraft.enchanted_golden_apple,6-item.botania.terrasteel_ingot,6-item.bhc.heart_amulet,6-item.cataclysm.infernal_forge,6-item.cataclysm.tidal_claws,6-item.cataclysm.void_forge,6-item.cataclysm.the_incinerator,6-item.cataclysm.gauntlet_of_bulwark,6-item.cataclysm.gauntlet_of_guard,6-item.cataclysm.bulwark_of_the_flame,6-item.eidolon.warded_mail,6-item.enigmaticlegacy.cursed_scroll,6-item.enigmaticlegacy.avarice_scroll,6-item.enigmaticlegacy.berserk_charm,6-item.enigmaticlegacy.enchanter_pearl,6-item.irons_spellbooks.mana_ring,6-item.irons_spellbooks.diamond_spell_book,6-block.irons_spellbooks.arcane_anvil,6-block.botania.rosa_arcana,6-item.alexscaves.ortholance,6-item.aquaculture.neptunium_fishing_rod,6-block.minecraft.enchanting_table,6-block.minecraft.brewing_stand,6-item.botania.magnet_ring_greater,6-item.botania.aura_ring_greater,6-item.botania.reach_ring,7-item.aquamirae.terrible_sword,7-item.botania.star_sword,7-item.botania.thunder_sword,7-item.cataclysm.bone_reptile_helmet,7-item.cataclysm.bone_reptile_chestplate,7-item.eidolon.raven_cloak,7-item.eidolon.void_amulet,7-item.aether.valkyrie_helmet,7-item.aether.valkyrie_chestplate,7-item.aether.valkyrie_leggings,7-item.aether.valkyrie_boots,7-item.aquaculture.neptunium_ingot,7-item.alexscaves.desolate_dagger,7-item.alexscaves.dreadbow,7-item.enigmaticlegacy.ender_slayer,7-item.enigmaticlegacy.guardian_heart,7-item.irons_spellbooks.blaze_spell_book,7-item.irons_spellbooks.necronomicon_spell_book,7-item.irons_spellbooks.evoker_spell_book,7-item.irons_spellbooks.druidic_spell_book,7-item.irons_spellbooks.villager_spell_book,7-block.occultism.dimensional_mineshaft,7-item.irons_spellbooks.shadowwalker_helmet,7-item.irons_spellbooks.shadowwalker_chestplate,7-item.irons_spellbooks.shadowwalker_leggings,7-item.irons_spellbooks.shadowwalker_boots,7-item.irons_spellbooks.netherite_mage_helmet,7-item.irons_spellbooks.netherite_mage_chestplate,7-item.irons_spellbooks.netherite_mage_leggings,7-item.irons_spellbooks.netherite_mage_boots,7-item.twilightforest.charm_of_keeping_2,7-item.twilightforest.magic_beans,7-block.botania.brewery,8-item.aquamirae.coral_lance,8-item.bhc.blade_of_vitality,8-item.celestisynth.breezebreaker,8-item.celestisynth.poltergeist,8-item.celestisynth.frostbound,8-item.alexscaves.extinction_spear,8-item.mythicbotany.alfsteel_ingot,8-item.mythicbotany.alfsteel_sword,8-item.mythicbotany.alfsteel_pick,8-item.mythicbotany.alfsteel_axe,8-block.mythicbotany.mjoellnir,8-item.enigmaticlegacy.etherium_ingot,8-item.enigmaticlegacy.infernal_shield,8-block.botania.gourmaryllis,8-item.enigmaticlegacy.forbidden_fruit,8-block.eidolon.soul_enchanter,9-item.bhc.soul_heart_amulet,9-item.celestisynth.solaris,9-item.celestisynth.rainfall_serenity,9-item.alexscaves.resistor_shield,9-item.relics.jellyfish_necklace,9-item.relics.holy_locket,9-item.alexscaves.raygun,9-item.enigmaticlegacy.forbidden_axe,9-item.relics.midnight_robe,9-item.relics.reflection_necklace,9-item.irons_spellbooks.emerald_stoneplate_ring,9-item.irons_spellbooks.cooldown_ring,9-item.irons_spellbooks.dragonskin_spell_book,9-block.botania.dandelifeon,9-block.alexsmobs.transmutation_table,9-item.deeperdarker.warden_sword,9-item.deeperdarker.warden_axe,9-item.deeperdarker.warden_hoe,9-item.deeperdarker.warden_shovel,9-item.deeperdarker.warden_pickaxe,9-item.deeperdarker.warden_helmet,9-item.deeperdarker.warden_chestplate,9-item.deeperdarker.warden_leggings,9-item.deeperdarker.warden_boots,9-item.mowziesmobs.spear,9-item.relics.infinity_ham,9-item.twilightforest.charm_of_keeping_3,9-item.endermanoverhaul.bubble_pearl,9-item.endermanoverhaul.summoner_pearl,9-item.endermanoverhaul.icy_pearl,9-item.endermanoverhaul.crimson_pearl,9-item.endermanoverhaul.warped_pearl,9-item.endermanoverhaul.soul_pearl,9-block.alexscaves.tremorzilla_egg,9-block.alexscaves.mussel,9-item.botania.spawner_mover,10-item.bloodmagic.livinghelmet,10-item.bloodmagic.livingplate,10-item.bloodmagic.livingleggings,10-item.bloodmagic.livingboots,10-item.aquamirae.abyssal_heaume,10-item.aquamirae.abyssal_brigantine,10-item.aquamirae.abyssal_leggings,10-item.aquamirae.abyssal_boots,10-item.aquamirae.divider,10-item.aquamirae.whisper_of_the_abyss,10-item.cataclysm.ignitium_helmet,10-item.cataclysm.ignitium_chestplate,10-item.cataclysm.ignitium_elytra_chestplate,10-item.cataclysm.ignitium_leggings,10-item.cataclysm.ignitium_boots,10-item.celestisynth.aquaflora,10-item.celestisynth.crescentia,10-item.relics.rage_glove,10-item.alexscaves.hood_of_darkness,10-item.alexscaves.cloak_of_darkness,10-item.enigmaticlegacy.fabulous_scroll,10-item.enigmaticlegacy.eldritch_pan,10-item.enigmaticlegacy.the_cube,10-item.enigmaticlegacy.ascension_amulet,10-item.enigmaticlegacy.eldritch_amulet,10-item.irons_spellbooks.netherite_spell_book,10-item.alexsmobs.rocky_chestplate,10-item.alexscaves.totem_of_possession,10-item.alexsmobs.novelty_hat,10-item.enigmaticlegacy.astral_fruit,10-item.enigmaticlegacy.ichor_bottle,10-item.twilightforest.charm_of_life_2,10-item.twilightforest.charm_of_life_1,10-item.enigmaticlegacy.the_judgement,6-item.botania.terra_pick,7-item.enigmaticlegacy.super_magnet_ring,9-item.enigmaticlegacy.heaven_scroll,7-item.enigmaticlegacy.escape_scroll,8-item.enigmaticlegacy.the_infinitum,9-item.enigmaticlegacy.desolation_ring,8-item.enigmaticlegacy.etherium_pickaxe,8-item.enigmaticlegacy.etherium_axe,8-item.enigmaticlegacy.etherium_shovel,8-item.enigmaticlegacy.etherium_sword,8-item.enigmaticlegacy.etherium_scythe,8-item.enigmaticlegacy.etherium_helmet,8-item.enigmaticlegacy.etherium_chestplate,8-item.enigmaticlegacy.etherium_leggings,8-item.enigmaticlegacy.etherium_boots,1-item.aether.lightning_knife,1-item.aether.holy_sword,1-item.aether.candy_cane_sword,1-item.aether.pig_slayer,1-item.aether.hammer_of_kingbdogz,8-item.skilltree.assassin_necklace,8-item.skilltree.bone_quiver,8-item.skilltree.diamond_quiver,9-block.occultism.storage_stabilizer_tier4,8-block.occultism.storage_stabilizer_tier3,3-item.botania.elementium_helmet,3-item.botania.elementium_chestplate,3-item.botania.elementium_leggings,3-item.botania.elementium_boots,3-item.botania.elementium_pickaxe,3-item.botania.elementium_shovel,3-item.botania.elementium_axe,3-item.botania.elementium_sword,3-item.botania.elementium_shears,3-block.mythicbotany.elementium_ore,5-block.mythicbotany.dragonstone_ore,2-block.minecraft.nether_quartz_ore,2-block.alexscaves.spelunkery_table,6-item.cataclysm.bloom_stone_pauldrons,8-item.mythicbotany.alfsteel_helmet,8-item.mythicbotany.alfsteel_chestplate,8-item.mythicbotany.alfsteel_leggings,8-item.mythicbotany.alfsteel_boots,8-item.mythicbotany.mana_ring_greatest,5-item.irons_spellbooks.concentration_amulet,2-item.minecraft.flint_and_steel,5-item.skilltree.simple_necklace,6-item.skilltree.amnesia_scroll,7-item.deeperdarker.sculk_transmitter,7-block.bosses_of_mass_destruction.mob_ward,7-item.aquaculture.neptunium_leggings,7-item.aquaculture.neptunium_boots,7-item.aquaculture.neptunium_chestplate,7-item.aquaculture.neptunium_helmet,7-item.aquaculture.neptunium_sword,7-item.aquaculture.neptunium_hoe,7-item.aquaculture.neptunium_axe,7-item.aquaculture.neptunium_shovel,7-item.aquaculture.neptunium_pickaxe,7-item.aquaculture.neptunium_bow,9-item.aquamirae.rune_of_the_storm,7-item.aquamirae.abyssal_amethyst,7-item.aquamirae.maze_rose,6-item.irons_spellbooks.amethyst_resonance_charm,6-item.aquamirae.poseidons_breakfast,5-item.aquamirae.sea_stew,2-item.twilightforest.magic_map,4-item.twilightforest.alpha_yeti_fur,2-item.minecraft.netherite_boots,2-item.minecraft.netherite_leggings,2-item.minecraft.netherite_chestplate,2-item.minecraft.netherite_helmet,2-item.minecraft.netherite_sword,3-item.alexsmobs.skelewag_sword,2-item.twilightforest.knightmetal_axe,2-item.twilightforest.knightmetal_pickaxe,2-item.twilightforest.knightmetal_ingot,2-item.twilightforest.knightmetal_helmet,2-item.twilightforest.knightmetal_chestplate,2-item.twilightforest.knightmetal_leggings,2-item.twilightforest.knightmetal_boots,2-item.twilightforest.knightmetal_sword,5-item.twilightforest.ice_sword,5-item.twilightforest.triple_bow,2-item.twilightforest.ironwood_helmet,2-item.twilightforest.ironwood_chestplate,2-item.twilightforest.ironwood_leggings,2-item.twilightforest.ironwood_boots,2-item.twilightforest.ironwood_sword,2-item.twilightforest.ironwood_shovel,2-item.twilightforest.ironwood_pickaxe,2-item.twilightforest.ironwood_axe,2-item.twilightforest.ironwood_hoe,2-item.twilightforest.gold_minotaur_axe,6-item.twilightforest.glass_sword,5-item.twilightforest.diamond_minotaur_axe,3-item.twilightforest.ender_bow,5-item.twilightforest.ice_bow,1-item.minecraft.bow,1-item.botania.livingwood_bow,3-item.botania.crystal_bow,6-item.aether.phoenix_bow,1-item.minecraft.crossbow,7-block.eidolon.wooden_brewing_stand,6-item.enigmaticlegacy.enchantment_transposer,6-item.botania.terrasteel_helmet,6-item.botania.terrasteel_chestplate,6-item.botania.terrasteel_leggings,6-item.botania.terrasteel_boots,6-item.aether.phoenix_helmet,6-item.aether.phoenix_chestplate,6-item.aether.phoenix_leggings,6-item.aether.phoenix_boots,6-item.botania.terra_sword,6-item.botania.terra_axe,6-item.botania.terra_axe,6-item.botania.terra_sword,4-item.minecraft.elytra,4-item.alexsmobs.tarantula_hawk_elytra,4-item.deeperdarker.soul_elytra,5-item.twilightforest.seeker_bow,6-item.irons_spellbooks.spellbreaker,6-item.irons_spellbooks.amethyst_rapier,10-item.celestisynth.keres,9-item.celestisynth.solar_crystal_helmet,9-item.celestisynth.solar_crystal_chestplate,9-item.celestisynth.solar_crystal_leggings,9-item.celestisynth.solar_crystal_boots,9-item.celestisynth.lunar_stone_helmet,9-item.celestisynth.lunar_stone_chestplate,9-item.celestisynth.lunar_stone_leggings,9-item.celestisynth.lunar_stone_boots,9-item.celestisynth.celestial_spell_book,9-item.celestisynth.lunar_stone_boots,9-item.celestisynth.celestial_spell_book,1-item.minecraft.diamond_shovel,1-item.minecraft.diamond_axe,1-item.minecraft.diamond_hoe,1-item.minecraft.diamond_pickaxe,1-item.supplementaries.wrench,10-item.supplementaries.soap,3-block.eidolon.lead_ore,1-block.occultism.silver_ore,1-block.occultism.silver_ore_deepslate,4-block.irons_spellbooks.armor_pile,1-block.eidolon.silver_ore,1-block.eidolon.deep_silver_ore,3-block.eidolon.deep_lead_ore,3-block.occultism.iesnium_ore,4-item.occultism.iesnium_pickaxe,4-block.call_of_yucutan.deepslate_jade_ore,4-block.call_of_yucutan.jade_ore,7-item.cataclysm.meat_shredder,2-block.minecraft.ancient_debris,2-item.aether.zanite_helmet,2-item.aether.zanite_chestplate,2-item.aether.zanite_leggings,2-item.aether.zanite_boots,2-item.aether.gravitite_helmet,2-item.aether.gravitite_chestplate,2-item.aether.gravitite_leggings,2-item.aether.gravitite_boots,2-item.aether.obsidian_helmet,2-item.aether.obsidian_chestplate,2-item.aether.obsidian_leggings,2-item.aether.obsidian_boots,2-item.aether.sentry_boots,2-block.aether.gravitite_ore,2-block.aether.zanite_ore,2-item.alexsmobs.ghostly_pickaxe,10-item.aquamirae.abyssal_tiara,3-item.goblins_tyranny.goblin_mace,3-item.goblins_tyranny.goblin_sword,3-item.goblins_tyranny.goblin_spear,3-item.goblins_tyranny.goblin_axe,3-item.goblins_tyranny.goblin_upgraded_mace,3-item.goblins_tyranny.goblins_prototype_chestplate,3-item.goblins_tyranny.upgraded_prototype_chestplate,3-item.goblins_tyranny.reinforced_prototype_helmet,3-item.goblins_tyranny.reinforced_prototype_chestplate,3-item.goblins_tyranny.reinforced_prototype_boots,3-item.goblins_tyranny.reinforced_prototype_leggings,";
    }

    public static String getBaseEntities(){
        return "1-entity.aether.aechor_plant,1-entity.aether.aerbunny,2-entity.aether.aerwhale,1-entity.aether.blue_swet,1-entity.aether.cloud_minion,2-entity.aether.cockatrice,1-entity.aether.evil_whirlwind,3-entity.aether.fire_minion,1-entity.aether.flying_cow,1-entity.aether.golden_swet,3-entity.aether.mimic,2-entity.aether.moa,1-entity.aether.phyg,1-entity.aether.sentry,1-entity.aether.sheepuff,2-entity.aether.slider,3-entity.aether.sun_spirit,3-entity.aether.valkyrie,3-entity.aether.valkyrie_queen,1-entity.aether.whirlwind,1-entity.aether.zephyr,10-entity.alexscaves.atlatitan,2-entity.alexscaves.boundroid,2-entity.alexscaves.boundroid_winch,3-entity.alexscaves.brainiac,1-entity.alexscaves.corrodent,2-entity.alexscaves.deep_one,4-entity.alexscaves.deep_one_knight,5-entity.alexscaves.deep_one_mage,1-entity.alexscaves.ferrouslime,10-entity.alexscaves.forsaken,1-entity.alexscaves.gammaroach,1-entity.alexscaves.gloomoth,1-entity.alexscaves.gossamer_worm,3-entity.alexscaves.grottoceratops,10-entity.alexscaves.hullbreaker,1-entity.alexscaves.lanternfish,10-entity.alexscaves.luxtructosaurus,2-entity.alexscaves.magnetron,2-entity.alexscaves.mine_guardian,1-entity.alexscaves.notor,3-entity.alexscaves.nucleeper,1-entity.alexscaves.radgill,2-entity.alexscaves.raycat,7-entity.alexscaves.relicheirus,1-entity.alexscaves.sea_pig,2-entity.alexscaves.subterranodon,1-entity.alexscaves.teletor,8-entity.alexscaves.tremorsaurus,10-entity.alexscaves.tremorzilla,1-entity.alexscaves.trilocaris,1-entity.alexscaves.tripodfish,2-entity.alexscaves.underzealot,2-entity.alexscaves.vallumraptor,1-entity.alexscaves.vesper,2-entity.alexscaves.watcher,1-entity.alexsmobs.alligator_snapping_turtle,3-entity.alexsmobs.anaconda,1-entity.alexsmobs.anaconda_part,2-entity.alexsmobs.anteater,1-entity.alexsmobs.bald_eagle,1-entity.alexsmobs.banana_slug,3-entity.alexsmobs.bison,1-entity.alexsmobs.blobfish,1-entity.alexsmobs.blue_jay,2-entity.alexsmobs.bone_serpent,1-entity.alexsmobs.bone_serpent_part,5-entity.alexsmobs.bunfungus,9-entity.alexsmobs.cachalot_whale,2-entity.alexsmobs.caiman,1-entity.alexsmobs.capuchin_monkey,1-entity.alexsmobs.catfish,1-entity.alexsmobs.centipede_body,2-entity.alexsmobs.centipede_head,1-entity.alexsmobs.centipede_tail,1-entity.alexsmobs.cockroach,1-entity.alexsmobs.comb_jelly,2-entity.alexsmobs.cosmaw,1-entity.alexsmobs.cosmic_cod,1-entity.alexsmobs.crimson_mosquito,2-entity.alexsmobs.crocodile,1-entity.alexsmobs.crow,1-entity.alexsmobs.devils_hole_pupfish,2-entity.alexsmobs.dropbear,5-entity.alexsmobs.elephant,2-entity.alexsmobs.emu,2-entity.alexsmobs.endergrade,2-entity.alexsmobs.enderiophage,4-entity.alexsmobs.farseer,1-entity.alexsmobs.flutter,1-entity.alexsmobs.fly,1-entity.alexsmobs.flying_fish,2-entity.alexsmobs.frilled_shark,2-entity.alexsmobs.froststalker,1-entity.alexsmobs.gazelle,1-entity.alexsmobs.gelada_monkey,2-entity.alexsmobs.giant_squid,2-entity.alexsmobs.gorilla,3-entity.alexsmobs.grizzly_bear,1-entity.alexsmobs.guster,2-entity.alexsmobs.hammerhead_shark,1-entity.alexsmobs.hummingbird,1-entity.alexsmobs.jerboa,2-entity.alexsmobs.kangaroo,2-entity.alexsmobs.komodo_dragon,4-entity.alexsmobs.laviathan,1-entity.alexsmobs.leafcutter_ant,1-entity.alexsmobs.lobster,1-entity.alexsmobs.maned_wolf,2-entity.alexsmobs.mantis_shrimp,1-entity.alexsmobs.mimic_octopus,2-entity.alexsmobs.mimicube,3-entity.alexsmobs.moose,1-entity.alexsmobs.mudskipper,1-entity.alexsmobs.mungus,2-entity.alexsmobs.murmur,2-entity.alexsmobs.murmur_head,4-entity.alexsmobs.orca,1-entity.alexsmobs.platypus,1-entity.alexsmobs.potoo,1-entity.alexsmobs.raccoon,1-entity.alexsmobs.rain_frog,1-entity.alexsmobs.rattlesnake,4-entity.alexsmobs.rhinoceros,1-entity.alexsmobs.roadrunner,1-entity.alexsmobs.rocky_roller,10-entity.alexsmobs.sea_bear,1-entity.alexsmobs.seagull,1-entity.alexsmobs.seal,1-entity.alexsmobs.shoebill,2-entity.alexsmobs.skelewag,1-entity.alexsmobs.skreecher,1-entity.alexsmobs.skunk,2-entity.alexsmobs.snow_leopard,1-entity.alexsmobs.soul_vulture,3-entity.alexsmobs.spectre,2-entity.alexsmobs.straddler,1-entity.alexsmobs.stradpole,1-entity.alexsmobs.sugar_glider,2-entity.alexsmobs.sunbird,1-entity.alexsmobs.tarantula_hawk,1-entity.alexsmobs.tasmanian_devil,1-entity.alexsmobs.terrapin,3-entity.alexsmobs.tiger,1-entity.alexsmobs.toucan,1-entity.alexsmobs.triops,3-entity.alexsmobs.tusklin,2-entity.alexsmobs.underminer,9-entity.alexsmobs.void_worm,2-entity.alexsmobs.void_worm_part,6-entity.alexsmobs.warped_mosco,2-entity.alexsmobs.warped_toad,1-entity.aquaculture.arapaima,1-entity.aquaculture.arrau_turtle,1-entity.aquaculture.atlantic_cod,1-entity.aquaculture.atlantic_halibut,1-entity.aquaculture.atlantic_herring,1-entity.aquaculture.bayad,1-entity.aquaculture.blackfish,1-entity.aquaculture.bluegill,1-entity.aquaculture.boulti,1-entity.aquaculture.box_turtle,1-entity.aquaculture.brown_shrooma,1-entity.aquaculture.brown_trout,1-entity.aquaculture.capitaine,1-entity.aquaculture.carp,1-entity.aquaculture.catfish,1-entity.aquaculture.gar,1-entity.aquaculture.jellyfish,1-entity.aquaculture.minnow,1-entity.aquaculture.muskellunge,1-entity.aquaculture.pacific_halibut,1-entity.aquaculture.perch,1-entity.aquaculture.pink_salmon,1-entity.aquaculture.piranha,1-entity.aquaculture.pollock,1-entity.aquaculture.rainbow_trout,1-entity.aquaculture.red_grouper,1-entity.aquaculture.red_shrooma,1-entity.aquaculture.smallmouth_bass,1-entity.aquaculture.starshell_turtle,1-entity.aquaculture.synodontis,1-entity.aquaculture.tambaqui,1-entity.aquaculture.tuna,3-entity.aquamirae.anglerfish,10-entity.aquamirae.captain_cornelia,10-entity.aquamirae.eel,1-entity.aquamirae.golden_moth,2-entity.aquamirae.luminous_jelly,2-entity.aquamirae.maw,6-entity.aquamirae.maze_mother,2-entity.aquamirae.maze_rose,2-entity.aquamirae.pillagers_patrol,2-entity.aquamirae.poisoned_chakra,2-entity.aquamirae.spinefish,2-entity.aquamirae.tortured_soul,10-entity.bosses_of_mass_destruction.gauntlet,10-entity.bosses_of_mass_destruction.lich,10-entity.bosses_of_mass_destruction.obsidilith,10-entity.bosses_of_mass_destruction.void_blossom,10-entity.botania.doppleganger,10-entity.botania.pink_wither,1-entity.botania.pixie,10-entity.call_of_yucutan.ah_puch,2-entity.call_of_yucutan.ahaw,1-entity.call_of_yucutan.blowgun_huracan,5-entity.call_of_yucutan.chaac,10-entity.call_of_yucutan.golden_guard,10-entity.call_of_yucutan.kukulkan,1-entity.call_of_yucutan.mitnal_monkey,1-entity.call_of_yucutan.undead_warrior,1-entity.call_of_yucutan.updater,10-entity.cataclysm.amethyst_crab,10-entity.cataclysm.ancient_remnant,6-entity.cataclysm.coral_golem,9-entity.cataclysm.coralssus,2-entity.cataclysm.deepling,2-entity.cataclysm.deepling_angler,4-entity.cataclysm.deepling_brute,3-entity.cataclysm.deepling_priest,3-entity.cataclysm.deepling_warlock,8-entity.cataclysm.ender_golem,10-entity.cataclysm.ender_guardian,1-entity.cataclysm.endermaptera,10-entity.cataclysm.ignis,4-entity.cataclysm.ignited_berserker,5-entity.cataclysm.ignited_revenant,10-entity.cataclysm.kobolediator,2-entity.cataclysm.koboleton,1-entity.cataclysm.lionfish,7-entity.cataclysm.modern_remnant,3-entity.cataclysm.nameless_sorcerer,10-entity.cataclysm.netherite_monstrosity,6-entity.cataclysm.the_baby_leviathan,10-entity.cataclysm.the_harbinger,10-entity.cataclysm.the_leviathan,8-entity.cataclysm.the_prowler,2-entity.cataclysm.the_watcher,8-entity.cataclysm.wadjet,10-entity.celestisynth.tempest,1-entity.crittersandcompanions.dragonfly,1-entity.crittersandcompanions.dumbo_octopus,1-entity.crittersandcompanions.ferret,1-entity.crittersandcompanions.jumping_spider,1-entity.crittersandcompanions.koi_fish,1-entity.crittersandcompanions.leaf_insect,1-entity.crittersandcompanions.otter,1-entity.crittersandcompanions.red_panda,1-entity.crittersandcompanions.sea_bunny,1-entity.crittersandcompanions.shima_enaga,2-entity.deeperdarker.sculk_centipede,1-entity.deeperdarker.sculk_leech,1-entity.deeperdarker.sculk_snapper,3-entity.deeperdarker.shattered,6-entity.deeperdarker.shriek_worm,10-entity.deeperdarker.stalker,3-entity.dummmmmmy.target_dummy,2-entity.easy_npc.allay,1-entity.easy_npc.cat,1-entity.easy_npc.chicken,2-entity.easy_npc.drowned,1-entity.easy_npc.fairy,2-entity.easy_npc.humanoid,2-entity.easy_npc.humanoid_slim,2-entity.easy_npc.husk,6-entity.easy_npc.iron_golem,2-entity.easy_npc.skeleton,2-entity.easy_npc.stray,2-entity.easy_npc.villager,2-entity.easy_npc.wither_skeleton,2-entity.easy_npc.zombie,2-entity.easy_npc.zombie_villager,2-entity.eidolon.giant_skeleton,3-entity.eidolon.necromancer,1-entity.eidolon.raven,1-entity.eidolon.slimy_slug,2-entity.eidolon.wraith,3-entity.eidolon.zombie_brute,2-entity.endermanoverhaul.axolotl_pet_enderman,3-entity.endermanoverhaul.badlands_enderman,3-entity.endermanoverhaul.cave_enderman,2-entity.endermanoverhaul.coral_enderman,2-entity.endermanoverhaul.crimson_forest_enderman,3-entity.endermanoverhaul.dark_oak_enderman,3-entity.endermanoverhaul.desert_enderman,3-entity.endermanoverhaul.end_enderman,5-entity.endermanoverhaul.end_islands_enderman,2-entity.endermanoverhaul.flower_fields_enderman,3-entity.endermanoverhaul.hammerhead_pet_enderman,4-entity.endermanoverhaul.ice_spikes_enderman,3-entity.endermanoverhaul.mushroom_fields_enderman,3-entity.endermanoverhaul.nether_wastes_enderman,3-entity.endermanoverhaul.pet_enderman,3-entity.endermanoverhaul.savanna_enderman,1-entity.endermanoverhaul.scarab,2-entity.endermanoverhaul.snowy_enderman,2-entity.endermanoverhaul.soulsand_valley_enderman,1-entity.endermanoverhaul.spirit,3-entity.endermanoverhaul.swamp_enderman,3-entity.endermanoverhaul.warped_forest_enderman,3-entity.endermanoverhaul.windswept_hills_enderman,3-entity.grimoireofgaia.ant,3-entity.grimoireofgaia.ant_hill,3-entity.grimoireofgaia.ant_salvager,5-entity.grimoireofgaia.anubis,3-entity.grimoireofgaia.arachne,5-entity.grimoireofgaia.banshee,3-entity.grimoireofgaia.bee,5-entity.grimoireofgaia.behender,5-entity.grimoireofgaia.bone_knight,3-entity.grimoireofgaia.cecaelia,3-entity.grimoireofgaia.centaur,1-entity.grimoireofgaia.chest,3-entity.grimoireofgaia.cobble_golem,5-entity.grimoireofgaia.cobblestone_golem,3-entity.grimoireofgaia.creep,3-entity.grimoireofgaia.creeper_girl,1-entity.grimoireofgaia.cyan_flower,3-entity.grimoireofgaia.cyclops,3-entity.grimoireofgaia.deathword,3-entity.grimoireofgaia.dryad,3-entity.grimoireofgaia.dullahan,5-entity.grimoireofgaia.dwarf,5-entity.grimoireofgaia.ender_dragon_girl,3-entity.grimoireofgaia.ender_eye,3-entity.grimoireofgaia.ender_girl,5-entity.grimoireofgaia.flesh_lich,5-entity.grimoireofgaia.gelatinous_slime,3-entity.grimoireofgaia.goblin,2-entity.grimoireofgaia.goblin_feral,1-entity.grimoireofgaia.gravemite,3-entity.grimoireofgaia.gryphon,3-entity.grimoireofgaia.harpy,3-entity.grimoireofgaia.horse,3-entity.grimoireofgaia.hunter,3-entity.grimoireofgaia.kobold,3-entity.grimoireofgaia.mandragora,3-entity.grimoireofgaia.matango,5-entity.grimoireofgaia.mermaid,3-entity.grimoireofgaia.mimic,9-entity.grimoireofgaia.minotaur,5-entity.grimoireofgaia.minotaurus,3-entity.grimoireofgaia.mummy,5-entity.grimoireofgaia.naga,5-entity.grimoireofgaia.nine_tails,3-entity.grimoireofgaia.oni,3-entity.grimoireofgaia.orc,3-entity.grimoireofgaia.satyress,5-entity.grimoireofgaia.shaman,5-entity.grimoireofgaia.sharko,3-entity.grimoireofgaia.siren,3-entity.grimoireofgaia.slime_girl,3-entity.grimoireofgaia.sludge_girl,9-entity.grimoireofgaia.sphinx,3-entity.grimoireofgaia.sporeling,5-entity.grimoireofgaia.spriggan,3-entity.grimoireofgaia.succubus,3-entity.grimoireofgaia.toad,3-entity.grimoireofgaia.trader,9-entity.grimoireofgaia.valkyrie,3-entity.grimoireofgaia.werecat,5-entity.grimoireofgaia.witch,3-entity.grimoireofgaia.wither_cow,3-entity.grimoireofgaia.wizard_harpy,5-entity.grimoireofgaia.yuki_onna,4-entity.irons_spellbooks.apothecarist,4-entity.irons_spellbooks.archevoker,2-entity.irons_spellbooks.catacombs_zombie,4-entity.irons_spellbooks.citadel_keeper,4-entity.irons_spellbooks.cryomancer,4-entity.irons_spellbooks.cultist,10-entity.irons_spellbooks.dead_king,10-entity.irons_spellbooks.dead_king_corpse,2-entity.irons_spellbooks.debug_wizard,2-entity.irons_spellbooks.firefly_swarm,1-entity.irons_spellbooks.frozen_humanoid,2-entity.irons_spellbooks.magehunter_vindicator,2-entity.irons_spellbooks.necromancer,4-entity.irons_spellbooks.priest,4-entity.irons_spellbooks.pyromancer,2-entity.irons_spellbooks.root,2-entity.irons_spellbooks.sculk_tentacle,2-entity.irons_spellbooks.spectral_hammer,1-entity.irons_spellbooks.spectral_steed,2-entity.irons_spellbooks.summoned_polar_bear,2-entity.irons_spellbooks.summoned_skeleton,1-entity.irons_spellbooks.summoned_vex,2-entity.irons_spellbooks.summoned_zombie,2-entity.irons_spellbooks.wisp,2-entity.minecraft.allay,2-entity.minecraft.armor_stand,1-entity.minecraft.axolotl,1-entity.minecraft.bat,1-entity.minecraft.bee,2-entity.minecraft.blaze,2-entity.minecraft.camel,1-entity.minecraft.cat,1-entity.minecraft.cave_spider,1-entity.minecraft.chicken,1-entity.minecraft.cod,1-entity.minecraft.cow,2-entity.minecraft.creeper,1-entity.minecraft.dolphin,3-entity.minecraft.donkey,2-entity.minecraft.drowned,5-entity.minecraft.elder_guardian,10-entity.minecraft.ender_dragon,3-entity.minecraft.enderman,1-entity.minecraft.endermite,2-entity.minecraft.evoker,1-entity.minecraft.fox,1-entity.minecraft.frog,1-entity.minecraft.ghast,6-entity.minecraft.giant,1-entity.minecraft.glow_squid,1-entity.minecraft.goat,2-entity.minecraft.guardian,3-entity.minecraft.hoglin,3-entity.minecraft.horse,2-entity.minecraft.husk,2-entity.minecraft.illusioner,6-entity.minecraft.iron_golem,3-entity.minecraft.llama,2-entity.minecraft.magma_cube,1-entity.minecraft.mooshroom,3-entity.minecraft.mule,1-entity.minecraft.ocelot,2-entity.minecraft.panda,1-entity.minecraft.parrot,2-entity.minecraft.phantom,1-entity.minecraft.pig,1-entity.minecraft.piglin,3-entity.minecraft.piglin_brute,2-entity.minecraft.pillager,2-entity.minecraft.polar_bear,1-entity.minecraft.pufferfish,1-entity.minecraft.rabbit,6-entity.minecraft.ravager,1-entity.minecraft.salmon,1-entity.minecraft.sheep,2-entity.minecraft.shulker,1-entity.minecraft.silverfish,2-entity.minecraft.skeleton,1-entity.minecraft.skeleton_horse,2-entity.minecraft.slime,10-entity.minecraft.warden,7-entity.twilightforest.alpha_yeti,2-entity.twilightforest.knight_phantom,2-entity.twilightforest.naga,4-entity.twilightforest.snow_queen,4-entity.twilightforest.quest_ram,4-entity.twilightforest.minoshroom,5-entity.twilightforest.ur_ghast,3-entity.twilightforest.hydra,3-entity.twilightforest.adherent,3-entity.twilightforest.kobold,3-entity.twilightforest.armored_giant,3-entity.twilightforest.bighorn_sheep,3-entity.twilightforest.blockchain_goblin,3-entity.twilightforest.boar,3-entity.twilightforest.carminite_broodling,3-entity.twilightforest.carminite_ghastling,3-entity.twilightforest.carminite_ghastguard,3-entity.twilightforest.carminite_golem,3-entity.twilightforest.death_tome,3-entity.twilightforest.deer,3-entity.twilightforest.dwarf_rabbit,3-entity.twilightforest.fire_beetle,3-entity.twilightforest.giant_miner,3-entity.twilightforest.hostile_wolf,3-entity.twilightforest.hedge_spider,3-entity.twilightforest.helmet_crab,3-entity.twilightforest.lower_goblin_knight,3-entity.twilightforest.troll,3-entity.twilightforest.ice_crystal,3-entity.twilightforest.king_spider,3-entity.twilightforest.stable_ice_core,3-entity.twilightforest.raven,3-entity.twilightforest.swarm_spider,3-entity.twilightforest.redcap_sapper,3-entity.twilightforest.yeti,3-entity.twilightforest.snow_guardian,3-entity.twilightforest.pinch_beetle,3-entity.twilightforest.squirrel,3-entity.twilightforest.wraith,3-entity.twilightforest.winter_wolf,3-entity.twilightforest.mist_wolf,3-entity.twilightforest.penguin,3-entity.twilightforest.mosquito_swarm,3-entity.twilightforest.slime_beetle,3-entity.twilightforest.unstable_ice_core,9-entity.celestisynth.traverser,4-entity.twilightforest.alpha_yeti,8-entity.dragonmounts.dragon,4-entity.goblins_tyranny.mini_goblin_1,4-entity.goblins_tyranny.mini_drunk_gob_1,4-entity.goblins_tyranny.mini_goblin_2,4-entity.goblins_tyranny.mini_drunk_gob_2,4-entity.goblins_tyranny.mini_goblin_3,4-entity.goblins_tyranny.mini_drunk_gob_3,4-entity.goblins_tyranny.leader_goblin,4-entity.goblins_tyranny.bartender_goblin,4-entity.goblins_tyranny.blacksmith_goblin,4-entity.goblins_tyranny.shaman_goblin,4-entity.goblins_tyranny.goblin_huntsman,4-entity.goblins_tyranny.goblin_hunter,4-entity.goblins_tyranny.engineeress_goblin,4-entity.goblins_tyranny.droblin,4-entity.goblins_tyranny.engineer_goblin,4-entity.goblins_tyranny.champion_goblin,4-entity.goblins_tyranny.bard,4-entity.goblins_tyranny.merchant,4-entity.goblins_tyranny.knight_goblin,";
    }
}
