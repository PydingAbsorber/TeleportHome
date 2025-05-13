package com.pyding.cs.items;

import com.github.L_Ender.cataclysm.init.ModEntities;
import com.github.L_Ender.cataclysm.init.ModTag;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class AbyssSummon extends Item {
    public AbyssSummon(Properties p_41383_) {
        super(p_41383_);
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        player.startUsingItem(hand);
        if (level instanceof ServerLevel serverLevel) {
            BlockPos foundPos = serverLevel.findNearestMapStructure(
                    ModTag.EYE_OF_ABYSS_LOCATED,
                    player.blockPosition(),
                    100,
                    false
            );
            if (foundPos != null) {
                if (foundPos.distSqr(player.blockPosition()) < 20000.0) {
                    LivingEntity entity = ModEntities.THE_LEVIATHAN.get().create(level);
                    entity.absMoveTo(
                            player.getX(),
                            player.getY(),
                            player.getZ()
                    );
                    level.addFreshEntity(entity);
                    itemStack.shrink(1);
                } else {
                    player.displayClientMessage(
                            Component.translatable("summon.fail"),
                            true
                    );
                }
                return InteractionResultHolder.success(itemStack);
            }
        }
        return InteractionResultHolder.fail(itemStack);
    }
}
