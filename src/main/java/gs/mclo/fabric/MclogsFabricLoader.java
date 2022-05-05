package gs.mclo.fabric;

import com.mojang.brigadier.context.CommandContext;
import gs.mclo.java.APIResponse;
import gs.mclo.java.MclogsAPI;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.*;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class MclogsFabricLoader implements DedicatedServerModInitializer {
    public static final Logger logger = LogManager.getLogger();

    /**
     * @param context command context
     * @return log files
     * @throws IOException io exception
     */
    public static String[] getLogs(CommandContext<ServerCommandSource> context) throws IOException {
        return MclogsAPI.listLogs(context.getSource().getServer().getRunDirectory().getCanonicalPath());
    }

    public static int share(ServerCommandSource source, String filename) {
        MclogsAPI.mcversion = source.getServer().getVersion();
        logger.log(Level.INFO,"Sharing "+filename);
        source.sendFeedback(Text.literal("Sharing " + filename), false);
        try {
            Path logs = source.getServer().getFile("logs/").toPath();
            Path log = logs.resolve(filename);
            if (!log.getParent().equals(logs)) {
                throw new FileNotFoundException();
            }
            APIResponse response = MclogsAPI.share(log);
            if (response.success) {
                MutableText feedback = Text.literal("Your log has been uploaded: ");
                feedback.setStyle(Style.EMPTY.withColor(Formatting.GREEN));

                MutableText link = Text.literal(response.url);
                Style linkStyle = Style.EMPTY.withColor(Formatting.BLUE);
                linkStyle = linkStyle.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,response.url));
                link.setStyle(linkStyle);

                source.sendFeedback(feedback.append(link),true);
                return 1;
            }
            else {
                logger.error("An error occurred when uploading your log: ");
                logger.error(response.error);
                source.sendError(Text.literal("An error occurred. Check your log for more details"));
                return 0;
            }
        }
        catch (FileNotFoundException|IllegalArgumentException e) {
            source.sendError(Text.literal("The log file "+filename+" doesn't exist. Use '/mclogs list' to list all logs."));
            return -1;
        }
        catch (IOException e) {
            source.sendError(Text.literal("An error occurred. Check your log for more details"));
            logger.error("Could not get log file!");
            logger.error(e);
            return 0;
        }
    }

    @Override
    public void onInitializeServer() {
        MclogsAPI.userAgent = "Mclogs-fabric";
        Optional<ModContainer> mclogs = FabricLoader.getInstance().getModContainer("mclogs");
        MclogsAPI.version = mclogs.isPresent() ? mclogs.get().getMetadata().getVersion().getFriendlyString() : "unknown";

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            CommandMclogs.register(dispatcher);
            CommandMclogsList.register(dispatcher);
            CommandMclogsShare.register(dispatcher);
        });
    }
}
