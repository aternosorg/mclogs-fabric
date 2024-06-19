package gs.mclo.fabric.commands.source;

import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.text.Text;

import java.nio.file.Path;

public class ClientSource implements Source {
    private final FabricClientCommandSource parent;

    public ClientSource(FabricClientCommandSource parent) {
        this.parent = parent;
    }

    @Override
    public String getMinecraftVersion() {
        return parent.getClient().getGameVersion();
    }

    @Override
    public void sendFeedback(Text message, boolean broadcastToOps) {
        parent.sendFeedback(message);
    }

    @Override
    public void sendError(Text message) {
        parent.sendError(message);
    }

    @Override
    public Path getRunDirectory() {
        return parent.getClient().runDirectory.toPath();
    }
}
