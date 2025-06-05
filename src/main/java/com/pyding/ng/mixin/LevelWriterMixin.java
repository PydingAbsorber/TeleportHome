package com.pyding.ng.mixin;

import com.pyding.ng.event.EventHandler;
import com.pyding.ng.util.ZoneUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Level.class)
public abstract class LevelWriterMixin {

    @Inject(
            method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private void onSetBlock(BlockPos pos, BlockState state, int flags, int recursion, CallbackInfoReturnable<Boolean> cir) {
        if(!ZoneUtil.canSetBlock(pos,state,((Level)(Object)this))) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }

    @Inject(
            method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z",
            at = @At("HEAD"),
            cancellable = true,
            require = 1
    )
    private void onDestroyBlock(BlockPos pos, boolean drop, Entity entity, int recursion, CallbackInfoReturnable<Boolean> cir) {
        if(ZoneUtil.isInStrictZone(pos)) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
