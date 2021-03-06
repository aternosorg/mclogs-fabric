package gs.mclo.fabric;

import gs.mclo.java.APIResponse;
import gs.mclo.java.MclogsAPI;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Style;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.IOException;

public class MclogsFabricLoader implements ModInitializer {
    public static final Logger logger = LogManager.getLogger();

    public static int share(ServerCommandSource source, String filename) {
        logger.log(Level.INFO,"Sharing "+filename);
        MclogsAPI.mcversion = source.getMinecraftServer().getVersion();
        try {
            String logpath = source.getMinecraftServer().getFile("logs/"+filename).getCanonicalPath();
            APIResponse response = MclogsAPI.share(logpath);
            if (response.success) {
                LiteralText feedback = new LiteralText("Your log has been uploaded: ");
                feedback.setStyle(new Style().setColor(Formatting.GREEN));

                LiteralText link = new LiteralText(response.url);
                Style linkStyle = new Style().setColor(Formatting.BLUE);
                linkStyle = linkStyle.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL,response.url));
                link.setStyle(linkStyle);

                source.sendFeedback(feedback.append(link),true);
                return 1;
            }
            else {
                logger.error("An error occurred when uploading your log: ");
                logger.error(response.error);
                LiteralText error = new LiteralText("An error occurred. Check your log for more details");
                source.sendError(error);
                return 0;
            }
        }
        catch (FileNotFoundException e) {
            LiteralText error = new LiteralText("The log file "+filename+" doesn't exist. Use '/mclogs list' to list all logs.");
            source.sendError(error);
            return -1;
        }
        catch (IOException e) {
            source.sendError(new LiteralText("An error occurred. Check your log for more details"));
            logger.error("Could not get log file!");
            logger.error(e);
            return 0;
        }
    }

    @Override
    public void onInitialize() {
        MclogsAPI.userAgent = "Mclogs-fabric";
        MclogsAPI.version = FabricLoader.getInstance().getModContainer("mclogs").get().getMetadata().getVersion().getFriendlyString();
        MclogsAPI.mcversion = "1.16.3";

        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            CommandMclogs.register(dispatcher);
            CommandMclogsList.register(dispatcher);
            CommandMclogsShare.register(dispatcher);
        });
    }
}
