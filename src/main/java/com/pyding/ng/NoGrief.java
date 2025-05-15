package com.pyding.ng;

import com.pyding.ng.network.PacketHandler;
import com.pyding.ng.util.ConfigHandler;
import com.pyding.ng.event.EventHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NoGrief.MODID)
public class NoGrief
{
    public static final String MODID = "ng";
    public static EventHandler eventHandler;

    public NoGrief()
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
