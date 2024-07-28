package yancey.openparticle.core;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import yancey.openparticle.core.command.CommandPar;
import yancey.openparticle.core.core.OpenParticleServerCore;
import yancey.openparticle.core.keys.KeyboardManager;
import yancey.openparticle.core.network.*;

public class OpenParticle implements ModInitializer {

    public static final String MOD_ID = "openparticle";

    @Override
    public void onInitialize() {
        PayloadTypeRegistry.playC2S().register(KeyboardPayloadC2S.ID, KeyboardPayloadC2S.CODEC);
        PayloadTypeRegistry.playC2S().register(RunPayloadC2S.ID, RunPayloadC2S.CODEC);
        PayloadTypeRegistry.playS2C().register(RunTickPayloadS2C.ID, RunTickPayloadS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(LoadAndRunPayloadS2C.ID, LoadAndRunPayloadS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(LoadPayloadS2C.ID, LoadPayloadS2C.CODEC);
        PayloadTypeRegistry.playS2C().register(RunPayloadS2C.ID, RunPayloadS2C.CODEC);
        ServerPlayNetworking.registerGlobalReceiver(KeyboardPayloadC2S.ID, (payload, context) ->
                context.player().server.execute(() -> KeyboardManager.runInServe(payload.idList(), context.player())));
        ServerPlayNetworking.registerGlobalReceiver(RunPayloadC2S.ID, (payload, context) ->
                OpenParticleServerCore.run(context.player().server, payload.path(), payload.tickEnd(), payload.isSingleThread()));
        KeyboardManager.init(false);
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CommandPar.init(dispatcher));
    }
}
