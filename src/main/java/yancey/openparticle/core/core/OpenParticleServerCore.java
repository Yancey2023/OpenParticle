package yancey.openparticle.core.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import yancey.openparticle.core.events.RunningEventManager;
import yancey.openparticle.core.network.LoadAndRunPayloadS2C;
import yancey.openparticle.core.network.LoadPayloadS2C;
import yancey.openparticle.core.network.RunPayloadS2C;
import yancey.openparticle.core.network.RunTickPayloadS2C;

import java.util.Objects;

@Environment(EnvType.CLIENT)
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
        for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(serverPlayerEntity, new LoadPayloadS2C(path));
        }
    }

    public static void loadAndRun(MinecraftServer server, String path) {
        OpenParticleServerCore.path = Objects.requireNonNull(path);
        for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(serverPlayerEntity, new LoadAndRunPayloadS2C(path));
        }
    }

    public static void run(MinecraftServer server) {
        if (OpenParticleServerCore.path == null) {
            return;
        }
        RunPayloadS2C payload = new RunPayloadS2C(path);
        for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(serverPlayerEntity, payload);
        }
    }

    public static void run(MinecraftServer server, String path, int tickEnd) {
        stop();
        if (!Objects.equals(OpenParticleServerCore.path, path)) {
            loadAndRun(server, path);
            return;
        }
        nextTick = 0;
        OpenParticleServerCore.tickEnd = tickEnd;
        RunningEventManager.INSTANCE.run(() -> {
            if (OpenParticleServerCore.path == null || nextTick > OpenParticleServerCore.tickEnd) {
                runTick(server, OpenParticleServerCore.tickEnd);
                stop();
                return;
            }
            runTick(server, nextTick);
            nextTick++;
        });
    }

    public static void runTick(MinecraftServer server, int tick) {
        if (OpenParticleServerCore.path == null) {
            return;
        }
        RunTickPayloadS2C payload = new RunTickPayloadS2C(OpenParticleServerCore.path, tick);
        for (ServerPlayerEntity serverPlayerEntity : server.getPlayerManager().getPlayerList()) {
            ServerPlayNetworking.send(serverPlayerEntity, payload);
        }
    }

}
