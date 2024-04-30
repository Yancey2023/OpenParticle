package yancey.openparticle.core.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static yancey.openparticle.core.OpenParticle.MOD_ID;

public record LoadAndRunPayloadS2C(String path) implements CustomPayload {

    public static final Id<LoadAndRunPayloadS2C> ID = new Id<>(new Identifier(MOD_ID, "load_and_run_project"));
    public static final PacketCodec<RegistryByteBuf, LoadAndRunPayloadS2C> CODEC =
            PacketCodec.tuple(PacketCodecs.STRING, LoadAndRunPayloadS2C::path, LoadAndRunPayloadS2C::new);

    @Override
    public Id<LoadAndRunPayloadS2C> getId() {
        return ID;
    }
}
