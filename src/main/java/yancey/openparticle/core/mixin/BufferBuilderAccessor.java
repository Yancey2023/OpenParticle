package yancey.openparticle.core.mixin;

import net.minecraft.client.render.BufferBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.ByteBuffer;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {

    @Accessor
    void setElementOffset(int elementOffset);

    @Accessor
    void setVertexCount(int vertexCount);

    @Invoker("grow")
    void invokeGrow(int size);

    @Accessor
    ByteBuffer getBuffer();
}
