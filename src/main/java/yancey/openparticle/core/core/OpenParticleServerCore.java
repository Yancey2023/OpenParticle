package yancey.openparticle.core.core;

import net.minecraft.server.MinecraftServer;
import yancey.openparticle.core.events.RunningEventManager;
import yancey.openparticle.core.network.LoadAndRunPayloadS2C;
import yancey.openparticle.core.network.LoadPayloadS2C;
import yancey.openparticle.core.network.RunPayloadS2C;
import yancey.openparticle.core.network.RunTickPayloadS2C;

import java.util.Objects;

public class OpenParticleServerCore {

    public static String path;
    public static int nextTick, tickEnd;

    private OpenParticleServerCore() {

    }

    public static void stop() {
        RunningEventManager.INSTANCE.stop();
    }

    public static void loadFile(MinecraftServer server, String path) {
        OpenParticleServerCore.path = Objects.requireNonNull(path);
        LoadPayloadS2C.ID.sendToAllClient(server, new LoadPayloadS2C(path));
    }

    public static void loadAndRun(MinecraftServer server, String path, boolean isSingleThread) {
        OpenParticleServerCore.path = Objects.requireNonNull(path);
        LoadAndRunPayloadS2C.ID.sendToAllClient(server, new LoadAndRunPayloadS2C(path, isSingleThread));
    }

    public static void run(MinecraftServer server, boolean isSingleThread) {
        if (OpenParticleServerCore.path == null) {
            return;
        }
        RunPayloadS2C.ID.sendToAllClient(server, new RunPayloadS2C(path, isSingleThread));
    }

    public static void run(MinecraftServer server, String path, int tickEnd, boolean isSingleThread) {
        stop();
        if (!Objects.equals(OpenParticleServerCore.path, path)) {
            loadAndRun(server, path, isSingleThread);
            return;
        }
        nextTick = 0;
        OpenParticleServerCore.tickEnd = tickEnd;
        RunningEventManager.INSTANCE.run(() -> {
            if (OpenParticleServerCore.path == null || nextTick > OpenParticleServerCore.tickEnd) {
                runTick(server, OpenParticleServerCore.tickEnd, isSingleThread);
                stop();
                return;
            }
            runTick(server, nextTick, isSingleThread);
            nextTick++;
        });
    }

    public static void runTick(MinecraftServer server, int tick, boolean isSingleThread) {
        if (OpenParticleServerCore.path == null) {
            return;
        }
        RunTickPayloadS2C.ID.sendToAllClient(server, new RunTickPayloadS2C(OpenParticleServerCore.path, tick, isSingleThread));
    }

}
