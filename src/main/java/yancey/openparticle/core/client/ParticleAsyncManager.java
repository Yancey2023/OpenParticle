package yancey.openparticle.core.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import org.slf4j.Logger;
import yancey.openparticle.core.mixin.BufferBuilderAccessor;
import yancey.openparticle.core.mixin.ParticleManagerAccessor;
import yancey.openparticle.core.particle.BetterParticle;

import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParticleAsyncManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int thread = 16;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(thread);

    public static Camera lastCamera;
    public static float lastTickDelta;

    @SuppressWarnings("unchecked")
    public static void renderParticles(VertexConsumer vertexConsumer) {
        Queue<Particle> queue = ((ParticleManagerAccessor) MinecraftClient.getInstance().particleManager)
                .getParticles().get(BetterParticle.BETTER_PARTICLE_SHEET);
        int size = queue.size();
        int elementOffset = 112 * size;
        ((BufferBuilderAccessor) vertexConsumer).invokeGrow(elementOffset);
        BetterParticle.buildRenderCache(lastCamera);
        CompletableFuture<Void>[] futures = new CompletableFuture[thread];
        Particle[] particles = queue.toArray(new Particle[0]);
        int nums = size / thread;
        for (int i = 0; i < thread; i++) {
            int start = i * nums;
            int end = i + 1 == thread ? size : (i + 1) * nums;
            futures[i] = CompletableFuture.runAsync(() -> {
                int j = start;
                while (j < end) {
                    ((BetterParticle) particles[j])
                            .buildGeometryAsync(vertexConsumer, lastCamera, lastTickDelta, j++ * 112);
                }
            }, executorService);
        }
        try {
            for (CompletableFuture<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("多线程VBO渲染失败", e);
            return;
        }
        ((BufferBuilderAccessor) vertexConsumer).setElementOffset(elementOffset);
        ((BufferBuilderAccessor) vertexConsumer).setVertexCount(4 * size);
    }

    @SuppressWarnings("unchecked")
    public static void clearTickCache() {
        Queue<Particle> queue = ((ParticleManagerAccessor) MinecraftClient.getInstance().particleManager)
                .getParticles().get(BetterParticle.BETTER_PARTICLE_SHEET);
        if (queue == null) {
            return;
        }
        int size = queue.size();
        CompletableFuture<Void>[] futures = new CompletableFuture[thread];
        Particle[] particles = queue.toArray(new Particle[0]);
        int nums = size / thread;
        for (int i = 0; i < thread; i++) {
            int start = i * nums;
            int end = i + 1 == thread ? size : (i + 1) * nums;
            futures[i] = CompletableFuture.runAsync(() -> {
                int j = start;
                while (j < end) {
                    ((BetterParticle) particles[j++]).clearCache();
                }
            }, executorService);
        }
        try {
            for (CompletableFuture<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("多线程清除粒子状态计算缓存失败", e);
        }
    }

}
