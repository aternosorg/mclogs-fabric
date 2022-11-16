package gs.mclo.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;

import static net.minecraft.server.command.CommandManager.literal;

public class CommandMclogsList {
    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mclogs").then(literal("list")
                .requires(source -> source.hasPermissionLevel(2))
                .executes((context) -> {
                    ServerCommandSource source = context.getSource();

                    try {
                        int total = 0;
                        MutableText message = Text.empty();

                        message.append(Text.literal("Available logs:")
                            .setStyle(Style.EMPTY
                                .withColor(Formatting.GREEN)
                                .withBold(true)
                            ));
                        for (String log : MclogsFabricLoader.getLogs(context)) {
                            MutableText tempText = Text.literal("\n" + log)
                                .setStyle(
                                    Style.EMPTY
                                    .withClickEvent(
                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/mclogs share " + log)
                                    )
                                );
                            message.append(tempText);
                            total ++;
                        }

                        message.append(Text.literal("\nAvailable crash reports:")
                                .setStyle(Style.EMPTY
                                        .withColor(Formatting.GREEN)
                                        .withBold(true)
                                ));
                        for (String report : MclogsFabricLoader.getCrashReports(context)) {
                            MutableText tempText = Text.literal("\n" + report)
                                .setStyle(
                                    Style.EMPTY
                                    .withClickEvent(
                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/mclogs share " + report)
                                    )
                                );
                            message.append(tempText);
                            total ++;
                        }


                        source.sendFeedback(message, false);
                        return total;
                    }
                    catch (Exception e) {
                        MclogsFabricLoader.logger.error("An error occurred when listing your logs.");
                        MclogsFabricLoader.logger.error(e);
                        source.sendError(Text.literal("An error occurred. Check your log for more details."));
                        return -1;
                    }
                })
        ));
    }
}
