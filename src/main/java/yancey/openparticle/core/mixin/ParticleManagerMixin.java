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
import yancey.openparticle.core.core.OpenParticleClientCore;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Shadow
    @Final
    private TextureManager textureManager;


    @Inject(method = "renderParticles", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;depthMask(Z)V", shift = At.Shift.BEFORE))
    private void injectRenderParticles(LightmapTextureManager lightmapTextureManager, Camera camera, float tickDelta, CallbackInfo ci) {
        lightmapTextureManager.enable();
        RenderSystem.enableDepthTest();

        RenderSystem.setShader(GameRenderer::getParticleProgram);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT.begin(bufferBuilder, textureManager);
        OpenParticleClientCore.render(camera, tickDelta, bufferBuilder);
        ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT.draw(tessellator);

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