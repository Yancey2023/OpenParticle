package yancey.openparticle.api.activity;

import yancey.openparticle.api.particle.Particle;
import yancey.openparticle.api.run.OpenParticleAPI;
import yancey.openparticle.api.run.data.DataParticleManager;
import yancey.openparticle.api.run.math.Vec3;
import yancey.openparticle.api.run.node.NodeCache;
import yancey.openparticle.api.run.node.NodeTicker;
import yancey.openparticle.api.run.util.ColorUtil;
import yancey.openparticle.api.type.ParticleType;
import yancey.openparticle.api.util.version.Version;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class Activity {

    protected final Random random = new Random(getRandomSeed());
    private final List<Particle> particleList = new ArrayList<>();

    /**
     * 随机值种子
     */
    protected long getRandomSeed() {
        return 0;
    }

    protected Version getVersion() {
        return Version.Java1_16;
    }

    protected abstract File getFile();

    protected abstract Vec3 getPosition();

    /**
     * 创造粒子，这是你需要重写的方法
     */
    protected void addParticle(Particle particle) {
        particleList.add(particle);
    }

    protected abstract void createParticle();

    protected Vec3 getRandomVec3(float range) {
        return new Vec3(
                random.nextFloat(-range, range),
                random.nextFloat(-range, range),
                random.nextFloat(-range, range)
        );
    }

    public void output(OpenParticleAPI openParticleAPI, boolean isRun, boolean isSaveFile) {
        System.out.println("----- " + getClass().getSimpleName() + " -----");
        System.out.println("开始阶段1: 获取粒子数据");
        long timeStart1 = System.currentTimeMillis();
        createParticle();
        if (particleList.isEmpty()) {
            openParticleAPI.logger.warn("粒子为空，你是不是忘记调用addParticle()");
            return;
        }
        Particle particle = Particle.compound(particleList).offset(getPosition());
        long timeEnd1 = System.currentTimeMillis();
        System.out.printf("阶段1: 运行耗时%dms\n\n", timeEnd1 - timeStart1);

        System.out.println("开始阶段2: 计算粒子数据");
        long timeStart2 = System.currentTimeMillis();
        ParticleType.clearAllCache();
        DataParticleManager dataParticleManager = new DataParticleManager(particle.runWithCache(getVersion()));
        long timeEnd2 = System.currentTimeMillis();
        System.out.printf("阶段2: 运行耗时%dms\n", timeEnd2 - timeStart2);
        long count = dataParticleManager.getParticleCount();
        System.out.printf("阶段2: 即将写入%d个粒子\n\n", count);

        System.out.println("开始阶段3: 写入粒子数据");
        File file = getFile();
        long timeStart3 = System.currentTimeMillis();
        openParticleAPI.output(file, dataParticleManager);
        long timeEnd3 = System.currentTimeMillis();
        System.out.printf("阶段3: 运行耗时%dms\n", timeEnd3 - timeStart3);
        long fileSize = file.length();
        System.out.printf("阶段3: 文件大小为%s\n\n", formatFileSize(fileSize));

        long timeStart4 = 0, timeEnd4 = 0, timeStart5 = 0, timeEnd5 = 0;
        int maxTick = 0;
        if (isRun) {
            System.out.println("开始阶段4: 读取粒子数据");
            timeStart4 = System.currentTimeMillis();
            DataParticleManager input;
            try {
                input = openParticleAPI.input(file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            timeEnd4 = System.currentTimeMillis();
            System.out.printf("阶段4: 运行耗时%dms\n\n", timeEnd4 - timeStart4);

            System.out.println("开始阶段5: 运行粒子数据");
            timeStart5 = System.currentTimeMillis();
            NodeTicker nodeTicker = input.getNodeTicker();
            maxTick = nodeTicker.root.tickEnd;
            ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            if (isSaveFile) {
                List<CompletableFuture<String>> futureList = new ArrayList<>();
                for (int tick = 0; tick < maxTick; tick++) {
                    int finalTick = tick;
                    futureList.add(CompletableFuture.supplyAsync(() -> {
                        NodeCache[] caches = nodeTicker.buildCache(finalTick);
                        StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append("---- tick: ").append(finalTick).append(" ------\n");
                        int j = 0;
                        for (NodeCache nodeCache : caches) {
                            if (nodeCache == null) {
                                continue;
                            }
                            Vec3 position = nodeCache.cachePosition.apply(Vec3.ZERO);
                            stringBuilder.append(++j).append(". ").append("pos:(")
                                    .append(position.x).append(", ")
                                    .append(position.y).append(", ")
                                    .append(position.z).append("), RGBA:(")
                                    .append(ColorUtil.getRed(nodeCache.cacheColor)).append(", ")
                                    .append(ColorUtil.getGreen(nodeCache.cacheColor)).append(", ")
                                    .append(ColorUtil.getBlue(nodeCache.cacheColor)).append(", ")
                                    .append(ColorUtil.getAlpha(nodeCache.cacheColor)).append(")\n");
                        }
                        return stringBuilder.toString();
                    }, executorService));
                }
                StringJoiner stringJoiner = new StringJoiner("\n");
                for (CompletableFuture<String> future : futureList) {
                    stringJoiner.add(future.join());
                }
                try {
                    Files.writeString(getFile().toPath().getParent().resolve("run.txt"), stringJoiner.toString());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else {
                List<CompletableFuture<Void>> futureList = new ArrayList<>();
                for (int tick = 0; tick < maxTick; tick++) {
                    int finalTick = tick;
                    futureList.add(CompletableFuture.runAsync(() -> nodeTicker.buildCache(finalTick), executorService));
                }
                for (CompletableFuture<Void> future : futureList) {
                    future.join();
                }
            }
            timeEnd5 = System.currentTimeMillis();
            System.out.printf("阶段5: 运行耗时%dms\n\n", timeEnd5 - timeStart5);
        }

        System.out.printf("导出耗时: %dms\n", timeEnd3 - timeStart1);
        if (isRun) {
            System.out.printf("导入耗时: %dms\n", timeEnd4 - timeStart4);
            System.out.printf("运行耗时: %dms\n", timeEnd5 - timeStart5);
            System.out.printf("粒子时长: %dms\n", maxTick * 50);
        }
        System.out.printf("粒子数量: %d\n", count);
        System.out.printf("文件大小: %s\n\n", formatFileSize(fileSize));
    }

    private static String formatFileSize(long size) {
        if (size <= 1024) {
            return String.format("%dB", size);
        }
        double temp = size;
        temp /= 1024;
        if (temp <= 1024) {
            return String.format("%.2fKB", temp);
        }
        temp /= 1024;
        if (temp <= 1024) {
            return String.format("%.2fMB", temp);
        }
        temp /= 1024;
        return String.format("%.2fGB", temp);
    }

}