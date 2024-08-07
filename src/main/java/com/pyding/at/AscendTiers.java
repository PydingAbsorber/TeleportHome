package com.pyding.at;

import com.mojang.logging.LogUtils;
import com.pyding.at.client.ClientProxy;
import com.pyding.at.common.CommonProxy;
import com.pyding.at.event.EventHandler;
import com.pyding.at.network.PacketHandler;
import com.pyding.at.util.ConfigHandler;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(AscendTiers.MODID)
public class AscendTiers
{
    public static final String MODID = "at";
    private static final Logger LOGGER = LogUtils.getLogger();

    public static EventHandler eventHandler;

    public static final CommonProxy PROXY = DistExecutor.unsafeRunForDist(() -> ClientProxy::new, () -> CommonProxy::new);

    public AscendTiers()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::postInit);
        eventHandler = new EventHandler();
        MinecraftForge.EVENT_BUS.register(eventHandler);
        FMLJavaModLoadingContext.get().getModEventBus().register(PROXY);
        MinecraftForge.EVENT_BUS.register(PROXY);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, ConfigHandler.COMMON_SPEC); //bye  bye
    }

    private void commonSetup(final FMLCommonSetupEvent event) //pre init
    {
        PacketHandler.register();
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {

    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) //init
        {
            PROXY.initEntityRendering();
        }
    }


    private void postInit(InterModEnqueueEvent event) {
        LOGGER.info("Sending messages to Curios API...");
    }

    public void onLoadComplete(FMLLoadCompleteEvent event) {
        PROXY.loadComplete(event);
    }
}
