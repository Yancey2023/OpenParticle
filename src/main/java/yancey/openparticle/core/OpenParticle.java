package yancey.openparticle.core;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import yancey.openparticle.core.keys.KeyboardManager;
import yancey.openparticle.core.network.NetworkHandler;

public class OpenParticle implements ModInitializer {

    public static final String MOD_ID = "openparticle";

    @Override
    public void onInitialize() {
        NetworkHandler.initServer();
        KeyboardManager.init(false);
        Registry.register(Registries.PARTICLE_TYPE, new Identifier(MOD_ID, "better_particle"), FabricParticleTypes.simple());
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(
                    CommandManager.literal("par").requires(source -> source.hasPermissionLevel(2))
                            .then(CommandManager.literal("loadAndRun")
                                    .then(CommandManager.argument("path", StringArgumentType.greedyString()).executes(context -> {
                                        PacketByteBuf packetByteBuf = PacketByteBufs.create();
                                        packetByteBuf.writeString(StringArgumentType.getString(context, "path"));
                                        for (ServerPlayerEntity serverPlayerEntity : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                            ServerPlayNetworking.send(serverPlayerEntity, NetworkHandler.RUN_AND_RUN_ON_CLIENT, packetByteBuf);
                                        }
                                        return 1;
                                    })))
                            .then(CommandManager.literal("load").then(CommandManager.argument("path", StringArgumentType.greedyString()).executes(context -> {
                                PacketByteBuf packetByteBuf = PacketByteBufs.create();
                                packetByteBuf.writeString(StringArgumentType.getString(context, "path"));
                                for (ServerPlayerEntity serverPlayerEntity : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                    ServerPlayNetworking.send(serverPlayerEntity, NetworkHandler.LOAD_ON_CLIENT, packetByteBuf);
                                }
                                return 1;
                            })))
                            .then(CommandManager.literal("run").executes(context -> {
                                PacketByteBuf packetByteBuf = PacketByteBufs.empty();
                                for (ServerPlayerEntity serverPlayerEntity : context.getSource().getServer().getPlayerManager().getPlayerList()) {
                                    ServerPlayNetworking.send(serverPlayerEntity, NetworkHandler.RUN_ON_CLIENT, packetByteBuf);
                                }
                                return 1;
                            })));
        });
    }
}
