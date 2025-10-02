package com.pyding.tp.util;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigHandler {
    public static final Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    public ConfigHandler() {
    }

    static {
        Pair<Common, ForgeConfigSpec> specPair = (new ForgeConfigSpec.Builder()).configure(Common::new);
        COMMON_SPEC = (ForgeConfigSpec)specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class Common {
        public final ForgeConfigSpec.ConfigValue homes;
        public final ForgeConfigSpec.ConfigValue spawn;
        public final ForgeConfigSpec.IntValue homesPerPlayer;
        public final ForgeConfigSpec.BooleanValue shouldNoHit;
        public final ForgeConfigSpec.LongValue hitTime;

        public Common(ForgeConfigSpec.Builder builder) {
            homes = builder.comment("Homes: ").define("homes","");
            homesPerPlayer = builder.comment("Homes per player maximum without creative if home commands enabled").defineInRange("homesPerPlayer",1,0,Integer.MAX_VALUE);
            spawn = builder.comment("Spawn cords: ").define("spawn","");
            shouldNoHit = builder.comment("Should player no hit before teleportation: ").define("shouldStand",false);
            hitTime = builder.comment("Time required in milies for no hit (1 second is 1000 milies)").defineInRange("standTime",3000,0,Long.MAX_VALUE);
        }
    }
}
