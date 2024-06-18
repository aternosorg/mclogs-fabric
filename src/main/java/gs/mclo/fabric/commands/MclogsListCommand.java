package gs.mclo.fabric.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import gs.mclo.fabric.MclogsFabric;
import gs.mclo.fabric.commands.source.ClientSource;
import gs.mclo.fabric.commands.source.ServerSource;
import gs.mclo.fabric.commands.source.Source;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;


public class MclogsListCommand implements Command {
    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> buildClient(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.then(ClientCommandManager
                .literal("list")
                .executes((context) -> execute(new ClientSource(context.getSource()), "mclogsc"))
        );
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> buildServer(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder.then(CommandManager
                .literal("list")
                .requires(source -> source.hasPermissionLevel(2))
                .executes((context) -> execute(new ServerSource(context.getSource()), "mclogs"))
        );
    }

    private int execute(Source source, String command) {
        try {
            int total = 0;

            MutableText message = new LiteralText("");


            total += list(message, MclogsFabric.getLogs(source), "Available Logs:", command);
            message.append("\n");
            total += list(message, MclogsFabric.getCrashReports(source), "Available Crash Reports:", command);

            if (total == 0) {
                message = new LiteralText("No logs or crash reports found.").formatted(Formatting.RED);
            }

            source.sendFeedback(message, false);
            return total;
        } catch (Exception e) {
            MclogsFabric.logger.error("An error occurred when listing your logs.");
            MclogsFabric.logger.error(e);
            LiteralText error = new LiteralText("An error occurred. Check your log for more details.");
            source.sendError(error);
            return -1;
        }
    }

    private int list(MutableText message, String[] items, String title, String command) {
        if (items.length > 0) {
            message.append(title(title));
            for (String log : items) {
                message.append(item(log, command));
            }
        }
        return items.length;
    }

    private MutableText title(String title) {
        return new LiteralText(title).setStyle(Style.EMPTY.withFormatting(Formatting.UNDERLINE));
    }

    private MutableText item(String filename, String command) {
        return new LiteralText("\n" + filename)
                .setStyle(Style.EMPTY.withClickEvent(
                        new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/"+ command + " share " + filename)
                ));
    }
}
