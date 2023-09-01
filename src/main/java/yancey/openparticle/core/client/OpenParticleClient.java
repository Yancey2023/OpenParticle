package yancey.openparticle.core.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.util.math.Vec3d;
import yancey.openparticle.api.math.Vec3;
import yancey.openparticle.core.keys.KeyboardManager;
import yancey.openparticle.core.network.NetworkHandler;
import yancey.openparticle.core.util.OpenParticleUtil;

public class OpenParticleClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyboardManager.init(true);
        NetworkHandler.initClient();
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> dispatcher.register(
                ClientCommandManager.literal("par")
                        .requires(source -> source.hasPermissionLevel(2))
                        .then(ClientCommandManager.literal("loadAndRun")
                                .then(ClientCommandManager.argument("path", StringArgumentType.greedyString())
                                        .executes(context -> {
                                            Vec3d position = context.getSource().getPosition();
                                            return OpenParticleUtil.loadAndRum(
                                                    new Vec3(position.x, position.y, position.z),
                                                    StringArgumentType.getString(context, "path"),
                                                    context.getSource().getWorld()
                                            ) ? 1 : -1;
                                        })))
                        .then(ClientCommandManager.literal("load")
                                .then(ClientCommandManager.argument("path", StringArgumentType.greedyString())
                                        .executes(context -> OpenParticleUtil.loadFile(
                                                StringArgumentType.getString(context, "path")
                                        ) ? 1 : -1)))
                        .then(ClientCommandManager.literal("run")
                                .executes(context -> {
                                    Vec3d position = context.getSource().getPosition();
                                    OpenParticleUtil.run(new Vec3(position.x, position.y, position.z), context.getSource().getWorld());
                                    return 1;
                                }))
        ));
    }
}
