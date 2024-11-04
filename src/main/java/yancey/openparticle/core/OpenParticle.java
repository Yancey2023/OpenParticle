package yancey.openparticle.core;

import net.fabricmc.api.ModInitializer;
import yancey.openparticle.core.command.CommandPar;
import yancey.openparticle.core.core.OpenParticleServerCore;
import yancey.openparticle.core.keys.KeyboardManager;
import yancey.openparticle.core.network.*;

//#if MC>=12005
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
//#endif

//#if MC>=11900
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
//#else
//$$ import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
//#endif

public class OpenParticle implements ModInitializer {

    public static final String MOD_ID = "openparticle";

    @Override
    public void onInitialize() {
        //#if MC>=12005
        PayloadTypeRegistry.playC2S().register(KeyboardPayloadC2S.ID.getId(), KeyboardPayloadC2S.ID.getCodec());
        PayloadTypeRegistry.playC2S().register(RunPayloadC2S.ID.getId(), RunPayloadC2S.ID.getCodec());
        PayloadTypeRegistry.playS2C().register(RunTickPayloadS2C.ID.getId(), RunTickPayloadS2C.ID.getCodec());
        PayloadTypeRegistry.playS2C().register(LoadAndRunPayloadS2C.ID.getId(), LoadAndRunPayloadS2C.ID.getCodec());
        PayloadTypeRegistry.playS2C().register(LoadPayloadS2C.ID.getId(), LoadPayloadS2C.ID.getCodec());
        PayloadTypeRegistry.playS2C().register(RunPayloadS2C.ID.getId(), RunPayloadS2C.ID.getCodec());
        //#endif
        KeyboardPayloadC2S.ID.registerServerGlobalReceiver((payload, server, player) ->
                player.server.execute(() -> KeyboardManager.runInServe(payload.idList, player)));
        RunPayloadC2S.ID.registerServerGlobalReceiver((payload, server, player) ->
                OpenParticleServerCore.run(player.server, payload.path, payload.tickEnd, payload.isSingleThread));
        KeyboardManager.init(false);
        //#if MC>=11900
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> CommandPar.register(dispatcher));
        //#else
        //$$ CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> CommandPar.register(dispatcher));
        //#endif
    }
}
