package yancey.openparticle.core.mixin;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.util.BufferAllocator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BufferBuilder.class)
public interface BufferBuilderAccessor {

    @Accessor
    void setVertexCount(int vertexCount);

    @Accessor
    BufferAllocator getAllocator();

}
