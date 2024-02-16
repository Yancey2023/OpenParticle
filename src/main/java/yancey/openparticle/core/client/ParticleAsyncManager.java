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

import java.util.ArrayList;
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
    private static CompletableFuture<Void>[] tickFutures, createFutures;

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
            LOGGER.error("多线程VBO注入失败", e);
            return;
        }
        ((BufferBuilderAccessor) vertexConsumer).setElementOffset(elementOffset);
        ((BufferBuilderAccessor) vertexConsumer).setVertexCount(4 * size);
    }

    @SuppressWarnings("unchecked")
    public static void tick() {
        //先把上次提前做的准备获取一下
        try {
            if (createFutures != null) {
                for (CompletableFuture<Void> future : createFutures) {
                    future.get();
                }
                createFutures = null;
            }
            if (tickFutures != null) {
                for (CompletableFuture<Void> future : tickFutures) {
                    future.get();
                }
                tickFutures = null;
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("多线程构建粒子状态缓存失败", e);
            return;
        }

        //计算当前tick
        Queue<Particle> queue = ((ParticleManagerAccessor) MinecraftClient.getInstance().particleManager)
                .getParticles().get(BetterParticle.BETTER_PARTICLE_SHEET);
        if (queue == null) {
            return;
        }
        int size = queue.size();
        if (size == 0) {
            return;
        }
        Particle[] particles = queue.toArray(new Particle[0]);
        int nums = size / thread;
        CompletableFuture<List<Integer>>[] futures = new CompletableFuture[thread];
        for (int i = 0; i < thread; i++) {
            int start = i * nums;
            int end = i + 1 == thread ? size : (i + 1) * nums;
            futures[i] = CompletableFuture.supplyAsync(() -> {
                int j = start;
                List<Integer> needToClear = new ArrayList<>();
                while (j < end) {
                    particles[j].tick();
                    if (!particles[j].isAlive()) {
                        needToClear.add(j);
                    }
                    j++;
                }
                return needToClear;
            }, executorService);
        }
        List<Integer> needToClear = new ArrayList<>();
        try {
            for (CompletableFuture<List<Integer>> future : futures) {
                needToClear.addAll(future.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.error("多线程获取粒子状态失败", e);
            return;
        }

        //清除粒子
        if (!needToClear.isEmpty()) {
            queue.clear();
            if (needToClear.size() != size) {
                Particle[] dest = new Particle[size - needToClear.size()];
                nums = needToClear.size() / thread;
                CompletableFuture<Void>[] futures2 = new CompletableFuture[thread];
                for (int i = 0; i < thread; i++) {
                    int start = i * nums;
                    int end = i + 1 == thread ? needToClear.size() : (i + 1) * nums;
                    futures2[i] = CompletableFuture.runAsync(() -> {
                        int j = start;
                        int srcPos, destPos;
                        while (j < end) {
                            srcPos = j == 0 ? 0 : needToClear.get(j - 1) + 1;
                            destPos = srcPos - j;
                            System.arraycopy(particles, srcPos, dest, destPos, needToClear.get(j) - srcPos);
                            j++;
                        }
                        if (end == needToClear.size()) {
                            srcPos = needToClear.get(needToClear.size() - 1) + 1;
                            if (srcPos < particles.length) {
                                System.arraycopy(particles, srcPos, dest, srcPos - needToClear.size(), particles.length - srcPos);
                            }
                        }
                    }, executorService);
                }
                try {
                    for (CompletableFuture<Void> future : futures2) {
                        future.get();
                    }
                } catch (InterruptedException | ExecutionException e) {
                    LOGGER.error("多线程清除粒子失败", e);
                    return;
                }
                queue.addAll(List.of(dest));
            }
        }

        //准备下一个tick
        tickFutures = prepareParticles(particles);
    }

    @SuppressWarnings("unchecked")
    public static void addParticles(World world, List<SimpleParticleController> controllerList) {
        //添加粒子
        int size = controllerList.size();
        Particle[] particles = new Particle[size];
        int nums = size / thread;
        CompletableFuture<Void>[] futures = new CompletableFuture[thread];
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
            LOGGER.error("多线程新建粒子失败", e);
        }
        MinecraftClient client = MinecraftClient.getInstance();
        for (Particle particle : particles) {
            client.particleManager.addParticle(particle);
        }

        //构建下一个tick的缓存
        createFutures = prepareParticles(particles);
    }

    @SuppressWarnings("unchecked")
    private static CompletableFuture<Void>[] prepareParticles(Particle[] particles) {
        CompletableFuture<Void>[] futures = new CompletableFuture[thread];
        int nums = particles.length / thread;
        for (int i = 0; i < thread; i++) {
            int start = i * nums;
            int end = i + 1 == thread ? particles.length : (i + 1) * nums;
            futures[i] = CompletableFuture.runAsync(() -> {
                int j = start;
                while (j < end) {
                    ((BetterParticle) particles[j++]).prepare();
                }
            }, executorService);
        }
        return futures;
    }

}
