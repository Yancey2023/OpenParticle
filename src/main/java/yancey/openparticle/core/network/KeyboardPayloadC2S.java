package yancey.openparticle.core.network;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

import static yancey.openparticle.core.OpenParticle.MOD_ID;

public record KeyboardPayloadC2S(List<Integer> idList) implements CustomPayload {

    public static final CustomPayload.Id<KeyboardPayloadC2S> ID = new CustomPayload.Id<>(new Identifier(MOD_ID, "key_board"));
    public static final PacketCodec<RegistryByteBuf, KeyboardPayloadC2S> CODEC = new PacketCodec<>() {
        @Override
        public KeyboardPayloadC2S decode(RegistryByteBuf buf) {
            int size = buf.readInt();
            List<Integer> idList = new ArrayList<>(size);
            for (int i = 0; i < size; i++) {
                idList.add(buf.readInt());
            }
            return new KeyboardPayloadC2S(idList);
        }

        @Override
        public void encode(RegistryByteBuf buf, KeyboardPayloadC2S value) {
            buf.writeInt(value.idList.size());
            for (int j : value.idList) {
                buf.writeInt(j);
            }
        }
    };

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

}
