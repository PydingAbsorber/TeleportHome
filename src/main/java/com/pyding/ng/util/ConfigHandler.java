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

        public Common(ForgeConfigSpec.Builder builder) {
            zones = builder.comment("Zones: ").define("zones","");
        }
    }
}
