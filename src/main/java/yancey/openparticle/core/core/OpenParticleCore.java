package yancey.openparticle.core.core;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.slf4j.Logger;
import yancey.openparticle.api.common.OpenParticleAPI;
import yancey.openparticle.api.common.data.DataRunningPerTick;
import yancey.openparticle.core.core.data.RunningHandler;
import yancey.openparticle.core.mixin.ParticleManagerAccessor;
import yancey.openparticle.core.util.MyLogger;

import java.io.*;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

@Environment(EnvType.CLIENT)
public class OpenParticleCore {

    private static final Logger LOGGER = LogUtils.getLogger();
    public static final OpenParticleAPI CORE = new OpenParticleAPI(
            identifier -> Registries.PARTICLE_TYPE.getRawId(Registries.PARTICLE_TYPE.get(
                    new net.minecraft.util.Identifier(identifier.getNamespace(), identifier.getValue()))),
            new MyLogger(LOGGER)
    );
    private static final ReentrantLock LOCK = new ReentrantLock();
    public static String lastPath = null;
    private static RunningHandler runningHandler = null;
    public static DataRunningPerTick[] dataRunningList = null;

    private OpenParticleCore() {

    }

    public static boolean loadFile(String path) {
        LOCK.lock();
        dataRunningList = null;
        runningHandler = null;
        try (DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(path)))) {
            long timeStart = System.currentTimeMillis();
            dataRunningList = CORE.input(new File(path)).getDataRunningList();
            long timeEnd = System.currentTimeMillis();
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("粒子文件加载成功(耗时:" + (timeEnd - timeStart) + "ms)"));
            lastPath = path;
            return true;
        } catch (IOException e) {
            return false;
        } finally {
            LOCK.unlock();
        }
    }

    public static void run(String path, World world) {
        if (runningHandler != null) {
            runningHandler.stop();
        }
        runningHandler = null;
        ((ParticleManagerAccessor) MinecraftClient.getInstance().particleManager).invokeClearParticles();
        if (!Objects.equals(lastPath, path) && !loadFile(path)) {
            return;
        }
        if (dataRunningList != null) {
            runningHandler = new RunningHandler(world, dataRunningList);
            runningHandler.run();
        }
    }

    public static void runTick(String path, World world, int tick) {
        if (!Objects.equals(lastPath, path) && !loadFile(path)) {
            return;
        }
        if (dataRunningList != null) {
            for (DataRunningPerTick dataRunning : runningHandler.dataRunningList) {
                if (dataRunning.tick == tick) {
                    runningHandler.runTick(path, world, dataRunning.controllerList);
                    break;
                }
            }
        }
    }

    public static boolean loadAndRun(String path, World world) {
        if (loadFile(path)) {
            run(path, world);
            return true;
        } else {
            return false;
        }
    }

}
