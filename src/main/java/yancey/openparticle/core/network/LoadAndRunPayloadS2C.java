package yancey.openparticle.core.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import static yancey.openparticle.core.OpenParticle.MOD_ID;

public record LoadAndRunPayloadS2C(String path, boolean isSingleThread) implements CustomPayload {

    public static final Id<LoadAndRunPayloadS2C> ID = new Id<>(Identifier.of(MOD_ID, "load_and_run_project"));
    public static final PacketCodec<RegistryByteBuf, LoadAndRunPayloadS2C> CODEC = new PacketCodec<>() {
        @Override
        public LoadAndRunPayloadS2C decode(RegistryByteBuf buf) {
            return new LoadAndRunPayloadS2C(buf.readString(), buf.readBoolean());
        }

        @Override
        public void encode(RegistryByteBuf buf, LoadAndRunPayloadS2C value) {
            buf.writeString(value.path);
            buf.writeBoolean(value.isSingleThread);
        }
    };

    @Override
    public Id<LoadAndRunPayloadS2C> getId() {
        return ID;
    }
}
