package yancey.openparticle.core.network;

import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import yancey.openparticle.core.versions.PayloadBase;
import yancey.openparticle.core.versions.PayloadCodec;
import yancey.openparticle.core.versions.PayloadId;

public class RunTickPayloadS2C extends PayloadBase<RunTickPayloadS2C> {

    public static final PayloadId<RunTickPayloadS2C> ID = new PayloadId<RunTickPayloadS2C>("run_tick", new PayloadCodec<RunTickPayloadS2C>() {
        @Override
        public RunTickPayloadS2C decode(PacketByteBuf buf) {
            return new RunTickPayloadS2C(buf.readString(), buf.readVarInt(), buf.readBoolean());
        }

        @Override
        public void encode(PacketByteBuf buf, RunTickPayloadS2C value) {
            buf.writeString(value.path);
            buf.writeVarInt(value.tick);
            buf.writeBoolean(value.isSingleThread);
        }
    });

    public String path;
    public int tick;
    public boolean isSingleThread;

    public RunTickPayloadS2C(String path, int tick, boolean isSingleThread) {
        this.path = path;
        this.tick = tick;
        this.isSingleThread = isSingleThread;
    }

    @Override
    public @NotNull PayloadId<RunTickPayloadS2C> getPayloadId() {
        return ID;
    }
}
