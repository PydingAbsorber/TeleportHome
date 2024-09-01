package com.pyding.at.event;

import com.pyding.at.AscendTiers;
import com.pyding.at.capability.PlayerCapabilityProviderAT;
import com.pyding.at.commands.ATCommands;
import com.pyding.at.compat.ATCompat;
import com.pyding.at.compat.ATCurio;
import com.pyding.at.util.ATUtil;
import com.pyding.at.util.ConfigHandler;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;

@Mod.EventBusSubscriber(modid = AscendTiers.MODID)
public class EventHandler {

    @SubscribeEvent
    public void tooltipEvent(ItemTooltipEvent event){
        ItemStack stack = event.getItemStack();
        int tier = ATUtil.getTier(stack);
        if(tier > 0){
            event.getToolTip().add(Component.translatable("at.tier",tier).withStyle(ATUtil.getColor(tier)));
            if (!ATUtil.notIgnored(stack))
                event.getToolTip().add(Component.translatable("at.ignored").withStyle(ATUtil.getColor(tier)));
            Player player = event.getEntity();
            if(player != null) {
                player.getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(cap -> {
                    event.getToolTip().add(Component.translatable("at.get.tier", cap.getTier(player)).withStyle(ATUtil.getColor(cap.getTier(player))));
                    if(ConfigHandler.COMMON.enableExp.get()) {
                        event.getToolTip().add(Component.translatable("at.tier.2", cap.getExp(), ATUtil.getExpNext(cap.getTier(player))).withStyle(ATUtil.getColor(tier)));
                        if (ATUtil.notContains(cap.getItems(), stack.getDescriptionId())) {
                            String token = "";
                            if(ATCompat.jeiLoaded()) {
                                Random random = new Random(tier);
                                StringBuilder result = new StringBuilder();
                                for (int i = 0; i < 3; i++) {
                                    char randomLetter = (char) ('A' + random.nextInt(26));
                                    result.append(randomLetter);
                                }
                                token = result.toString();
                            }
                            event.getToolTip().add(Component.translatable("at.discovered",token).withStyle(ChatFormatting.GRAY));
                        }
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void eatEvent(LivingEntityUseItemEvent.Finish event){
        if(event.getEntity() instanceof Player player) {
            ItemStack stack = event.getResultStack();
            player.getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(cap -> {
                int tier = ATUtil.getTier(stack);
                if(tier > cap.getTier(player) && ATUtil.notIgnored(stack)) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.translatable("at.chat.use", tier));
                }
            });
        }
    }

    @SubscribeEvent
    public static void useEvent(PlayerInteractEvent.RightClickItem event){
        Player player = event.getEntity();
        ItemStack stack = event.getItemStack();
        player.getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(cap -> {
            int tier = ATUtil.getTier(stack);
            if(tier > cap.getTier(player) && ATUtil.notIgnored(stack)) {
                event.setCanceled(true);
                event.setCancellationResult(InteractionResult.FAIL);
                player.sendSystemMessage(Component.translatable("at.chat.use", tier));
            }
        });
    }

    @SubscribeEvent
    public static void craftEvent(PlayerEvent.ItemCraftedEvent event){
        if(ConfigHandler.COMMON.craftLock.get()) {
            ItemStack stack = event.getCrafting();
            Player player = event.getEntity();
            player.getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(cap -> {
                int tier = ATUtil.getTier(stack);
                if (tier > cap.getTier(player) && ATUtil.notIgnored(stack)) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.translatable("at.chat.craft", tier));
                }
            });
        }
    }

    @SubscribeEvent
    public static void tick(LivingEvent.LivingTickEvent event){
        if(event.getEntity() instanceof Player player){
            if(player.tickCount % 20 == 0){
                ATUtil.initMaps(player);
            }
            player.getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(cap -> {
                if(player.getCommandSenderWorld().isClientSide)
                    return;
                if (player.tickCount % 20 == 0) {
                    cap.sync(player);
                }
                for (int i = 0; i < player.getInventory().armor.size(); i++) {
                    ItemStack stack = player.getInventory().armor.get(i);
                    if (ATUtil.getTier(stack) > cap.getTier(player) && ATUtil.notIgnored(stack)) {
                        player.drop(stack, true);
                        player.getInventory().armor.set(i, ItemStack.EMPTY);
                    } else cap.addItem(stack, player);
                }
                if(ATCompat.curiosLoaded()){
                    ATCurio.dropCurios(player,cap.getTier(player));
                }
                if (ATUtil.getTier(player.getOffhandItem()) > cap.getTier(player) && ATUtil.notIgnored(player.getOffhandItem())) {
                    ItemStack stack = player.getOffhandItem();
                    player.drop(stack, true);
                    player.getInventory().offhand.set(0, ItemStack.EMPTY);
                }
                float drop = ConfigHandler.COMMON.timeToDrop.get();
                if (player.tickCount % 20 == 0) {
                    if(drop > 0) {
                        float time = player.getPersistentData().getLong("ATTime");
                        if (time == 0) {
                            List<ItemStack> list = ATUtil.getAllItems(player);
                            boolean display = false;
                            for (ItemStack stack : list) {
                                if (ATUtil.getTier(stack) > cap.getTier(player) && ATUtil.notIgnored(stack)) {
                                    player.getPersistentData().putLong("ATTime", (long) (System.currentTimeMillis() + drop));
                                    display = true;
                                } else cap.addItem(stack, player);
                            }
                            if(display)
                                player.sendSystemMessage(Component.translatable("at.chat.1", (int) (drop / 1000)));
                        } else if (time > System.currentTimeMillis()) {
                            player.sendSystemMessage(Component.translatable("at.chat.1", (int) ((time - System.currentTimeMillis()) / 1000)));
                        } else if (time <= System.currentTimeMillis()) {
                            player.getPersistentData().putLong("ATTime", 0);
                            List<ItemStack> list = ATUtil.getAllItems(player);
                            for (ItemStack stack : list) {
                                if (ATUtil.getTier(stack) > cap.getTier(player) && ATUtil.notIgnored(stack)) {
                                    player.getInventory().removeItem(stack);
                                    player.drop(stack, true);
                                } else cap.addItem(stack, player);
                            }
                        }
                    } else {
                        for (ItemStack stack : ATUtil.getAllItems(player)) {
                            if (!(ATUtil.getTier(stack) > cap.getTier(player)) || !ATUtil.notIgnored(stack)) {
                                cap.addItem(stack, player);
                            }
                        }
                    }
                }
                if (player.tickCount % 20 == 0 && cap.getExp() > 0 && cap.getExp() >= ATUtil.getExpNext(cap.getTier(player)) && ATUtil.getExpNext(cap.getTier(player)) > 0) {
                    cap.addTier(player);
                }
            });
        }
    }


    @SubscribeEvent
    public static void onPlayerPickUp(PlayerEvent.ItemPickupEvent event){
        if(!ConfigHandler.COMMON.pickUp.get()) {
            Player player = event.getEntity();
            ItemStack stack = event.getStack();
            player.getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(cap -> {
                int tier = ATUtil.getTier(stack);
                if(tier > cap.getTier(player) && ATUtil.notIgnored(stack)) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.translatable("at.chat.use", tier));
                }
            });
        }
    }


