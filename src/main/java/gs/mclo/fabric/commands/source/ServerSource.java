package gs.mclo.fabric.commands.source;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.nio.file.Path;

public class ServerSource implements Source {
    private final ServerCommandSource parent;

    public ServerSource(ServerCommandSource parent) {
        this.parent = parent;
    }

    @Override
    public String getMinecraftVersion() {
        return parent.getServer().getVersion();
    }

    @Override
    public void sendFeedback(Text message, boolean broadcastToOps) {
        parent.sendFeedback(() -> message, broadcastToOps);
    }

    @Override
    public void sendError(Text message) {
        parent.sendError(message);
    }

    @Override
    public Path getRunDirectory() {
        return parent.getServer().getRunDirectory();
    }
}
