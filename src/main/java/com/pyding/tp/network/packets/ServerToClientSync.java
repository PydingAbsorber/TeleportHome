package com.pyding.tp.network.packets;

import com.pyding.tp.util.TPUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ServerToClientSync {
    private String message;

    public ServerToClientSync(String message) {
        this.message = message;
    }

    public static void encode(ServerToClientSync msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.message);
    }

    public static ServerToClientSync decode(FriendlyByteBuf buf) {
        return new ServerToClientSync(buf.readUtf());
    }

    public static void handle(ServerToClientSync msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handle2(msg.message);
        });

        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle2(String message) {
        TPUtil.homes = TPUtil.parseInput(message);
    }
}
