package gs.mclo.fabric;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import gs.mclo.api.Log;
import gs.mclo.api.MclogsClient;
import gs.mclo.api.response.UploadLogResponse;
import gs.mclo.fabric.commands.Command;
import gs.mclo.fabric.commands.MclogsCommand;
import gs.mclo.fabric.commands.MclogsListCommand;
import gs.mclo.fabric.commands.MclogsShareCommand;
import gs.mclo.fabric.commands.source.Source;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.command.v1.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.command.CommandManager;
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
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class MclogsFabric implements DedicatedServerModInitializer, ClientModInitializer, ModInitializer {
    public static final Logger logger = LogManager.getLogger();
    private static final MclogsClient client = new MclogsClient("Mclogs-fabric");
    private static final Command[] COMMANDS = new Command[]{
            new MclogsCommand(),
            new MclogsListCommand(),
            new MclogsShareCommand()
    };

    /**
     * @param source command source
     * @return log files
     * @throws IOException io exception
     */
    public static String[] getLogs(Source source) throws IOException {
        return client.listLogsInDirectory(source.getRunDirectory().toString());
    }

    /**
     * @param source command source
     * @return crash reports
     * @throws IOException io exception
     */
    public static String[] getCrashReports(Source source) throws IOException {
        return client.listCrashReportsInDirectory(source.getRunDirectory().toString());
    }

    public static int share(Source source, String filename) {
        client.setMinecraftVersion(source.getMinecraftVersion());
        logger.log(Level.INFO, "Sharing {}", filename);
        source.sendFeedback(new LiteralText("Sharing " + filename), false);

        Path directory = source.getRunDirectory();
        Path logs = directory.resolve("logs");
        Path crashReports = directory.resolve("crash-reports");
        Path log = directory.resolve("logs").resolve(filename);

        if (!log.toFile().exists()) {
            log = directory.resolve("crash-reports").resolve(filename);
        }

        boolean isInAllowedDirectory = false;
        try {
            Path logPath = log.toRealPath();
            isInAllowedDirectory = (logs.toFile().exists() && logPath.startsWith(logs.toRealPath()))
                    || (crashReports.toFile().exists() && logPath.startsWith(crashReports.toRealPath()));
        } catch (IOException ignored) {
        }

        if (!log.toFile().exists() || !isInAllowedDirectory
                || !log.getFileName().toString().matches(Log.ALLOWED_FILE_NAME_PATTERN.pattern())) {
            source.sendError(new LiteralText("There is no log or crash report with the name '" + filename
                    + "'. Use '/mclogs list' to list all logs."));
            return -1;
        }

        try {
            CompletableFuture<UploadLogResponse> response = client.uploadLog(log);
            UploadLogResponse res = response.get();
            res.setClient(client);
            if (res.isSuccess()) {
                LiteralText feedback = new LiteralText("Your log has been uploaded: ");

                LiteralText link = new LiteralText(res.getUrl());
                Style linkStyle = Style.EMPTY
                        .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, res.getUrl()))
                        .withFormatting(Formatting.UNDERLINE);
                link.setStyle(linkStyle);

                source.sendFeedback(feedback.append(link), true);
                return 1;
            } else {
                logger.error("An error occurred when uploading your log: ");
                logger.error(res.getError());
                LiteralText error = new LiteralText("An error occurred. Check your log for more details");
                source.sendError(error);
                return 0;
            }
        } catch (FileNotFoundException | IllegalArgumentException e) {
            LiteralText error = new LiteralText("The log file " + filename + " doesn't exist. Use '/mclogs list' to list all logs.");
            source.sendError(error);
            return -1;
        } catch (IOException | InterruptedException | ExecutionException e) {
            source.sendError(new LiteralText("An error occurred. Check your log for more details"));
            logger.error("Could not get log file!");
            logger.error(e);
            return 0;
        }
    }

    @Override
    public void onInitialize() {
        Optional<ModContainer> mclogs = FabricLoader.getInstance().getModContainer("mclogs");
        client.setProjectVersion(mclogs.isPresent() ? mclogs.get().getMetadata().getVersion().getFriendlyString() : "unknown");
    }

    @Override
    public void onInitializeServer() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (!dedicated) {
                return;
            }

            logger.info("Registering server commands");
            LiteralArgumentBuilder<ServerCommandSource> mclogs = CommandManager.literal("mclogs");
            for (Command command : COMMANDS) {
                dispatcher.register(command.buildServer(mclogs));
            }
        });
    }

    @Override
    public void onInitializeClient() {
        logger.info("Registering client commands");
        LiteralArgumentBuilder<FabricClientCommandSource> mclogsc = ClientCommandManager.literal("mclogsc");
        for (Command command : COMMANDS) {
            ClientCommandManager.DISPATCHER.register(command.buildClient(mclogsc));
        }
    }

}