    ////////////////entities


    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void damageEventLowest(LivingDamageEvent event){
        if(event.getSource() == null || event.getSource().getEntity() == null)
            return;
        int maxTier = ConfigHandler.COMMON.maxTier.get();
        if(event.getSource().getEntity() instanceof Player player) {
            player.getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(cap -> {
                ItemStack stack = player.getMainHandItem();
                int tier = ATUtil.getTier(stack);
                if(cap.getTier(player) == maxTier)
                    tier = maxTier;
                int entityTier = ATUtil.getTier(event.getEntity());
                if(event.getEntity() instanceof Player dealerPlayer) {
                    entityTier = 0;
                    if(cap.getTier(dealerPlayer) == maxTier)
                        entityTier = maxTier;
                    else {
                        for (ItemStack armor : dealerPlayer.getInventory().armor) {
                            entityTier += ATUtil.getTier(armor);
                        }
                        entityTier /= 4;
                    }
                } else if(tier < entityTier && player.getPersistentData().getLong("ATCd") < System.currentTimeMillis()){
                    player.getPersistentData().putLong("ATCd",System.currentTimeMillis()+10000);
                    player.sendSystemMessage(Component.translatable("at.chat.2",entityTier));
                }
                event.setAmount(ATUtil.calculateBonus(event.getAmount(),tier,entityTier,true));
            });
        } else if(event.getEntity() instanceof Player player){
            player.getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(cap -> {
                int tier = 0;
                if(cap.getTier(player) == maxTier)
                    tier = maxTier;
                else {
                    for (ItemStack stack : player.getInventory().armor) {
                        tier += ATUtil.getTier(stack);
                    }
                    tier /= 4;
                }
                int entityTier = 0;
                if(event.getSource().getEntity() instanceof Player dealerPlayer) {
                    entityTier = ATUtil.getTier(dealerPlayer.getMainHandItem());
                    if(cap.getTier(dealerPlayer) == maxTier)
                        entityTier = maxTier;
                }
                else if(event.getSource().getEntity() instanceof LivingEntity entity) entityTier = ATUtil.getTier(entity);
                event.setAmount(ATUtil.calculateBonus(event.getAmount(),tier,entityTier,false));
            });
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void attackEventLowest(LivingAttackEvent event){
        if(event.getSource() != null && event.getSource().getEntity() instanceof Player player) {
            player.getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(cap -> {
                ItemStack stack = player.getItemInHand(player.getUsedItemHand());
                int tier = ATUtil.getTier(stack);
                if(tier > cap.getTier(player) && ATUtil.notIgnored(stack))
                    event.setCanceled(true);
            });
        }
    }

    @SubscribeEvent
    public static void onBreak(BlockEvent.BreakEvent event){
        Player player = event.getPlayer();
        player.getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(cap -> {
            int tier = ATUtil.getTier(player.getCommandSenderWorld().getBlockState(event.getPos()).getBlock());
            if(tier > ATUtil.getTier(player.getMainHandItem())) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.translatable("at.chat.use", tier));
            } else cap.addItem(player.getCommandSenderWorld().getBlockState(event.getPos()).getBlock().asItem().getDefaultInstance(), player);
        });
    }

