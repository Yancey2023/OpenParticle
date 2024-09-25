package yancey.openparticle.core.versions;

import net.minecraft.network.PacketByteBuf;

//#if MC>=12005
import net.minecraft.network.codec.PacketCodec;
//#endif

public abstract class PayloadCodec<T>
        //#if MC>=12005
        implements PacketCodec<PacketByteBuf, T>
        //#endif
{

    //#if MC<12005
    //$$ public abstract T decode(PacketByteBuf buf);
    //$$
    //$$ public abstract void encode(PacketByteBuf buf, T value);
    //#endif

}
