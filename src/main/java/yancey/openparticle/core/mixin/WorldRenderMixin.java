package yancey.openparticle.core.mixin;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yancey.openparticle.core.core.OpenParticleClientCore;

@Mixin(WorldRenderer.class)
public abstract class WorldRenderMixin {

    @Inject(method = "render", at = @At(value = "HEAD"))
    public void render(float tickDelta,
                       long limitTime,
                       boolean renderBlockOutline,
                       Camera camera,
                       GameRenderer gameRenderer,
                       LightmapTextureManager lightmapTextureManager,
                       Matrix4f matrix4f,
                       Matrix4f matrix4f2,
                       CallbackInfo ci
    ) {
        OpenParticleClientCore.inRendered = false;
    }
}
