package com.pyding.ng.network.packets;

import com.pyding.ng.util.ConfigHandler;
import com.pyding.ng.util.ZoneUtil;
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
        ZoneUtil.zones = ZoneUtil.parseInput(message);
    }
}
