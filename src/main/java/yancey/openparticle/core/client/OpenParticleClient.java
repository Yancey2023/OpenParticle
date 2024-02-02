package yancey.openparticle.core.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import yancey.openparticle.core.command.CommandPar;
import yancey.openparticle.core.keys.KeyboardManager;
import yancey.openparticle.core.network.NetworkHandler;

public class OpenParticleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NetworkHandler.initClient();
        KeyboardManager.init(true);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) ->
                CommandPar.init(dispatcher, false));
    }
}
