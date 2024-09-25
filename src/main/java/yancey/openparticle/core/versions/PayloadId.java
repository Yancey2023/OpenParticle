package yancey.openparticle.core.versions;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

//#if MC>=12005
import net.minecraft.network.packet.CustomPayload;
//#else
//$$ import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
//$$ import net.minecraft.network.PacketByteBuf;
//#endif

import static yancey.openparticle.core.OpenParticle.MOD_ID;

public class PayloadId<T extends PayloadBase<T>> {

    //#if MC>=12005
    private final CustomPayload.Id<T> id;
    //#else
    //$$ private final Identifier id;
    //#endif
    private final PayloadCodec<T> codec;

    public PayloadId(String id, PayloadCodec<T> codec) {
        //#if MC>=12005
        this.id = new CustomPayload.Id<>(IdentifierUtil.create(MOD_ID, id));
        //#else
        //$$ this.id = IdentifierUtil.create(MOD_ID, id);
        //#endif
        this.codec = codec;
    }

    //#if MC>=12005
    public CustomPayload.Id<T> getId() {
        return id;
    }
    //#else
    //$$ public Identifier getId() {
    //$$      return id;
    //$$ }
    //#endif

    public PayloadCodec<T> getCodec() {
        return codec;
    }

    public void registerServerGlobalReceiver(ServerPayloadHandler<T> handler) {
        //#if MC>=12006
        ServerPlayNetworking.registerGlobalReceiver(id, (payload, context) -> handler.receive(payload, context.server(), context.player()));
        //#elseif MC>=12005
        //$$ ServerPlayNetworking.registerGlobalReceiver(id, (payload, context) -> handler.receive(payload, context.player().server, context.player()));
        //#else
        //$$ ServerPlayNetworking.registerGlobalReceiver(id, (server, player, handler0, buf, responseSender) -> handler.receive(codec.decode(buf), server, player));
        //#endif
    }

    public void registerClientGlobalReceiver(ClientPayloadHandler<T> handler) {
        //#if MC>=12005
        ClientPlayNetworking.registerGlobalReceiver(id, (payload, context) -> handler.receive(payload, context.client(), context.player()));
        //#else
        //$$ ClientPlayNetworking.registerGlobalReceiver(id, (client, handler0, buf, responseSender) -> handler.receive(codec.decode(buf), client, client.player));
        //#endif
    }

    public void sendToServer(T payload) {
        //#if MC>=12005
        ClientPlayNetworking.send(payload);
        //#else
        //$$ PacketByteBuf buf = PacketByteBufs.create();
        //$$ codec.encode(buf, payload);
        //$$ ClientPlayNetworking.send(id, buf);
        //#endif
    }

    public void sendToClient(ServerPlayerEntity player, T payload) {
        //#if MC>=12005
        ServerPlayNetworking.send(player, payload);
        //#else
        //$$ PacketByteBuf buf = PacketByteBufs.create();
        //$$ codec.encode(buf, payload);
        //$$ ServerPlayNetworking.send(player, id, buf);
        //#endif
    }

    public void sendToAllClient(MinecraftServer server, T payload) {
        //#if MC>=12005
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(player, payload);
        }
        //#else
        //$$ PacketByteBuf buf = PacketByteBufs.create();
        //$$ codec.encode(buf, payload);
        //$$ for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
        //$$    ServerPlayNetworking.send(player, id, buf);
        //$$ }
        //#endif
    }

    public interface ServerPayloadHandler<T> {
        void receive(T payload, MinecraftServer server, ServerPlayerEntity player);
    }

    public interface ClientPayloadHandler<T> {
        void receive(T payload, MinecraftClient client, ClientPlayerEntity player);
    }

}
