package gs.mclo.fabric.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import gs.mclo.fabric.MclogsFabric;
import gs.mclo.fabric.commands.source.ClientSource;
import gs.mclo.fabric.commands.source.ServerSource;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;
import net.minecraft.server.command.ServerCommandSource;


public class MclogsCommand implements Command {
    @Override
    public LiteralArgumentBuilder<FabricClientCommandSource> buildClient(LiteralArgumentBuilder<FabricClientCommandSource> builder) {
        return builder.executes(context -> MclogsFabric.share(new ClientSource(context.getSource()), "latest.log"));
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> buildServer(LiteralArgumentBuilder<ServerCommandSource> builder) {
        return builder
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> MclogsFabric.share(new ServerSource(context.getSource()), "latest.log"));
    }
}
