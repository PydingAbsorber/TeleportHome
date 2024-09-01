package com.pyding.at.compat;

import net.minecraftforge.fml.ModList;

public class ATCompat {

    public static boolean jeiLoaded() {
        return ModList.get().isLoaded("jei");
    }

    public static boolean curiosLoaded() {
        return ModList.get().isLoaded("curios");
    }

}
