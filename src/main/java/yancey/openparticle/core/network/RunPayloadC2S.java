package yancey.openparticle.core.network;

import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import yancey.openparticle.core.versions.PayloadBase;
import yancey.openparticle.core.versions.PayloadCodec;
import yancey.openparticle.core.versions.PayloadId;

public class RunPayloadC2S extends PayloadBase<RunPayloadC2S> {

    public static final PayloadId<RunPayloadC2S> ID = new PayloadId<RunPayloadC2S>("tick_end", new PayloadCodec<RunPayloadC2S>() {
        @Override
        public RunPayloadC2S decode(PacketByteBuf buf) {
            return new RunPayloadC2S(buf.readString(), buf.readVarInt(), buf.readBoolean());
        }

        @Override
        public void encode(PacketByteBuf buf, RunPayloadC2S value) {
            buf.writeString(value.path);
            buf.writeVarInt(value.tickEnd);
            buf.writeBoolean(value.isSingleThread);
        }
    });

    public String path;
    public int tickEnd;
    public boolean isSingleThread;

    public RunPayloadC2S(String path, int tickEnd, boolean isSingleThread) {
        this.path = path;
        this.tickEnd = tickEnd;
        this.isSingleThread = isSingleThread;
    }

    @Override
    public @NotNull PayloadId<RunPayloadC2S> getPayloadId() {
        return ID;
    }
}
