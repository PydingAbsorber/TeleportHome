package com.pyding.at.network.packets;

import com.pyding.at.util.ATUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.UUID;
import java.util.function.Supplier;

public class HashMapClient {
    private String map;
    private int id;

    public HashMapClient(String map, int id) {
        this.map = map;
        this.id = id;
    }

    public static void encode(HashMapClient msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.map);
        buf.writeInt(msg.id);
    }

    public static HashMapClient decode(FriendlyByteBuf buf) {
        return new HashMapClient(buf.readUtf(), buf.readInt());
    }

    public static void handle(HashMapClient msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            handle2(msg.map,msg.id);
        });

        ctx.get().setPacketHandled(true);
    }

    @OnlyIn(Dist.CLIENT)
    private static void handle2(String map, int id) {
        if(id == 1)
            ATUtil.initMap(map,ATUtil.itemTiers);
        else if(id == 2) {
            ATUtil.initMap(map, ATUtil.entityTiers);
            ATUtil.getItems();
        }
    }
}
