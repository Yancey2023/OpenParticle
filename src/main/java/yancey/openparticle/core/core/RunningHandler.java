package yancey.openparticle.core.core;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import yancey.openparticle.api.common.controller.SimpleParticleController;
import yancey.openparticle.api.common.data.DataRunningPerTick;
import yancey.openparticle.core.client.ParticleAsyncManager;
import yancey.openparticle.core.events.RunningEventManager;
import yancey.openparticle.core.network.NetworkHandler;

import java.util.List;

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
            if (world.isClient) {
                runTick(world, dataRunningList[which++].controllerList);
            } else {
                which++;
                NetworkHandler.summonParticle((ServerWorld) world, OpenParticleCore.lastPath, tick);
            }
        }
        stop();
    }

    public void runTick(World world, List<SimpleParticleController> controllerList) {
        ParticleAsyncManager.addParticles(world, controllerList);
    }
}
