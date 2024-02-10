package yancey.openparticle.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.command.CommandSource;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import yancey.openparticle.core.core.OpenParticleCore;
import yancey.openparticle.core.network.NetworkHandler;

import java.util.function.Function;

public class CommandPar {

    public static <T extends CommandSource> void init(CommandDispatcher<T> dispatcher, boolean isInitInServer) {
        if (isInitInServer) {
            command(dispatcher, true, false);
        }
        command(dispatcher, isInitInServer, true);
    }

    private static <T extends CommandSource> void command(CommandDispatcher<T> dispatcher, boolean isInitInServer, boolean isRunInClient) {
        LiteralArgumentBuilder<T> builder = LiteralArgumentBuilder.literal(isRunInClient ? "parc" : "par");
        Function<String, LiteralArgumentBuilder<T>> literal = LiteralArgumentBuilder::literal;
        if (isInitInServer) {
            builder.requires(source -> source.hasPermissionLevel(2));
        } else {
            builder.then(literal.apply("stop").executes(context -> {
                OpenParticleCore.clearParticle();
                return 1;
            }));
        }
        RequiredArgumentBuilder<T, String> path = RequiredArgumentBuilder.argument("path", StringArgumentType.greedyString());
        Function<Identifier, RequiredArgumentBuilder<T, String>> execute = identifier ->
                path.executes(executeFile(identifier, isRunInClient));
        dispatcher.register(builder
                .then(literal.apply("loadAndRun")
                        .then(execute.apply(NetworkHandler.ID_LOAD_AND_RUN)))
                .then(literal.apply("load")
                        .then(execute.apply(NetworkHandler.ID_LOAD)))
                .then(literal.apply("run")
                        .executes(executeFile(NetworkHandler.ID_RUN, isRunInClient)))
        );
    }

    private static <T extends CommandSource> Command<T> executeFile(Identifier identifier, boolean isRunInClient) {
        return context -> {
            String path = null;
            if (identifier != NetworkHandler.ID_RUN) {
                path = StringArgumentType.getString(context, "path");
            }
            World world;
            CommandSource source = context.getSource();
            if (source instanceof FabricClientCommandSource clientCommandSource) {
                world = clientCommandSource.getWorld();
            } else if (source instanceof ServerCommandSource serverCommandSource) {
                world = serverCommandSource.getWorld();
                if (isRunInClient) {
                    PacketByteBuf packetByteBuf = PacketByteBufs.create();
                    if (path != null) {
                        packetByteBuf.writeString(path);
                    }
                    for (ServerPlayerEntity serverPlayerEntity : serverCommandSource.getServer().getPlayerManager().getPlayerList()) {
                        ServerPlayNetworking.send(serverPlayerEntity, identifier, packetByteBuf);
                    }
                    return 1;
                }
            } else {
                throw new RuntimeException("unknown command source");
            }
            boolean isSuccess = true;
            if (identifier == NetworkHandler.ID_LOAD) {
                isSuccess = OpenParticleCore.loadFile(path);
            } else if (identifier == NetworkHandler.ID_RUN) {
                OpenParticleCore.run(OpenParticleCore.lastPath, world);
            } else if (identifier == NetworkHandler.ID_LOAD_AND_RUN) {
                isSuccess = OpenParticleCore.loadAndRun(path, world);
            } else {
                throw new RuntimeException("unknown identifier -> " + identifier);
            }
            return isSuccess ? 1 : -1;
        };
    }

}
