package yancey.openparticle.core.core.data;

import net.minecraft.world.World;
import yancey.openparticle.core.core.OpenParticleCore;
import yancey.openparticle.core.core.events.RunningEventManager;

public class RunningHandler {
    public final World world;
    public boolean isRunning = false;
    public final DataRunningPerTick[] dataRunningList;
    private int tick = 0;
    private int which = 0;

    public RunningHandler(World world, DataRunningPerTick[] dataRunningList) {
        this.dataRunningList = dataRunningList;
        this.world = world;
    }

    public void run() {
        if (dataRunningList != null && dataRunningList.length != 0) {
            isRunning = true;
            tick = Math.min(dataRunningList[0].tick, 0);
            which = 0;
            RunningEventManager.INSTANCE.run(this);
        } else if (isRunning) {
            stop();
        }
    }

    public void stop() {
        isRunning = false;
        tick = 0;
        which = 0;
        RunningEventManager.INSTANCE.stop(this);
    }

    public void runPerTick() {
        if (dataRunningList == null) {
            stop();
            return;
        }
        while (which < dataRunningList.length) {
            if (tick != dataRunningList[which].tick) {
                tick++;
                return;
            }
            dataRunningList[which++].run(OpenParticleCore.lastPath, world);
        }
        stop();
    }
}
