package gs.mclo.fabric.commands;

import com.google.common.collect.ImmutableList;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import gs.mclo.fabric.MclogsFabric;
import gs.mclo.fabric.commands.source.ClientSource;
import gs.mclo.fabric.commands.source.ServerSource;
import gs.mclo.fabric.commands.source.Source;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.client.network.ClientCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;

import java.io.IOException;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.literal;

public class MclogsShareCommand implements Command {
    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> buildClient(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.then(ClientCommandManager
                .literal("share")
                .then(ClientCommandManager
                        .argument("filename", StringArgumentType.greedyString())
                        .suggests((x, y) -> this.suggest(x, y, new ClientSource(x.getSource())))
                        .executes(context -> MclogsFabric.share(
                                new ClientSource(context.getSource()),
                                context.getArgument("filename", String.class)
                        ))
                )
        );
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> buildServer(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder.then(CommandManager
                .literal("share")
                .requires(source -> source.hasPermissionLevel(2))
                .then(CommandManager.argument("filename", StringArgumentType.greedyString())
                        .suggests((x, y) -> this.suggest(x, y, new ServerSource(x.getSource())))
                        .executes(context -> MclogsFabric.share(
                                new ServerSource(context.getSource()),
                                context.getArgument("filename", String.class)
                        ))
                )
        );
    }

    private CompletableFuture<Suggestions> suggest(CommandContext<?> context, SuggestionsBuilder builder, Source source) {
        ImmutableList.Builder<Suggestion> suggestions = ImmutableList.builder();
        String[] logs, reports;
        try {
            logs = MclogsFabric.getLogs(source);
            reports = MclogsFabric.getCrashReports(source);
        } catch (IOException e) {
            MclogsFabric.logger.error("Failed to suggest log files", e);
            return Suggestions.empty();
        }

        String argument = builder.getRemaining();
        int start = "/mclogs share ".length();

        for (String log : logs) {
            if (!log.startsWith(argument)) continue;
            suggestions.add(new Suggestion(StringRange.between(start, context.getInput().length()), log));
        }

        for (String report : reports) {
            if (!report.startsWith(argument)) continue;
            suggestions.add(new Suggestion(StringRange.between(start, context.getInput().length()), report));
        }

        return CompletableFuture.completedFuture(Suggestions.create("mclogs", suggestions.build()));
    }
}
