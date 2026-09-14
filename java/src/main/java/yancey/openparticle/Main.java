package yancey.openparticle;

import activity.ActivityTest;
import yancey.openparticle.api.activity.ActivityManager;
import yancey.openparticle.api.common.nativecore.OpenParticleProject;
import yancey.openparticle.api.run.OpenParticleAPI;
import yancey.openparticle.api.run.data.DataParticleManager;
import yancey.openparticle.api.run.math.Vec3;
import yancey.openparticle.api.run.node.NodeCache;
import yancey.openparticle.api.run.node.NodeTicker;
import yancey.openparticle.api.run.util.ColorUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

    public static void main(String[] args) {
        //declare a open particle API
        OpenParticleAPI openParticleAPI = new OpenParticleAPI();

        //add activity you want to output
        ActivityManager.add(ActivityTest.class);
//        ActivityManager.add(ActivityTest2.class);
        //output activity as particle file
        ActivityManager.output(openParticleAPI);
        //output activity as particle file and try to run it in java
//        ActivityManager.outputAndRun(openParticleAPI, true);

        // if you want to run particle file, you can use runInJava or runInNative to run it
        String nativeLibPath = "D:\\CLion\\project\\OpenParticle\\cmake-build-release\\libOpenParticle.dll";
        File file = new File("D:\\PyCharm\\project\\OpenParticleAPI-py\\output\\2.par");
        runInJava(openParticleAPI, file, null);
        runInNative(nativeLibPath, openParticleAPI, file);

        // exit
        System.exit(0);
    }

    public static void runInJava(OpenParticleAPI openParticleAPI, File file, String outputFile) {
        System.out.println("-----run in java-----");
        long startTime1 = System.currentTimeMillis();
        DataParticleManager input;
        try {
            input = openParticleAPI.input(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        NodeTicker nodeTicker = input.getNodeTicker();
        int maxTick = nodeTicker.root.tickEnd;
        long endTime1 = System.currentTimeMillis();
        System.out.println("粒子数量：" + input.getParticleCount());
        System.out.println("加载耗时：" + (endTime1 - startTime1) + "ms");
        long startTime2 = System.currentTimeMillis();
        ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        if (outputFile != null) {
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
                Files.writeString(Path.of(outputFile), stringJoiner.toString());
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
        long endTime2 = System.currentTimeMillis();
        System.out.println("运行耗时：" + (endTime2 - startTime2) + "ms");
    }

    public static void runInNative(String nativeLibPath, OpenParticleAPI openParticleAPI, File file) {
        System.load(nativeLibPath);
        System.out.println("-----run in native-----");
        long startTime1 = System.currentTimeMillis();
        OpenParticleProject project = new OpenParticleProject(openParticleAPI.bridge, file.getAbsolutePath());
        long endTime1 = System.currentTimeMillis();
        System.out.println("加载耗时：" + (endTime1 - startTime1) + "ms");
        long startTime2 = System.currentTimeMillis();
        try (project) {
            for (int i = 0; i < project.tickEnd; i++) {
                project.tick(i);
            }
        }
        long endTime2 = System.currentTimeMillis();
        System.out.println("运行耗时：" + (endTime2 - startTime2) + "ms");
    }

}
