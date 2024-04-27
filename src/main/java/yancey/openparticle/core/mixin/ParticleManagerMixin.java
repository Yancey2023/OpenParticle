package yancey.openparticle.core.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yancey.openparticle.core.core.OpenParticleCore;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Shadow
    @Final
    private TextureManager textureManager;


    @Inject(method = "renderParticles", at = @At(value = "HEAD"))
    private void injectRenderParticles(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, LightmapTextureManager lightmapTextureManager, Camera camera, float tickDelta, CallbackInfo ci) {
        lightmapTextureManager.enable();
        RenderSystem.enableDepthTest();
        MatrixStack matrixStack = RenderSystem.getModelViewStack();
        matrixStack.push();
        matrixStack.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
        RenderSystem.applyModelViewMatrix();

        RenderSystem.setShader(GameRenderer::getParticleProgram);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT.begin(bufferBuilder, textureManager);
        OpenParticleCore.render(camera, tickDelta, bufferBuilder);
        ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT.draw(tessellator);

        matrixStack.pop();
        RenderSystem.applyModelViewMatrix();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        lightmapTextureManager.disable();
    }

    @Inject(method = "tick", at = @At(value = "RETURN"))
    private void injectTick(CallbackInfo ci) {
        OpenParticleCore.tick();
    }

}