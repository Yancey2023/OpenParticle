package yancey.openparticle.core.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class RunningEventManager implements ServerTickEvents.StartTick {

    public static final RunningEventManager INSTANCE = new RunningEventManager();
    private final List<RunningHandler> runningHandlerList = new ArrayList<>();
    private final List<RunningHandler> runningHandlerAddList = new ArrayList<>();
    private final List<RunningHandler> runningHandlerRemoveList = new ArrayList<>();

    private RunningEventManager() {
        ServerTickEvents.START_SERVER_TICK.register(this);
    }

    public void run(RunningHandler runningHandler) {
        if (!runningHandlerAddList.contains(runningHandler)) {
            runningHandlerAddList.add(runningHandler);
        }
    }

    public void stop(RunningHandler runningHandler) {
        if (!runningHandlerRemoveList.contains(runningHandler)) {
            runningHandlerRemoveList.add(runningHandler);
        }
    }

    @Override
    public void onStartTick(MinecraftServer server) {
        if (!runningHandlerRemoveList.isEmpty()) {
            runningHandlerList.removeAll(runningHandlerRemoveList);
            runningHandlerRemoveList.clear();
        }
        if (!runningHandlerAddList.isEmpty()) {
            runningHandlerList.addAll(runningHandlerAddList);
            runningHandlerAddList.clear();
        }
        for (RunningHandler runningHandler : runningHandlerList) {
            if (runningHandler.isRunning) {
                runningHandler.runPerTick();
            } else {
                runningHandler.stop();
            }
        }
    }
}
