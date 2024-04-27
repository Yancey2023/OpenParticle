package yancey.openparticle.core.client;

import net.fabricmc.api.ClientModInitializer;
import yancey.openparticle.core.keys.KeyboardManager;
import yancey.openparticle.core.network.NetworkHandler;

public class OpenParticleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NetworkHandler.initClient();
        KeyboardManager.init(true);
//        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
//                CommandPar.init(dispatcher, false));
    }
}
