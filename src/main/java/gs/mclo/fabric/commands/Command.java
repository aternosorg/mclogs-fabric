package gs.mclo.fabric.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.server.command.ServerCommandSource;

public interface Command {
    LiteralArgumentBuilder<FabricClientCommandSource> buildClient(LiteralArgumentBuilder<FabricClientCommandSource> builder);
    LiteralArgumentBuilder<ServerCommandSource> buildServer(LiteralArgumentBuilder<ServerCommandSource> builder);
}
