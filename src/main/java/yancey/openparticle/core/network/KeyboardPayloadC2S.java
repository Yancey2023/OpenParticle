package yancey.openparticle.core.network;

import net.minecraft.network.PacketByteBuf;
import yancey.openparticle.core.versions.PayloadBase;
import yancey.openparticle.core.versions.PayloadCodec;
import yancey.openparticle.core.versions.PayloadId;

public class KeyboardPayloadC2S extends PayloadBase<KeyboardPayloadC2S> {

    public static final PayloadId<KeyboardPayloadC2S> ID = new PayloadId<KeyboardPayloadC2S>("key_board", new PayloadCodec<KeyboardPayloadC2S>() {
        @Override
        public KeyboardPayloadC2S decode(PacketByteBuf buf) {
            return new KeyboardPayloadC2S(buf.readIntArray());
        }

        @Override
        public void encode(PacketByteBuf buf, KeyboardPayloadC2S value) {
            buf.writeIntArray(value.idList);
        }
    });

    public int[] idList;

    public KeyboardPayloadC2S(int[] idList) {
        this.idList = idList;
    }


    @Override
    public PayloadId<KeyboardPayloadC2S> getPayloadId() {
        return ID;
    }
}
