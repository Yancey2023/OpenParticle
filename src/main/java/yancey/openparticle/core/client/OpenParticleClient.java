package yancey.openparticle.core.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import yancey.openparticle.core.core.OpenParticleCore;
import yancey.openparticle.core.keys.KeyboardManager;
import yancey.openparticle.core.network.RunTickPayloadS2C;

public class OpenParticleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        PayloadTypeRegistry.playS2C().register(RunTickPayloadS2C.ID, RunTickPayloadS2C.CODEC);
        ClientPlayNetworking.registerGlobalReceiver(RunTickPayloadS2C.ID, (payload, context) -> {
            if (context.player().getWorld() == null) {
                return;
            }
            OpenParticleCore.runTick(payload.path(), payload.tick());
        });
        KeyboardManager.init(true);
    }
}
