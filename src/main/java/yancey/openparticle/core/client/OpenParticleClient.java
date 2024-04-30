package yancey.openparticle.core.client;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import org.slf4j.Logger;
import yancey.openparticle.core.core.OpenParticleClientCore;
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
        URL source = Objects.requireNonNull(getClass().getClassLoader().getResource("native/libOpenParticle.dll"));
        Path dest = MinecraftClient.getInstance().runDirectory.toPath().resolve("openparticle").resolve("libOpenParticle.dll");
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
        ClientPlayNetworking.registerGlobalReceiver(RunTickPayloadS2C.ID, (payload, context) ->
                OpenParticleClientCore.runTick(payload.path(), payload.tick()));
        ClientPlayNetworking.registerGlobalReceiver(LoadAndRunPayloadS2C.ID, (payload, context) ->
                OpenParticleClientCore.loadAndRun(payload.path()));
        ClientPlayNetworking.registerGlobalReceiver(LoadPayloadS2C.ID, (payload, context) ->
                OpenParticleClientCore.loadFile(payload.path()));
        ClientPlayNetworking.registerGlobalReceiver(RunPayloadS2C.ID, (payload, context) ->
                OpenParticleClientCore.run(payload.path()));
        KeyboardManager.init(true);
    }
}
