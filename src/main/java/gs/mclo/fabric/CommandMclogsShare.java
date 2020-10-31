package gs.mclo.fabric;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class CommandMclogsShare {
    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mclogs").then(literal("share")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("filename", StringArgumentType.greedyString())
            .executes(context ->  MclogsFabricLoader.share(context.getSource(), context.getArgument("filename",String.class))))
        ));
    }
}
