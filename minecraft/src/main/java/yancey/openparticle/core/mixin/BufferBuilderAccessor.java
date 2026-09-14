package yancey.openparticle.core.mixin;

import net.minecraft.client.render.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

//#if MC>=12100
import net.minecraft.client.util.BufferAllocator;
//#else
//$$ import org.spongepowered.asm.mixin.gen.Invoker;
//$$ import java.nio.ByteBuffer;
//#endif

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {

    @Accessor
    void setVertexCount(int vertexCount);

    //#if MC>=12100
    @Accessor
    BufferAllocator getAllocator();
    //#else
    //$$ @Invoker
    //$$ void invokeGrow(int size);
    //$$
    //$$ @Accessor
    //$$ ByteBuffer getBuffer();
    //#endif

}
