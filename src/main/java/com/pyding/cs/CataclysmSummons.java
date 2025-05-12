package com.pyding.cs;

import com.github.L_Ender.cataclysm.init.ModGroup;
import com.mojang.logging.LogUtils;
import com.pyding.cs.items.ModItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(CataclysmSummons.MODID)
public class CataclysmSummons
{
    public static final String MODID = "cs";
    private static final Logger LOGGER = LogUtils.getLogger();



    public CataclysmSummons()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        MinecraftForge.EVENT_BUS.register(this);
        ModItems.register(modEventBus);
        modEventBus.addListener(this::addCreative);         //ModList.get().isLoaded("jei")
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event){
        if(event.getTab() == ModGroup.ITEM.get()){
            event.accept(ModItems.MECH_SUMMON);
            event.accept(ModItems.FLAME_SUMMON);
            event.accept(ModItems.VOID_SUMMON);
            event.accept(ModItems.MONSTROUS_SUMMON);
            event.accept(ModItems.ABYSS_SUMMON);
            event.accept(ModItems.DESERT_SUMMON);
            event.accept(ModItems.CURSED_SUMMON);
        }
    }

}
