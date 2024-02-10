package yancey.openparticle.core.client;

import com.mojang.logging.LogUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.slf4j.Logger;
import yancey.openparticle.api.common.controller.SimpleParticleController;
import yancey.openparticle.core.mixin.BufferBuilderAccessor;
import yancey.openparticle.core.mixin.ParticleManagerAccessor;
import yancey.openparticle.core.particle.BetterParticle;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ParticleAsyncManager {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final int thread = 128;
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
    public static void tick() {
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
                    particles[j++].tick();
                }
            }, executorService);
        }
        try {
            for (CompletableFuture<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("多线程清除粒子状态计算缓存失败", e);
            return;
        }
        queue.removeIf(particle -> !particle.isAlive());
    }

    @SuppressWarnings("unchecked")
    public static void addParticles(World world, List<SimpleParticleController> controllerList) {
        int size = controllerList.size();
        CompletableFuture<Void>[] futures = new CompletableFuture[thread];
        Particle[] particles = new Particle[size];
        int nums = size / thread;
        for (int i = 0; i < thread; i++) {
            int start = i * nums;
            int end = i + 1 == thread ? size : (i + 1) * nums;
            futures[i] = CompletableFuture.runAsync(() -> {
                int j = start;
                while (j < end) {
                    particles[j] = (BetterParticle.create((ClientWorld) world, controllerList.get(j)));
                    j++;
                }
            }, executorService);
        }
        try {
            for (CompletableFuture<Void> future : futures) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("新建粒子失败", e);
        }
        MinecraftClient client = MinecraftClient.getInstance();
        for (Particle particle : particles) {
            client.particleManager.addParticle(particle);
        }
    }

}
