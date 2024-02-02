package yancey.openparticle.core.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import yancey.openparticle.core.client.GuiProgressBar;
import yancey.openparticle.core.core.data.DataRunningPerTick;
import yancey.openparticle.core.core.data.RunningHandler;
import yancey.openparticle.core.mixin.ParticleManagerAccessor;
import yancey.openparticle.core.util.CommonUtil;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

@Environment(EnvType.CLIENT)
public class OpenParticleCore {

    public static DataRunningPerTick[] dataRunningPerTicks = null;
    public static String lastPath = null;
    private static RunningHandler runningHandler = null;

    private OpenParticleCore() {

    }

    public static boolean loadFile(String path) {
        dataRunningPerTicks = null;
        runningHandler = null;
        try (DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(path)))) {
            long timeStart = System.currentTimeMillis();
            int quantity = dataInputStream.readInt();
            GuiProgressBar guiProgressBar = new GuiProgressBar("加载粒子文件中", quantity);
            int a = quantity / 4;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if (guiProgressBar.progress <= a) {
                        CommonUtil.openGui(guiProgressBar);
                    }
                }
            }, 100);
            dataRunningPerTicks = DataRunningPerTick.readDataRunningPerTicksToFile(dataInputStream, guiProgressBar);
            long timeEnd = System.currentTimeMillis();
            CommonUtil.mc.inGameHud.getChatHud().addMessage(Text.literal("粒子文件加载成功(耗时:" + (timeEnd - timeStart) + "ms)"));
            lastPath = path;
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void run(String path, World world) {
        if (runningHandler != null) {
            runningHandler.stop();
        }
        runningHandler = null;
        ((ParticleManagerAccessor) CommonUtil.mc.particleManager).invokeClearParticles();
        if (!Objects.equals(lastPath, path) && !loadFile(path)) {
            return;
        }
        if (dataRunningPerTicks != null) {
            runningHandler = new RunningHandler(world, dataRunningPerTicks);
            runningHandler.run();
        }
    }

    public static void runTick(String path, World world, int tick) {
        if (!Objects.equals(lastPath, path) && !loadFile(path)) {
            return;
        }
        if (dataRunningPerTicks != null) {
            Arrays.stream(runningHandler.dataRunningList)
                    .filter(dataRunningPerTick -> dataRunningPerTick.tick == tick)
                    .forEach(dataRunningPerTick -> dataRunningPerTick.run(path, world));
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
