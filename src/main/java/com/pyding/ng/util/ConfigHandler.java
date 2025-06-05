package com.pyding.ng.util;

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
        public final ForgeConfigSpec.ConfigValue zones;
        public final ForgeConfigSpec.DoubleValue zoneMaximum;
        public final ForgeConfigSpec.IntValue zonesPerPlayer;


        public Common(ForgeConfigSpec.Builder builder) {
            zones = builder.comment("Zones: ").define("zones","");
            zoneMaximum = builder.comment("Maximum range for zone from x to z for players without creative. F.e x1,z1=113, x2,z2=-200, range=313").defineInRange("zoneMaximum",100,0,Float.MAX_VALUE);
            zonesPerPlayer = builder.comment("Zones per player maximum without creative").defineInRange("zonesPerPlayer",1,0,Integer.MAX_VALUE);
        }
    }
}
