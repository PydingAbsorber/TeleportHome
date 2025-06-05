package com.pyding.ng.event;

import com.pyding.ng.NoGrief;
import com.pyding.ng.command.NGCommands;
import com.pyding.ng.util.ZoneUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDestroyBlockEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.pyding.ng.util.ZoneUtil.canInteract;

@Mod.EventBusSubscriber(modid = NoGrief.MODID)
public class EventHandler {

    public static Map<Player, List<BlockPos>> zoneMarks = new HashMap<>();

    @SubscribeEvent
    public static void blockBreak(BlockEvent.BreakEvent event){
        Player player = event.getPlayer();
        if(!canInteract(player,event.getPos()))
            event.setCanceled(true);
    }

    @SubscribeEvent
    public static void blockPlace(BlockEvent.EntityPlaceEvent event){
        if(ZoneUtil.isInZone(event.getPos())){
            if(!(event.getEntity() instanceof Player player))
                event.setCanceled(true);
            else if(!canInteract(player,event.getPos()))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void blockMultiPlace(BlockEvent.EntityMultiPlaceEvent event){
        if(ZoneUtil.isInZone(event.getPos())){
            if(!(event.getEntity() instanceof Player player))
                event.setCanceled(true);
            else if(!canInteract(player,event.getPos()))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void blockDestroy(LivingDestroyBlockEvent event){
        if(ZoneUtil.isInZone(event.getPos())){
            if(!(event.getEntity() instanceof Player player))
                event.setCanceled(true);
            else if(!canInteract(player,event.getPos()))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void entityJoin(EntityJoinLevelEvent event){
        if(!ZoneUtil.canSpawn(event.getEntity().getOnPos())){
            if(!(event.getEntity() instanceof Player) && !isNpc(event.getEntity().getType()) && !(event.getEntity() instanceof ItemEntity))
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void attackEvent(LivingAttackEvent event){
        if(ZoneUtil.isInStrictZone(event.getEntity().getOnPos())){
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void damageEvent(LivingHurtEvent event){
        if(ZoneUtil.isInStrictZone(event.getEntity().getOnPos())){
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        NGCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event){
        List<BlockPos> list = new ArrayList<>();
        list.add(null);
        list.add(null);
        zoneMarks.put(event.getEntity(), list);
    }

    @SubscribeEvent
    public static void use(PlayerInteractEvent.LeftClickBlock event){
        Player player = event.getEntity();
        if(!ZoneUtil.canUse(player,event.getPos()))
            event.setCanceled(true);
        if(event.getItemStack().is(Items.WOODEN_AXE) && !player.level().isClientSide()){
            zoneMarks.get(player).set(0, event.getPos());
            player.sendSystemMessage(Component.literal("First position is set to " + event.getPos()));
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void use(PlayerInteractEvent.RightClickBlock event){
        Player player = event.getEntity();
        if(!ZoneUtil.canUse(player,event.getPos()))
            event.setCanceled(true);
        if(event.getItemStack().is(Items.WOODEN_AXE) && !player.level().isClientSide()){
            zoneMarks.get(player).set(1, event.getPos());
            player.sendSystemMessage(Component.literal("Second position is set to " + event.getPos()));
            event.setCanceled(true);
        }
    }

    public static boolean isNpc(EntityType<?> type){
        if(type.toString().contains("easy_npc"))
            return true;
        return false;
    }
}
