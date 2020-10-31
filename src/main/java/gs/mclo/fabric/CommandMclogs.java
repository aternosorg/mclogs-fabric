package gs.mclo.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;

import static net.minecraft.server.command.CommandManager.literal;

public class CommandMclogs {
    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mclogs")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> MclogsFabricLoader.share(context.getSource(), "latest.log"))
        );
    }
}
