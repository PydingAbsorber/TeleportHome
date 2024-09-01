package com.pyding.at.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PlayerCapabilityProviderAT implements ICapabilityProvider, INBTSerializable<CompoundTag> {
    public static Capability<PlayerCapabilityAT> playerCap = CapabilityManager.get(new CapabilityToken<PlayerCapabilityAT>() {});
    private PlayerCapabilityAT capabilityVP = null;
    private final LazyOptional<PlayerCapabilityAT> optional = LazyOptional.of(this::createPlayerCapability);
    private PlayerCapabilityAT createPlayerCapability(){
        if(this.capabilityVP == null){
            this.capabilityVP = new PlayerCapabilityAT();
        }
        return this.capabilityVP;
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if(cap == playerCap){
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        createPlayerCapability().saveNBT(nbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        createPlayerCapability().loadNBT(nbt);
    }
}
