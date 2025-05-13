package com.pyding.cs;

import com.github.L_Ender.cataclysm.init.ModGroup;
import com.pyding.cs.items.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(CataclysmSummons.MODID)
public class CataclysmSummons
{
    public static final String MODID = "cs";

    public CataclysmSummons()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        ModItems.register(modEventBus);
    }

}
