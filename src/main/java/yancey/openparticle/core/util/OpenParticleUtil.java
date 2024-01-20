package yancey.openparticle.core.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.text.Text;
import yancey.openparticle.core.client.GuiProgressBar;
import yancey.openparticle.core.events.DataRunningPerTick;
import yancey.openparticle.core.events.RunningHandler;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

@Environment(EnvType.CLIENT)
public class OpenParticleUtil {

    private static DataRunningPerTick[] dataRunningPerTicks = null;

    private OpenParticleUtil() {

    }

    public static boolean loadFile(String path) {
        try (DataInputStream dataInputStream = new DataInputStream(new BufferedInputStream(new FileInputStream(path)))) {
            long timeStart = System.currentTimeMillis();
            int quantity = dataInputStream.readInt();
            GuiProgressBar guiProgressBar = new GuiProgressBar("加载粒子文件中", quantity);
            int a = quantity / 4;
            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    if(guiProgressBar.progress <= a){
                        CommonUtil.openGui(guiProgressBar);
                    }
                }
            },100);
            dataRunningPerTicks = DataRunningPerTick.readDataRunningPerTicksToFile(dataInputStream, guiProgressBar);
            long timeEnd = System.currentTimeMillis();
            CommonUtil.mc.inGameHud.getChatHud().addMessage(Text.literal("粒子文件加载成功(耗时:" + (timeEnd - timeStart) + "ms)"));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static void run(ClientWorld clientWorld) {
        if (dataRunningPerTicks != null) {
            new RunningHandler(clientWorld, dataRunningPerTicks).run();
        }
    }

    public static boolean loadAndRum(String path, ClientWorld clientWorld) {
        if (loadFile(path)) {
            run(clientWorld);
            return true;
        } else {
            return false;
        }
    }

}
