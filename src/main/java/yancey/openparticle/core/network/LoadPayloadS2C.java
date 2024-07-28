package yancey.openparticle.core.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static yancey.openparticle.core.OpenParticle.MOD_ID;

public record LoadPayloadS2C(String path) implements CustomPayload {

    public static final CustomPayload.Id<LoadPayloadS2C> ID = new CustomPayload.Id<>(Identifier.of(MOD_ID, "load_project"));
    public static final PacketCodec<RegistryByteBuf, LoadPayloadS2C> CODEC =
            PacketCodec.tuple(PacketCodecs.STRING, LoadPayloadS2C::path, LoadPayloadS2C::new);

    @Override
    public Id<LoadPayloadS2C> getId() {
        return ID;
    }
}
