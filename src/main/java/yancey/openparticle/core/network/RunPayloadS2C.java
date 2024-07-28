package yancey.openparticle.core.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static yancey.openparticle.core.OpenParticle.MOD_ID;

public record RunPayloadS2C(String path, boolean isSingleThread) implements CustomPayload {

    public static final Id<RunPayloadS2C> ID = new Id<>(Identifier.of(MOD_ID, "run_project"));
    public static final PacketCodec<RegistryByteBuf, RunPayloadS2C> CODEC = new PacketCodec<>() {
        @Override
        public RunPayloadS2C decode(RegistryByteBuf buf) {
            return new RunPayloadS2C(buf.readString(), buf.readBoolean());
        }

        @Override
        public void encode(RegistryByteBuf buf, RunPayloadS2C value) {
            buf.writeString(value.path);
            buf.writeBoolean(value.isSingleThread);
        }
    };

    @Override
    public Id<RunPayloadS2C> getId() {
        return ID;
    }
}
