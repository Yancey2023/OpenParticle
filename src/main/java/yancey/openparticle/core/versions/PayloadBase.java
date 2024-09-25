package yancey.openparticle.core.versions;

//#if MC>=12005
import net.minecraft.network.packet.CustomPayload;
//#endif

public abstract class PayloadBase<SELF extends PayloadBase<SELF>>
        //#if MC>=12005
        implements CustomPayload
        //#endif
{

    public abstract PayloadId<SELF> getPayloadId();

    //#if MC>=12005
    @Override
    public Id<SELF> getId() {
        return getPayloadId().getId();
    }
    //#endif

}