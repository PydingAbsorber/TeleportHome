package com.pyding.at.util;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class ConfigHandler {
    public static final ConfigHandler.Common COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    public ConfigHandler() {
    }

    static {
        Pair<ConfigHandler.Common, ForgeConfigSpec> specPair = (new ForgeConfigSpec.Builder()).configure(ConfigHandler.Common::new);
        COMMON_SPEC = (ForgeConfigSpec)specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static class Common {
        public final ForgeConfigSpec.IntValue maxTier;
        public final ForgeConfigSpec.BooleanValue creativeMax;
        public final ForgeConfigSpec.BooleanValue enableExp;
        public final ForgeConfigSpec.DoubleValue startingExpPercent;
        public final ForgeConfigSpec.BooleanValue pickUp;
        public final ForgeConfigSpec.BooleanValue craftLock;
        public final ForgeConfigSpec.IntValue damagePercentBonus;
        public final ForgeConfigSpec.IntValue durabilityPercentBonus;
        public final ForgeConfigSpec.IntValue armorPercentBonus;
        public final ForgeConfigSpec.IntValue attackPercentBonus;
        public final ForgeConfigSpec.LongValue timeToDrop;
        public final ForgeConfigSpec.ConfigValue itemTiers;
        public final ForgeConfigSpec.ConfigValue entityTiers;
        public final ForgeConfigSpec.ConfigValue blockTiers;

        public Common(ForgeConfigSpec.Builder builder) {
            maxTier = builder.comment("Defines maximum Tier value.").defineInRange("maxTier", 10, 1, 2100000000);
            creativeMax = builder.comment("Enables max Tier in Creative.").define("creativeMax", true);
            enableExp = builder.comment("Enables exp system to lvl up Tier.").define("enableExp", true);
            startingExpPercent = builder.comment("Percentage for second Tier and higher.").defineInRange("startingExpPercent", 0.3, 0.01, 1);
            pickUp = builder.comment("Defines should player pick up items with higher Tier or not.").define("pickUp", true);
            craftLock = builder.comment("Set to true if player can't craft higher Tier item.").define("craftLock", false);
            itemTiers = builder.comment("Tiers for Items: ").define("itemTiers","");
            entityTiers = builder.comment("Tiers for Entities: ").define("entityTiers","");
            blockTiers = builder.comment("Tiers for Blocks: ").define("blockTiers","");
            timeToDrop = builder.comment("Time until items with higher Tier will drop from player. Set to 0 to disable.").defineInRange("timeToDrop", 5000, 0, Long.MAX_VALUE);

            damagePercentBonus = builder.comment("Maximum bonus from max Tier tool damage dealt to Tier 1 creature. Works reverse also.").defineInRange("damagePercentBonus", 400, 1, 2100000000);
            durabilityPercentBonus = builder.comment("Maximum durability bonus for Max Tier.").defineInRange("durabilityPercentBonus", 150, 1, 2100000000);
            armorPercentBonus = builder.comment("Maximum armor bonus for Max Tier.").defineInRange("armorPercentBonus", 150, 1, 2100000000);
            attackPercentBonus = builder.comment("Maximum damage bonus for Max Tier.").defineInRange("attackPercentBonus", 200, 1, 2100000000);
        }


    }
}
