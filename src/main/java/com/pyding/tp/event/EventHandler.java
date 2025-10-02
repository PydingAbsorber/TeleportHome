package com.pyding.tp.event;

import com.pyding.tp.TeleportHome;
import com.pyding.tp.command.TPCommands;
import com.pyding.tp.util.ConfigHandler;
import com.pyding.tp.util.TPUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = TeleportHome.MODID)
public class EventHandler {

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        TPCommands.register(event.getDispatcher());
    }

    @SubscribeEvent
    public static void login(PlayerEvent.PlayerLoggedInEvent event) {
        TPUtil.sync();
    }

    @SubscribeEvent
    public static void hit(LivingHurtEvent event) {
        if(event.getEntity() instanceof Player player)
            player.getPersistentData().putLong("TPHit",System.currentTimeMillis() + ConfigHandler.COMMON.hitTime.get());
    }
}
