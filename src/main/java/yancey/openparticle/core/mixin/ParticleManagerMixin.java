package yancey.openparticle.core.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.TextureManager;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yancey.openparticle.core.client.core.OpenParticleClientCore;

//#if MC<11900
//$$ import net.minecraft.client.util.math.MatrixStack;
//#endif

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Shadow
    @Final
    private TextureManager textureManager;

    @Inject(method = "renderParticles", at = @At(value = "HEAD"))
    private void injectRenderParticles(
            //#if MC<11900
            //$$ MatrixStack matrices,
            //$$ VertexConsumerProvider.Immediate vertexConsumers,
            //#endif
            LightmapTextureManager lightmapTextureManager,
            Camera camera,
            float tickDelta,
            CallbackInfo ci
    ) {
        lightmapTextureManager.enable();
        RenderSystem.enableDepthTest();
        //#if MC<11900
        //$$ MatrixStack matrixStack = RenderSystem.getModelViewStack();
        //$$ matrixStack.push();
        //$$ matrixStack.multiplyPositionMatrix(matrices.peek().getPositionMatrix());
        //$$ RenderSystem.applyModelViewMatrix();
        //#endif

        RenderSystem.setShader(GameRenderer::getParticleProgram);
        Tessellator tessellator = Tessellator.getInstance();
        //#if MC>=12100
        BufferBuilder bufferBuilder = ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT.begin(tessellator, textureManager);
        //#else
        //$$ BufferBuilder bufferBuilder = tessellator.getBuffer();
        //$$ ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT.begin(bufferBuilder, textureManager);
        //#endif
        if (bufferBuilder != null) {
            OpenParticleClientCore.render(camera, tickDelta, bufferBuilder);
            //#if MC>=12100
            BuiltBuffer builtBuffer = bufferBuilder.endNullable();
            if (builtBuffer != null) {
                BufferRenderer.drawWithGlobalProgram(builtBuffer);
            }
            //#elseif MC>=19000
            //$$ BufferBuilder.BuiltBuffer builtBuffer = bufferBuilder.endNullable();
            //$$ if (builtBuffer != null) {
            //$$     BufferRenderer.drawWithGlobalProgram(builtBuffer);
            //$$ }
            //#else
            //$$ tessellator.draw();
            //#endif
        }

        //#if MC<11900
        //$$ matrixStack.pop();
        //$$ RenderSystem.applyModelViewMatrix();
        //#endif
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
        lightmapTextureManager.disable();
    }

    @Inject(method = "tick", at = @At(value = "RETURN"))
    private void injectTick(CallbackInfo ci) {
        OpenParticleClientCore.tick();
    }

    @Inject(method = "getDebugString", at = @At(value = "RETURN"), cancellable = true)
    private void injectGetDebugString(CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(String.valueOf(Integer.parseInt(cir.getReturnValue()) + OpenParticleClientCore.getParticleSize()));
    }

}