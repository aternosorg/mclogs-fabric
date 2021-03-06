package gs.mclo.fabric;

import com.mojang.brigadier.CommandDispatcher;
import gs.mclo.java.MclogsAPI;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;

import static net.minecraft.server.command.CommandManager.literal;

public class CommandMclogsList {
    static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(literal("mclogs").then(literal("list")
                .requires(source -> source.hasPermissionLevel(2))
                .executes((context) -> {
                    ServerCommandSource source = context.getSource();

                    try {
                        String[] logs = MclogsAPI.listLogs(context.getSource().getMinecraftServer().getRunDirectory().getCanonicalPath());

                        if (logs.length == 0) {
                            source.sendFeedback(new LiteralText("No logs available!"), false);
                            return 0;
                        }

                        LiteralText feedback = new LiteralText("Available Logs:");
                        for (String log : logs) {
                            Style s = Style.EMPTY.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/mclogs share " + log));
                            LiteralText tempText = new LiteralText("\n" + log);
                            tempText.setStyle(s);
                            feedback.append(tempText);
                        }
                        source.sendFeedback(feedback, false);
                        return logs.length;
                    }
                    catch (Exception e) {
                        MclogsFabricLoader.logger.error("An error occurred when listing your logs.");
                        MclogsFabricLoader.logger.error(e);
                        LiteralText error = new LiteralText("An error occurred. Check your log for more details.");
                        source.sendError(error);
                        return -1;
                    }
                })
        ));
    }
}
