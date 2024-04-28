package yancey.openparticle.core;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import yancey.openparticle.core.command.CommandPar;
import yancey.openparticle.core.keys.KeyboardManager;
import yancey.openparticle.core.network.KeyboardPayloadC2S;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class OpenParticle implements ModInitializer {

    public static final boolean isDebug = false;
    public static final String MOD_ID = "openparticle";
    private static final Logger LOGGER = LogUtils.getLogger();

    @Override
    public void onInitialize() {
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
        PayloadTypeRegistry.playC2S().register(KeyboardPayloadC2S.ID, KeyboardPayloadC2S.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(KeyboardPayloadC2S.ID, (payload, context) ->
                context.player().server.execute(() ->
                        KeyboardManager.runInServe(payload.idList(), context.player())));
        KeyboardManager.init(false);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "better_particle"), FabricParticleTypes.simple());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                CommandPar.init(dispatcher));
    }
}
