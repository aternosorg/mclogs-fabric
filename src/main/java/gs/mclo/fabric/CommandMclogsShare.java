package gs.mclo.fabric;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.literal;

public class CommandMclogsShare {
    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mclogs").then(literal("share")
            .requires(source -> source.hasPermissionLevel(2))
            .then(CommandManager.argument("filename", StringArgumentType.greedyString())
            .suggests(CommandMclogsShare::suggest)
            .executes(context ->  MclogsFabricLoader.share(context.getSource(), context.getArgument("filename",String.class))))
        ));
    }

    private static CompletableFuture<Suggestions> suggest(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) {
        ImmutableList.Builder<Suggestion> suggestions = ImmutableList.builder();
        String[] logs;
        try {
            logs = MclogsFabricLoader.getLogs(context);
        } catch (IOException e) {
            MclogsFabricLoader.logger.error("Failed to suggest log files", e);
            return Suggestions.empty();
        }

        String input = context.getInput();
        String[] args = input.split(" ");
        if (args.length > 3) return Suggestions.empty();

        String partialLogName = args.length == 3 ? args[2] : "";
        int start = "/mclogs share ".length();

        for (String log: logs) {
            if (!log.startsWith(partialLogName)) continue;
            suggestions.add(new Suggestion(StringRange.between(start, input.length()), log));
        }

        return CompletableFuture.completedFuture(Suggestions.create("mclogs", suggestions.build()));
    }
}
