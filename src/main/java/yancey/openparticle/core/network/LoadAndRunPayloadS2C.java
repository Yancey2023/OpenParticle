package yancey.openparticle.core.network;

import net.minecraft.network.PacketByteBuf;
import yancey.openparticle.core.versions.PayloadBase;
import yancey.openparticle.core.versions.PayloadCodec;
import yancey.openparticle.core.versions.PayloadId;

public class LoadAndRunPayloadS2C extends PayloadBase<LoadAndRunPayloadS2C> {

    public static final PayloadId<LoadAndRunPayloadS2C> ID = new PayloadId<LoadAndRunPayloadS2C>("load_and_run_project", new PayloadCodec<LoadAndRunPayloadS2C>() {
        @Override
        public LoadAndRunPayloadS2C decode(PacketByteBuf buf) {
            return new LoadAndRunPayloadS2C(buf.readString(), buf.readBoolean());
        }

        @Override
        public void encode(PacketByteBuf buf, LoadAndRunPayloadS2C value) {
            buf.writeString(value.path);
            buf.writeBoolean(value.isSingleThread);
        }
    });

    public String path;
    public boolean isSingleThread;

    public LoadAndRunPayloadS2C(String path, boolean isSingleThread) {
        this.path = path;
        this.isSingleThread = isSingleThread;
    }

    @Override
    public PayloadId<LoadAndRunPayloadS2C> getPayloadId() {
        return ID;
    }
}
