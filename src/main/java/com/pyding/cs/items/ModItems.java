package com.pyding.cs.items;

import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.pyding.cs.CataclysmSummons.MODID;

public class ModItems {
    public static DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> MECH_SUMMON = ITEMS.register("mech_summon", () -> new MechSummon(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> FLAME_SUMMON = ITEMS.register("flame_summon", () -> new FlameSummon(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> VOID_SUMMON = ITEMS.register("void_summon", () -> new VoidSummon(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> MONSTROUS_SUMMON = ITEMS.register("monstrous_summon", () -> new MonstrousSummon(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> ABYSS_SUMMON = ITEMS.register("abyss_summon", () -> new AbyssSummon(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> DESERT_SUMMON = ITEMS.register("desert_summon", () -> new DesertSummon(new Item.Properties().stacksTo(64)));
    public static final RegistryObject<Item> CURSED_SUMMON = ITEMS.register("cursed_summon", () -> new CursedSummon(new Item.Properties().stacksTo(64)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
