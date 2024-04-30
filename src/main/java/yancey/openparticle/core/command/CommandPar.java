package yancey.openparticle.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import yancey.openparticle.core.core.OpenParticleServerCore;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CommandPar {

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        RequiredArgumentBuilder<ServerCommandSource, String> path = argument("path", StringArgumentType.greedyString());
        dispatcher.register(literal("par")
                .requires(source -> source.hasPermissionLevel(2))
                .then(literal("loadAndRun").then(path.executes(context -> execute(context, true, true))))
                .then(literal("load").then(path.executes(context -> execute(context, true, false))))
                .then(literal("run").executes(context -> execute(context, false, true))));
    }

    public static int execute(CommandContext<ServerCommandSource> context, boolean isLoad, boolean isRun) {
        if (isLoad) {
            String path = StringArgumentType.getString(context, "path");
            if (isRun) {
                OpenParticleServerCore.loadAndRun(context.getSource().getServer(), path);
            } else {
                OpenParticleServerCore.loadFile(context.getSource().getServer(), path);
            }
        } else if (isRun) {
            OpenParticleServerCore.run(context.getSource().getServer());
            return 3;
        }
        return Command.SINGLE_SUCCESS;
    }

}
