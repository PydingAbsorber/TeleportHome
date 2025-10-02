package com.pyding.tp;

import com.pyding.tp.network.PacketHandler;
import com.pyding.tp.util.ConfigHandler;
import com.pyding.tp.event.EventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(TeleportHome.MODID)
public class TeleportHome
{
    public static final String MODID = "tp";
    public static EventHandler eventHandler;

    public TeleportHome()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        eventHandler = new EventHandler();
        MinecraftForge.EVENT_BUS.register(eventHandler);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ConfigHandler.COMMON_SPEC);
    }

    private void commonSetup(final FMLCommonSetupEvent event)
    {
        PacketHandler.register();
    }
}
