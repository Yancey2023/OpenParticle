package yancey.openparticle.core.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static yancey.openparticle.core.OpenParticle.MOD_ID;

public record RunTickPayloadS2C(String path, int tick) implements CustomPayload {

    public static final CustomPayload.Id<RunTickPayloadS2C> ID = new CustomPayload.Id<>(new Identifier(MOD_ID, "run_tick"));
    public static final PacketCodec<RegistryByteBuf, RunTickPayloadS2C> CODEC = new PacketCodec<>() {
        @Override
        public RunTickPayloadS2C decode(RegistryByteBuf buf) {
            return new RunTickPayloadS2C(buf.readString(), buf.readInt());
        }

        @Override
        public void encode(RegistryByteBuf buf, RunTickPayloadS2C value) {
            buf.writeString(value.path);
            buf.writeInt(value.tick);
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

}
