package yancey.openparticle.core.network;

import net.minecraft.network.PacketByteBuf;
import yancey.openparticle.core.versions.PayloadBase;
import yancey.openparticle.core.versions.PayloadCodec;
import yancey.openparticle.core.versions.PayloadId;

public class RunPayloadS2C extends PayloadBase<RunPayloadS2C> {

    public static final PayloadId<RunPayloadS2C> ID = new PayloadId<RunPayloadS2C>("run_project", new PayloadCodec<RunPayloadS2C>() {
        @Override
        public RunPayloadS2C decode(PacketByteBuf buf) {
            return new RunPayloadS2C(buf.readString(), buf.readBoolean());
        }

        @Override
        public void encode(PacketByteBuf buf, RunPayloadS2C value) {
            buf.writeString(value.path);
            buf.writeBoolean(value.isSingleThread);
        }
    });

    public String path;
    public boolean isSingleThread;

    public RunPayloadS2C(String path, boolean isSingleThread) {
        this.path = path;
        this.isSingleThread = isSingleThread;
    }

    @Override
    public PayloadId<RunPayloadS2C> getPayloadId() {
        return null;
    }

}
