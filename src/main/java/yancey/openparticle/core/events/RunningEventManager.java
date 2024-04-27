package yancey.openparticle.core.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.util.concurrent.locks.ReentrantLock;

public class RunningEventManager implements ServerTickEvents.StartTick {

    public static final RunningEventManager INSTANCE = new RunningEventManager();

    private RunningEventManager() {
        ServerTickEvents.START_SERVER_TICK.register(this);
    }

    private final ReentrantLock lock = new ReentrantLock();
    private Runnable runnable;

    public void run(Runnable runnable) {
        lock.lock();
        try {
            this.runnable = runnable;
        } finally {
            lock.unlock();
        }
    }

    public void stop() {
        lock.lock();
        try {
            this.runnable = null;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void onStartTick(MinecraftServer server) {
        lock.lock();
        try {
            if (runnable != null) {
                runnable.run();
            }
        } finally {
            lock.unlock();
        }
    }
}
