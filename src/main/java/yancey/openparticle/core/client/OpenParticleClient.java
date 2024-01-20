package yancey.openparticle.core.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import yancey.openparticle.core.keys.KeyboardManager;
import yancey.openparticle.core.network.NetworkHandler;
import yancey.openparticle.core.util.OpenParticleUtil;

public class OpenParticleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NetworkHandler.initClient();
        KeyboardManager.init(true);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("par")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(ClientCommandManager.literal("loadAndRun")
                                .then(ClientCommandManager.argument("path", StringArgumentType.greedyString())
                                        .executes(context -> OpenParticleUtil.loadAndRum(
                                                StringArgumentType.getString(context, "path"),
                                                context.getSource().getWorld()
                                        ) ? 1 : -1)))
                        .then(ClientCommandManager.literal("load")
                                .then(ClientCommandManager.argument("path", StringArgumentType.greedyString())
                                        .executes(context -> OpenParticleUtil.loadFile(
                                                StringArgumentType.getString(context, "path")
                                        ) ? 1 : -1)))
                        .then(ClientCommandManager.literal("run")
                                .executes(context -> {
                                    OpenParticleUtil.run(context.getSource().getWorld());
                                    return 1;
                                }))
        ));
    }
}