    @SubscribeEvent
    public static void onPlaced(BlockEvent.EntityPlaceEvent event){
        if(event.getEntity() instanceof Player player) {
            player.getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(cap -> {
                int tier = ATUtil.getTier(event.getPlacedBlock().getBlock());
                boolean ignored = false;
                if(event.getPlacedBlock().getBlock().asItem() == player.getMainHandItem().getItem()){
                    ignored = ATUtil.notIgnored(player.getMainHandItem());
                } else if(event.getPlacedBlock().getBlock().asItem() == player.getOffhandItem().getItem()){
                    ignored = ATUtil.notIgnored(player.getOffhandItem());
                }
                if(tier > cap.getTier(player) && ignored) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.translatable("at.chat.use", tier));
                }
            });
        }
    }

    @SubscribeEvent
    public static void useBlock(PlayerInteractEvent.RightClickBlock event){
        Player player = event.getEntity();
        player.getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(cap -> {
            int tier = ATUtil.getTier(player.getCommandSenderWorld().getBlockState(event.getPos()).getBlock());
            if(tier > cap.getTier(player)) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.translatable("at.chat.use", tier));
            }
        });
    }
    @SubscribeEvent
    public static void interactEvent(PlayerInteractEvent.LeftClickBlock event){
        Player player = event.getEntity();
        player.getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(cap -> {
            int tier = ATUtil.getTier(player.getCommandSenderWorld().getBlockState(event.getPos()).getBlock());
            if(tier > cap.getTier(player)) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.translatable("at.chat.use", tier));
            }
        });
    }

    ///////////////////////other

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        ATCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void capabilityAttach(AttachCapabilitiesEvent<Entity> event){
        if(event.getObject() instanceof Player && !(event.getObject() instanceof FakePlayer)){
            event.addCapability(new ResourceLocation(AscendTiers.MODID, "properties"), new PlayerCapabilityProviderAT());
        }
    }

    @SubscribeEvent
    public static void onPlayerCloned(PlayerEvent.Clone event){
        event.getOriginal().reviveCaps();
        event.getOriginal().getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(oldStore -> {
            event.getEntity().getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(newStore -> {
                newStore.copyNBT(oldStore);
                newStore.sync(event.getEntity());
            });
        });
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void spawnEvent(PlayerEvent.PlayerRespawnEvent event){
        Player player = event.getEntity();
        player.getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(cap -> {
           cap.sync(player);
        });
    }

    @SubscribeEvent
    public static void loginIn(PlayerEvent.PlayerLoggedInEvent event){
        Player player = event.getEntity();
        player.getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(cap -> {
            cap.sync(player);
        });
        ATUtil.initMaps(player);
        ATUtil.getItems();
    }

    @SubscribeEvent
    public static void loginOut(PlayerEvent.PlayerLoggedOutEvent event){
        Player player = event.getEntity();
        player.getCapability(PlayerCapabilityProviderAT.playerCap).ifPresent(cap -> {
            cap.sync(player);
        });
    }
}
