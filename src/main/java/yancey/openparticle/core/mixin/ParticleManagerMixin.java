package yancey.openparticle.core.mixin;

import com.google.common.collect.EvictingQueue;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yancey.openparticle.core.client.ParticleAsyncManager;
import yancey.openparticle.core.particle.BetterParticle;

import java.util.*;

@Mixin(ParticleManager.class)
public abstract class ParticleManagerMixin {

    @Mutable
    @Shadow
    @Final
    private static List<ParticleTextureSheet> PARTICLE_TEXTURE_SHEETS;

    @Shadow
    @Final
    private Map<ParticleTextureSheet, Queue<Particle>> particles;

    @Unique
    private ParticleTextureSheet lastTextureSheet;

//    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;"))
//    private <K, V> V redirectComputeIfAbsent(Map<K, V> particles, K k, Function<? super K, ? extends V> key) {
//        return particles.computeIfAbsent(k, (sheet) -> (V) new LinkedList<Particle>());
//    }

    @Redirect(method = {"method_18125"}, at = @At(value = "INVOKE", target = "Lcom/google/common/collect/EvictingQueue;create(I)Lcom/google/common/collect/EvictingQueue;", remap = false))
    private static EvictingQueue<Particle> modifyArgTick(int maxSize) {
        return EvictingQueue.create(500000);
    }

    @Inject(method = "<clinit>", at = @At(value = "RETURN"))
    private static void modifyParticleTextureSheets(CallbackInfo ci) {
        List<ParticleTextureSheet> particleTextureSheets = new ArrayList<>(PARTICLE_TEXTURE_SHEETS.size() + 1);
        particleTextureSheets.addAll(PARTICLE_TEXTURE_SHEETS);
        particleTextureSheets.add(BetterParticle.BETTER_PARTICLE_SHEET);
        PARTICLE_TEXTURE_SHEETS = particleTextureSheets;
    }

    @Inject(method = "renderParticles", at = @At(value = "HEAD"))
    public void renderParticles(MatrixStack matrices, VertexConsumerProvider.Immediate vertexConsumers, LightmapTextureManager lightmapTextureManager, Camera camera, float tickDelta, CallbackInfo ci) {
        ParticleAsyncManager.lastCamera = camera;
        ParticleAsyncManager.lastTickDelta = tickDelta;
    }

    @Redirect(method = "renderParticles", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"))
    private Object redirectGet(Map<ParticleTextureSheet, Queue<Particle>> particles, Object particleTextureSheet) {
        lastTextureSheet = (ParticleTextureSheet) particleTextureSheet;
        return particles.get(lastTextureSheet);
    }

    /**
     * @reason 因为不是通过这个方法渲染，没有必要继续循环
     */
    @Redirect(method = "renderParticles", at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z", ordinal = 1))
    private boolean redirectHasNext(Iterator<Particle> iterator) {
        if (lastTextureSheet == BetterParticle.BETTER_PARTICLE_SHEET) {
            return false;
        }
        return iterator.hasNext();
    }

    @Inject(method = "tick", at = @At(value = "HEAD"))
    private void injectTick(CallbackInfo ci) {
        ParticleAsyncManager.tick();
    }

    @Inject(method = "tickParticles", at = @At(value = "HEAD"), cancellable = true)
    private void injectTickParticles(Collection<Particle> particles, CallbackInfo ci) {
        if (this.particles.get(BetterParticle.BETTER_PARTICLE_SHEET) == particles) {
            ci.cancel();
        }
    }

}