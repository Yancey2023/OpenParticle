package yancey.openparticle.core.client;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import yancey.openparticle.core.client.core.OpenParticleClientCore;
import yancey.openparticle.core.keys.KeyboardManager;
import yancey.openparticle.core.network.LoadAndRunPayloadS2C;
import yancey.openparticle.core.network.LoadPayloadS2C;
import yancey.openparticle.core.network.RunPayloadS2C;
import yancey.openparticle.core.network.RunTickPayloadS2C;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class OpenParticleClient implements ClientModInitializer {

    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitializeClient() {
        String fileName = "libOpenParticle-amd64." + (System.getProperty("os.name").toLowerCase().contains("windows") ? "dll" : "so");
        URL source = Objects.requireNonNull(getClass().getClassLoader().getResource("native/" + fileName));
        Path dest = MinecraftClient.getInstance().runDirectory.toPath().resolve("openparticle").resolve(fileName);
        try {
            if (!Files.exists(dest.getParent())) {
                Files.createDirectories(dest.getParent());
            }
            Files.copy(source.openStream(), dest, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            LOGGER.error("fail to copy native core of open particle", e);
            if (!Files.exists(dest)) {
                throw new RuntimeException(e);
            }
        }
        System.load(dest.toString());
        RunTickPayloadS2C.ID.registerClientGlobalReceiver((payload, client, player) ->
                OpenParticleClientCore.runTick(payload.path, payload.tick, payload.isSingleThread));
        LoadAndRunPayloadS2C.ID.registerClientGlobalReceiver((payload, client, player) ->
                OpenParticleClientCore.loadAndRun(payload.path, payload.isSingleThread));
        LoadPayloadS2C.ID.registerClientGlobalReceiver((payload, client, player) ->
                OpenParticleClientCore.loadFile(payload.path));
        RunPayloadS2C.ID.registerClientGlobalReceiver((payload, client, player) ->
                OpenParticleClientCore.run(payload.path, payload.isSingleThread));
        KeyboardManager.init(true);
        WorldRenderEvents.START.register(context -> OpenParticleClientCore.inRendered = false);
    }
}
