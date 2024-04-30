package yancey.openparticle.core.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static yancey.openparticle.core.OpenParticle.MOD_ID;

public record RunPayloadS2C(String path) implements CustomPayload {

    public static final Id<RunPayloadS2C> ID = new Id<>(new Identifier(MOD_ID, "run_project"));
    public static final PacketCodec<RegistryByteBuf, RunPayloadS2C> CODEC =
            PacketCodec.tuple(PacketCodecs.STRING, RunPayloadS2C::path, RunPayloadS2C::new);

    @Override
    public Id<RunPayloadS2C> getId() {
        return ID;
    }
}
