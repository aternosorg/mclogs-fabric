package gs.mclo.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;

import static net.minecraft.server.command.CommandManager.literal;

public class CommandMclogsList {
    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mclogs").then(literal("list")
                .requires(source -> source.hasPermissionLevel(2))
                .executes((context) -> {
                    ServerCommandSource source = context.getSource();

                    try {
                        String[] logs = MclogsFabricLoader.getLogs(context);

                        if (logs.length == 0) {
                            source.sendFeedback(Text.literal("No logs available!"), false);
                            return 0;
                        }

                        MutableText feedback = Text.literal("Available Logs:");
                        for (String log : logs) {
                            Style s = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/mclogs share " + log));
                            MutableText tempText = Text.literal("\n" + log);
                            tempText.setStyle(s);
                            feedback.append(tempText);
                        }
                        source.sendFeedback(feedback, false);
                        return logs.length;
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
