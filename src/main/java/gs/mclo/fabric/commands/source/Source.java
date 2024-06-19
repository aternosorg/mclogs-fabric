package gs.mclo.fabric.commands.source;

import net.minecraft.text.Text;

import java.nio.file.Path;

public interface Source {
    String getMinecraftVersion();
    void sendFeedback(Text message, boolean broadcastToOps);
    void sendError(Text message);
    Path getRunDirectory();
}
