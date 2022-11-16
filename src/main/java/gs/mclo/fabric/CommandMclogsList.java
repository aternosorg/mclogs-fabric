package gs.mclo.fabric;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
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

                        Text message = new LiteralText("");

                        message.append(new LiteralText("Available logs:")
                            .setStyle(new Style()
                                .setColor(Formatting.GREEN)
                                .setBold(true)
                            ));
                        for (String log : MclogsFabricLoader.getLogs(context)) {
                            Text tempText = new LiteralText("\n" + log)
                                .setStyle(
                                    new Style()
                                    .setClickEvent(
                                        new ClickEvent(ClickEvent.Action.RUN_COMMAND,"/mclogs share " + log)
                                    )
                                );
                            message.append(tempText);
                            total ++;
                        }

                        message.append(new LiteralText("\nAvailable crash reports:")
                                .setStyle(new Style()
                                        .setColor(Formatting.GREEN)
                                        .setBold(true)
                                ));
                        for (String report : MclogsFabricLoader.getCrashReports(context)) {
                            Text tempText = new LiteralText("\n" + report)
                                .setStyle(
                                    new Style()
                                    .setClickEvent(
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
                        LiteralText error = new LiteralText("An error occurred. Check your log for more details.");
                        source.sendError(error);
                        return -1;
                    }
                })
        ));
    }
}
