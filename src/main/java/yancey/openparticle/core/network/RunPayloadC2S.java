package yancey.openparticle.core.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static yancey.openparticle.core.OpenParticle.MOD_ID;

public record RunPayloadC2S(String path, int tickEnd, boolean isSingleThread) implements CustomPayload {

    public static final Id<RunPayloadC2S> ID = new Id<>(Identifier.of(MOD_ID, "tick_end"));
    public static final PacketCodec<RegistryByteBuf, RunPayloadC2S> CODEC = new PacketCodec<>() {
        @Override
        public RunPayloadC2S decode(RegistryByteBuf buf) {
            return new RunPayloadC2S(buf.readString(), buf.readVarInt(), buf.readBoolean());
        }

        @Override
        public void encode(RegistryByteBuf buf, RunPayloadC2S value) {
            buf.writeString(value.path);
            buf.writeVarInt(value.tickEnd);
            buf.writeBoolean(value.isSingleThread);
        }
    };

    @Override
    public Id<RunPayloadC2S> getId() {
        return ID;
    }

}
