package yancey.openparticle.core.network;

import net.minecraft.network.PacketByteBuf;
import org.jetbrains.annotations.NotNull;
import yancey.openparticle.core.versions.PayloadBase;
import yancey.openparticle.core.versions.PayloadCodec;
import yancey.openparticle.core.versions.PayloadId;

public class LoadPayloadS2C extends PayloadBase<LoadPayloadS2C> {

    public static final PayloadId<LoadPayloadS2C> ID = new PayloadId<LoadPayloadS2C>("load_project", new PayloadCodec<LoadPayloadS2C>() {
        @Override
        public LoadPayloadS2C decode(PacketByteBuf buf) {
            return new LoadPayloadS2C(buf.readString());
        }

        @Override
        public void encode(PacketByteBuf buf, LoadPayloadS2C value) {
            buf.writeString(value.path);
        }
    });

    public String path;

    public LoadPayloadS2C(String path) {
        this.path = path;
    }

    @Override
    public @NotNull PayloadId<LoadPayloadS2C> getPayloadId() {
        return ID;
    }
}
