package yancey.openparticle.core.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;
import yancey.openparticle.core.core.OpenParticleServerCore;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class CommandPar {

    public static void init(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("par")
                .requires(source -> source.hasPermissionLevel(2))
                .then(literal("loadAndRun")
                        .then(argument("isSingleThread", BoolArgumentType.bool())
                                .then(argument("path", StringArgumentType.greedyString())
                                        .executes(context -> execute(context, true, true, true)))))
                .then(literal("load")
                        .then(argument("path", StringArgumentType.greedyString())
                                .executes(context -> execute(context, true, false, false))))
                .then(literal("run")
                        .executes(context -> execute(context, false, true, false))
                        .then(argument("isSingleThread", BoolArgumentType.bool())
                                .executes(context -> execute(context, false, true, true)))));
    }

    public static int execute(CommandContext<ServerCommandSource> context, boolean isLoad, boolean isRun, boolean isHasSingleThread) {
        if (isLoad) {
            String path = StringArgumentType.getString(context, "path");
            if (isRun) {
                boolean isSingleThread = !isHasSingleThread || BoolArgumentType.getBool(context, "isSingleThread");
                OpenParticleServerCore.loadAndRun(context.getSource().getServer(), path, isSingleThread);
            } else {
                OpenParticleServerCore.loadFile(context.getSource().getServer(), path);
            }
        } else if (isRun) {
            boolean isSingleThread = !isHasSingleThread || BoolArgumentType.getBool(context, "isSingleThread");
            OpenParticleServerCore.run(context.getSource().getServer(), isSingleThread);
            return 3;
        }
        return Command.SINGLE_SUCCESS;
    }

}
