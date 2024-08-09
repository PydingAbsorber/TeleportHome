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

    public static String getBaseTiers(){
        return "3-item.aquaculture.neptunium_fishing_rod,4-item.botania.mining_ring,1-item.irons_spellbooks.silver_ring,1-item.eidolon.silver_helmet,1-item.eidolon.silver_chestplate,1-item.eidolon.silver_leggings,1-item.eidolon.silver_boots,1-item.eidolon.silver_sword,1-item.eidolon.silver_pickaxe,1-item.eidolon.silver_axe,1-item.eidolon.silver_shovel,1-item.eidolon.silver_hoe,1-item.aether.holystone_sword,1-item.aether.holystone_shovel,1-item.aether.holystone_pickaxe,1-item.aether.holystone_axe,1-item.aether.holystone_hoe,1-item.alexscaves.diving_helmet,1-item.alexscaves.diving_chestplate,1-item.alexscaves.diving_leggings,1-item.alexscaves.diving_boots,1-item.minecraft.diamond,1-item.farmersdelight.flint_knife,1-item.farmersdelight.golden_knife,1-item.botania.manasteel_boots,1-item.botania.manasteel_leggings,1-item.botania.manasteel_chestplate,1-item.botania.manasteel_helmet,1-item.botania.manasteel_shears,1-item.botania.manasteel_sword,1-item.botania.manasteel_axe,1-item.botania.manasteel_shovel,1-item.botania.manasteel_pick,1-item.botania.manaweave_chestplate,1-item.botania.manaweave_leggings,1-item.botania.manaweave_boots,1-item.botania.manaweave_helmet,1-item.the_bumblezone.stinger_spear,1-item.alexscaves.limestone_spear,1-item.grimoireofgaia.metal_dagger,1-item.minecraft.fishing_rod,1-item.minecraft.diamond_sword,1-item.minecraft.diamond_helmet,1-item.minecraft.diamond_chestplate,1-item.minecraft.diamond_leggings,1-item.minecraft.diamond_boots,1-item.botania.manasteel_ingot,1-item.farmersdelight.iron_knife,1-item.farmersdelight.diamond_knife,1-item.minecraft.iron_sword,1-item.minecraft.iron_shovel,1-item.minecraft.iron_axe,1-item.minecraft.iron_hoe,1-item.minecraft.iron_pickaxe,1-item.minecraft.iron_helmet,1-item.minecraft.iron_chestplate,1-item.minecraft.iron_leggings,1-item.minecraft.iron_boots,1-item.minecraft.chainmail_helmet,1-item.minecraft.chainmail_chestplate,1-item.minecraft.chainmail_leggings,1-item.minecraft.chainmail_boots,1-item.minecraft.golden_helmet,1-item.minecraft.golden_chestplate,1-item.minecraft.golden_leggings,1-item.minecraft.golden_boots,1-item.minecraft.golden_axe,1-item.minecraft.golden_sword,1-item.minecraft.golden_shovel,1-item.minecraft.golden_pickaxe,1-item.minecraft.golden_axe,1-item.minecraft.golden_hoe,1-item.minecraft.leather_helmet,1-item.minecraft.leather_chestplate,1-item.minecraft.leather_boots,1-item.minecraft.leather_leggings,2-item.minecraft.netherite_ingot,2-item.botania.ender_dagger,2-item.aquamirae.remnants_saber,2-item.aquamirae.poisoned_blade,2-item.aether.zanite_sword,2-item.aether.zanite_shovel,2-item.aether.zanite_pickaxe,2-item.aether.zanite_axe,2-item.aether.zanite_hoe,2-item.aether.gravitite_sword,2-item.aether.gravitite_shovel,2-item.aether.gravitite_pickaxe,2-item.aether.gravitite_axe,2-item.aether.gravitite_hoe,2-item.twilightforest.phantom_helmet,2-item.twilightforest.phantom_chestplate,2-item.twilightforest.naga_chestplate,2-item.twilightforest.naga_leggings,2-item.aether.flaming_sword,2-item.alexscaves.hazmat_mask,2-item.alexscaves.hazmat_chestplate,2-item.alexscaves.hazmat_leggings,2-item.alexscaves.hazmat_boots,2-item.enigmaticlegacy.the_acknowledgment,2-item.irons_spellbooks.heavy_chain_necklace,2-item.twilightforest.steeleaf_ingot,2-item.twilightforest.steeleaf_helmet,2-item.twilightforest.steeleaf_chestplate,2-item.twilightforest.steeleaf_leggings,2-item.twilightforest.steeleaf_boots,2-item.twilightforest.steeleaf_sword,2-item.twilightforest.steeleaf_shovel,2-item.twilightforest.steeleaf_pickaxe,2-item.twilightforest.steeleaf_axe,2-item.twilightforest.steeleaf_hoe,2-item.call_of_yucutan.obsidian_spear,2-item.call_of_yucutan.obsidian_tecpatl,2-item.call_of_yucutan.flint_spear,2-item.call_of_yucutan.silex_tecpatl,2-item.call_of_yucutan.wooden_spear,2-item.call_of_yucutan.wooden_tecpatl,2-item.call_of_yucutan.macuahuitl,2-item.twilightforest.arctic_helmet,2-item.twilightforest.arctic_chestplate,2-item.twilightforest.arctic_leggings,2-item.twilightforest.arctic_boots,2-item.mowziesmobs.naga_fang_dagger,2-item.aquaculture.iron_fishing_rod,2-item.twilightforest.ironwood_ingot,2-item.farmersdelight.netherite_knife,2-item.minecraft.netherite_shovel,2-item.minecraft.netherite_pickaxe,2-item.minecraft.netherite_axe,2-item.minecraft.netherite_hoe,2-item.minecraft.turtle_helmet,2-item.the_bumblezone.bumble_bee_chestplate_1,2-item.the_bumblezone.bumble_bee_chestplate_2,2-item.the_bumblezone.bumble_bee_chestplate_trans_1,2-item.the_bumblezone.bumble_bee_chestplate_trans_2,2-item.the_bumblezone.honey_bee_leggings_1,2-item.the_bumblezone.carpenter_bee_boots_1,2-item.the_bumblezone.carpenter_bee_boots_2,2-item.the_bumblezone.honey_bee_leggings_2,3-item.botania.mana_gun,3-item.botania.elementium_ingot,3-item.aquamirae.three_bolt_helmet,3-item.aquamirae.three_bolt_suit,3-item.aquamirae.three_bolt_leggings,3-item.aquamirae.three_bolt_boots,3-item.aether.valkyrie_hoe,3-item.aether.valkyrie_axe,3-item.aether.valkyrie_pickaxe,3-item.aether.valkyrie_shovel,3-item.aether.valkyrie_lance,3-item.twilightforest.fiery_helmet,3-item.twilightforest.fiery_chestplate,3-item.twilightforest.fiery_leggings,3-item.twilightforest.fiery_boots,3-item.twilightforest.fiery_sword,3-item.twilightforest.fiery_pickaxe,3-item.aether.lightning_sword,3-item.unusualfishmod.depth_scythe,3-item.irons_spellbooks.keeper_flamberge,3-block.botania.endoflame,3-item.eidolon.reaper_scythe,3-item.eidolon.cleaving_axe,3-item.irons_spellbooks.cryomancer_helmet,3-item.irons_spellbooks.cryomancer_chestplate,3-item.irons_spellbooks.cryomancer_leggings,3-item.irons_spellbooks.cryomancer_boots,3-item.irons_spellbooks.priest_helmet,3-item.irons_spellbooks.priest_chestplate,3-item.irons_spellbooks.priest_leggings,3-item.irons_spellbooks.priest_boots,3-item.irons_spellbooks.plagued_helmet,3-item.irons_spellbooks.plagued_chestplate,3-item.irons_spellbooks.plagued_leggings,3-item.irons_spellbooks.plagued_boots,3-item.irons_spellbooks.copper_spell_book,3-item.cataclysm.coral_spear,3-item.aquaculture.gold_fishing_rod,3-item.twilightforest.fiery_ingot,3-item.irons_spellbooks.pumpkin_boots,3-item.irons_spellbooks.pumpkin_leggings,3-item.irons_spellbooks.pumpkin_chestplate,3-item.irons_spellbooks.pumpkin_helmet,3-item.irons_spellbooks.wandering_magician_boots,3-item.irons_spellbooks.wandering_magician_leggings,3-item.irons_spellbooks.wandering_magician_chestplate,3-item.irons_spellbooks.wandering_magician_helmet,3-item.irons_spellbooks.cultist_boots,3-item.irons_spellbooks.cultist_leggings,3-item.irons_spellbooks.cultist_chestplate,3-item.irons_spellbooks.cultist_helmet,3-item.irons_spellbooks.archevoker_boots,3-item.irons_spellbooks.archevoker_leggings,3-item.irons_spellbooks.electromancer_leggings,3-item.irons_spellbooks.archevoker_chestplate,3-item.irons_spellbooks.pyromancer_helmet,3-item.irons_spellbooks.pyromancer_chestplate,3-item.irons_spellbooks.pyromancer_leggings,3-item.irons_spellbooks.pyromancer_boots,3-item.irons_spellbooks.electromancer_helmet,3-item.irons_spellbooks.electromancer_chestplate,3-item.irons_spellbooks.electromancer_boots,3-item.irons_spellbooks.archevoker_helmet,4-item.aquamirae.fin_cutter,4-item.aquamirae.terrible_helmet,4-item.aquamirae.terrible_chestplate,4-item.aquamirae.terrible_leggings,4-item.aquamirae.terrible_boots,4-item.call_of_yucutan.jades_helmet,4-item.call_of_yucutan.jades_chestplate,4-item.call_of_yucutan.jades_leggings,4-item.call_of_yucutan.jades_boots,4-item.eidolon.reversal_pick,4-item.eidolon.sapping_sword,4-item.twilightforest.yeti_helmet,4-item.twilightforest.yeti_chestplate,4-item.twilightforest.yeti_leggings,4-item.twilightforest.yeti_boots,4-item.aether.vampire_blade,4-item.enigmaticlegacy.animal_guidebook,4-item.enigmaticlegacy.hunter_guidebook,4-item.irons_spellbooks.magehunter,4-item.irons_spellbooks.cast_time_ring,4-item.irons_spellbooks.iron_spell_book,4-item.rats.plague_scythe,4-item.aquamirae.dagger_of_greed,4-item.bosses_of_mass_destruction.earthdive_spear,4-item.aquaculture.diamond_fishing_rod,4-item.minecraft.end_crystal,4-block.relics.researching_table,4-item.call_of_yucutan.jade_sword,4-item.call_of_yucutan.jade_pickaxe,4-item.call_of_yucutan.jade_axe,4-item.call_of_yucutan.jade_shovel,4-item.call_of_yucutan.jade_hoe,4-block.minecraft.smithing_table,4-block.minecraft.anvil,4-block.irons_spellbooks.inscription_table,4-item.botania.magnet_ring,4-item.botania.aura_ring,4-item.botania.mining_ring,5-item.bloodmagic.soulsword,5-item.bloodmagic.soulpickaxe,5-item.bloodmagic.soulaxe,5-item.bloodmagic.soulshovel,5-item.bloodmagic.soulscythe,5-item.eidolon.bonelord_helm,5-item.eidolon.bonelord_chestplate,5-item.eidolon.bonelord_greaves,5-item.eidolon.deathbringer_scythe,5-item.alexscaves.primitive_club,5-item.enigmaticlegacy.the_twist,5-item.enigmaticlegacy.enigmatic_elytra,5-item.enigmaticlegacy.infinimeal,5-item.enigmaticlegacy.forbidden_fruit,5-item.irons_spellbooks.lightning_rod,5-item.irons_spellbooks.artificer_cane,5-item.irons_spellbooks.ice_staff,5-item.irons_spellbooks.graybeard_staff,5-item.irons_spellbooks.blood_staff,5-item.irons_spellbooks.gold_spell_book,5-item.irons_spellbooks.rotten_spell_book,5-block.botania.hydroangeas,5-item.mowziesmobs.wrought_axe,5-item.twilightforest.charm_of_keeping_1,5-item.eidolon.prestigious_palm,5-item.eidolon.warlock_boots,5-item.eidolon.warlock_cloak,5-item.eidolon.warlock_hat,5-item.minecraft.enchanted_golden_apple,6-item.botania.terrasteel_ingot,6-item.bhc.heart_amulet,6-item.cataclysm.infernal_forge,6-item.cataclysm.tidal_claws,6-item.cataclysm.void_forge,6-item.cataclysm.meat_shredder,6-item.cataclysm.the_incinerator,6-item.cataclysm.gauntlet_of_bulwark,6-item.cataclysm.gauntlet_of_guard,6-item.cataclysm.bulwark_of_the_flame,6-item.eidolon.warded_mail,6-item.enigmaticlegacy.cursed_scroll,6-item.enigmaticlegacy.avarice_scroll,6-item.enigmaticlegacy.berserk_charm,6-item.enigmaticlegacy.enchanter_pearl,6-item.irons_spellbooks.mana_ring,6-item.irons_spellbooks.diamond_spell_book,6-block.irons_spellbooks.arcane_anvil,6-block.botania.rosa_arcana,6-item.alexscaves.ortholance,6-item.aquaculture.neptunium_fishing_rod,6-block.minecraft.brewing_stand,6-item.botania.magnet_ring_greater,6-item.botania.aura_ring_greater,6-item.botania.reach_ring,7-item.aquamirae.terrible_sword,7-item.botania.star_sword,7-item.botania.thunder_sword,7-item.cataclysm.bone_reptile_helmet,7-item.cataclysm.bone_reptile_chestplate,7-item.eidolon.raven_cloak,7-item.eidolon.void_amulet,7-item.aether.valkyrie_helmet,7-item.aether.valkyrie_chestplate,7-item.aether.valkyrie_leggings,7-item.aether.valkyrie_boots,7-item.aquaculture.neptunium_ingot,7-item.alexscaves.desolate_dagger,7-item.alexscaves.dreadbow,7-item.enigmaticlegacy.ender_slayer,7-item.enigmaticlegacy.guardian_heart,7-item.irons_spellbooks.blaze_spell_book,7-item.irons_spellbooks.necronomicon_spell_book,7-item.irons_spellbooks.evoker_spell_book,7-item.irons_spellbooks.druidic_spell_book,7-item.irons_spellbooks.villager_spell_book,7-block.occultism.dimensional_mineshaft,7-item.irons_spellbooks.shadowwalker_helmet,7-item.irons_spellbooks.shadowwalker_chestplate,7-item.irons_spellbooks.shadowwalker_leggings,7-item.irons_spellbooks.shadowwalker_boots,7-item.irons_spellbooks.netherite_mage_helmet,7-item.irons_spellbooks.netherite_mage_chestplate,7-item.irons_spellbooks.netherite_mage_leggings,7-item.irons_spellbooks.netherite_mage_boots,7-item.twilightforest.charm_of_keeping_2,7-item.twilightforest.magic_beans,7-block.botania.brewery,8-item.aquamirae.coral_lance,8-item.celestisynth.breezebreaker,8-item.celestisynth.poltergeist,8-item.celestisynth.frostbound,8-item.alexscaves.extinction_spear,8-item.mythicbotany.alfsteel_ingot,8-item.mythicbotany.alfsteel_sword,8-item.mythicbotany.alfsteel_pick,8-item.mythicbotany.alfsteel_axe,8-block.mythicbotany.mjoellnir,8-item.enigmaticlegacy.etherium_ingot,8-item.enigmaticlegacy.infernal_shield,8-block.botania.gourmaryllis,8-item.enigmaticlegacy.forbidden_fruit,9-item.bhc.soul_heart_amulet,9-item.celestisynth.solaris,9-item.celestisynth.rainfall_serenity,9-item.alexscaves.resistor_shield,9-item.relics.jellyfish_necklace,9-item.relics.holy_locket,9-item.alexscaves.raygun,9-item.enigmaticlegacy.forbidden_axe,9-item.relics.midnight_robe,9-item.relics.reflection_necklace,9-item.irons_spellbooks.emerald_stoneplate_ring,9-item.irons_spellbooks.cooldown_ring,9-item.irons_spellbooks.dragonskin_spell_book,9-block.botania.dandelifeon,9-block.alexsmobs.transmutation_table,9-item.deeperdarker.warden_sword,9-item.deeperdarker.warden_axe,9-item.deeperdarker.warden_hoe,9-item.deeperdarker.warden_shovel,9-item.deeperdarker.warden_pickaxe,9-item.deeperdarker.warden_helmet,9-item.deeperdarker.warden_chestplate,9-item.deeperdarker.warden_leggings,9-item.deeperdarker.warden_boots,9-item.mowziesmobs.spear,9-item.relics.infinity_ham,9-item.twilightforest.charm_of_keeping_3,9-item.endermanoverhaul.bubble_pearl,9-item.endermanoverhaul.icy_pearl,9-item.endermanoverhaul.crimson_pearl,9-item.endermanoverhaul.warped_pearl,9-item.endermanoverhaul.soul_pearl,9-block.alexscaves.tremorzilla_egg,9-block.alexscaves.mussel,9-item.botania.spawner_mover,10-item.bloodmagic.livinghelmet,10-item.bloodmagic.livingplate,10-item.bloodmagic.livingleggings,10-item.bloodmagic.livingboots,10-item.aquamirae.abyssal_heaume,10-item.aquamirae.abyssal_brigantine,10-item.aquamirae.abyssal_leggings,10-item.aquamirae.abyssal_boots,10-item.aquamirae.divider,10-item.aquamirae.whisper_of_the_abyss,10-item.cataclysm.ignitium_helmet,10-item.cataclysm.ignitium_chestplate,10-item.cataclysm.ignitium_elytra_chestplate,10-item.cataclysm.ignitium_leggings,10-item.cataclysm.ignitium_boots,10-item.celestisynth.aquaflora,10-item.celestisynth.crescentia,10-item.relics.rage_glove,10-item.alexscaves.hood_of_darkness,10-item.alexscaves.cloak_of_darkness,10-item.enigmaticlegacy.fabulous_scroll,10-item.enigmaticlegacy.eldritch_pan,10-item.enigmaticlegacy.the_cube,10-item.enigmaticlegacy.ascension_amulet,10-item.enigmaticlegacy.eldritch_amulet,10-item.irons_spellbooks.netherite_spell_book,10-item.alexsmobs.rocky_chestplate,10-item.alexscaves.totem_of_possession,10-item.alexsmobs.novelty_hat,10-item.enigmaticlegacy.astral_fruit,10-item.enigmaticlegacy.ichor_bottle,10-item.twilightforest.charm_of_life_2,10-item.twilightforest.charm_of_life_1,10-item.enigmaticlegacy.the_judgement,6-item.botania.terra_pick,7-item.enigmaticlegacy.super_magnet_ring,9-item.enigmaticlegacy.heaven_scroll,7-item.enigmaticlegacy.escape_scroll,8-item.enigmaticlegacy.the_infinitum,9-item.enigmaticlegacy.desolation_ring,8-item.enigmaticlegacy.etherium_pickaxe,8-item.enigmaticlegacy.etherium_axe,8-item.enigmaticlegacy.etherium_shovel,8-item.enigmaticlegacy.etherium_sword,8-item.enigmaticlegacy.etherium_scythe,8-item.enigmaticlegacy.etherium_helmet,8-item.enigmaticlegacy.etherium_chestplate,8-item.enigmaticlegacy.etherium_leggings,8-item.enigmaticlegacy.etherium_boots,1-item.aether.lightning_knife,1-item.aether.holy_sword,1-item.aether.candy_cane_sword,1-item.aether.pig_slayer,1-item.aether.hammer_of_kingbdogz,8-item.skilltree.assassin_necklace,8-item.skilltree.bone_quiver,8-item.skilltree.diamond_quiver,9-block.occultism.storage_stabilizer_tier4,8-block.occultism.storage_stabilizer_tier3,3-item.botania.elementium_helmet,3-item.botania.elementium_chestplate,3-item.botania.elementium_leggings,3-item.botania.elementium_boots,3-item.botania.elementium_pickaxe,3-item.botania.elementium_shovel,3-item.botania.elementium_axe,3-item.botania.elementium_sword,3-item.botania.elementium_shears,3-block.mythicbotany.elementium_ore,5-block.mythicbotany.dragonstone_ore,2-block.minecraft.nether_quartz_ore,2-block.alexscaves.spelunkery_table,6-item.cataclysm.bloom_stone_pauldrons,8-item.mythicbotany.alfsteel_helmet,8-item.mythicbotany.alfsteel_chestplate,8-item.mythicbotany.alfsteel_leggings,8-item.mythicbotany.alfsteel_boots,8-item.mythicbotany.mana_ring_greatest,5-item.irons_spellbooks.concentration_amulet,2-item.minecraft.flint_and_steel,5-item.skilltree.simple_necklace,6-item.skilltree.amnesia_scroll,7-item.deeperdarker.sculk_transmitter,7-block.bosses_of_mass_destruction.mob_ward,7-item.aquaculture.neptunium_leggings,7-item.aquaculture.neptunium_boots,7-item.aquaculture.neptunium_chestplate,7-item.aquaculture.neptunium_helmet,7-item.aquaculture.neptunium_sword,7-item.aquaculture.neptunium_hoe,7-item.aquaculture.neptunium_axe,7-item.aquaculture.neptunium_shovel,7-item.aquaculture.neptunium_pickaxe,7-item.aquaculture.neptunium_bow,9-item.aquamirae.rune_of_the_storm,7-item.aquamirae.abyssal_amethyst,7-item.aquamirae.maze_rose,6-item.irons_spellbooks.amethyst_resonance_charm,6-item.aquamirae.poseidons_breakfast,5-item.aquamirae.sea_stew,2-item.twilightforest.magic_map,4-item.twilightforest.alpha_yeti_fur,2-item.minecraft.netherite_boots,2-item.minecraft.netherite_leggings,2-item.minecraft.netherite_chestplate,2-item.minecraft.netherite_helmet,2-item.minecraft.netherite_sword,3-item.alexsmobs.skelewag_sword,2-item.twilightforest.knightmetal_axe,2-item.twilightforest.knightmetal_pickaxe,2-item.twilightforest.knightmetal_ingot,2-item.twilightforest.knightmetal_helmet,2-item.twilightforest.knightmetal_chestplate,2-item.twilightforest.knightmetal_leggings,2-item.twilightforest.knightmetal_boots,2-item.twilightforest.knightmetal_sword,7-block.eidolon.wooden_brewing_stand,1-item.twilightforest.ironwood_helmet,1-item.twilightforest.ironwood_chestplate,1-item.twilightforest.ironwood_leggings,1-item.twilightforest.ironwood_boots,1-item.aether.zanite_helmet,1-item.aether.zanite_chestplate,1-item.aether.zanite_leggings,1-item.aether.zanite_boots,1-item.aether.gravitite_helmet,1-item.aether.gravitite_chestplate,1-item.aether.gravitite_leggings,1-item.aether.gravitite_boots,1-item.aether.obsidian_helmet,1-item.aether.obsidian_chestplate,1-item.aether.obsidian_leggings,1-item.aether.obsidian_boots,1-item.aether.sentry_boots,6-item.botania.terrasteel_helmet,6-item.botania.terrasteel_chestplate,6-item.botania.terrasteel_leggings,6-item.botania.terrasteel_boots,6-item.aether.phoenix_helmet,6-item.aether.phoenix_chestplate,6-item.aether.phoenix_leggings,6-item.aether.phoenix_boots,6-item.aether.phoenix_boots,6-item.aether.phoenix_leggings,8-block.minecraft.enchanting_table,4-block.eidolon.soul_enchanter,6-item.botania.terra_axe,6-item.botania.terra_sword,5-item.minecraft.elytra,5-item.alexsmobs.tarantula_hawk_elytra,5-item.deeperdarker.soul_elytra,3-block.irons_spellbooks.alchemist_cauldron,";
    }
}
