package me.axieum.mcmod.chatter.impl.discord.callback.minecraft;

import me.axieum.mcmod.chatter.impl.discord.ChatterDiscord;
import me.axieum.mcmod.chatter.impl.discord.util.DiscordDispatcher;
import me.axieum.mcmod.chatter.impl.discord.util.ServerUtils;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

public class PlayerChangeWorldCallback implements ServerEntityWorldChangeEvents.AfterPlayerChange
{
    @Override
    public void afterChangeWorld(ServerPlayerEntity player, ServerWorld origin, ServerWorld destination)
    {
        // Send Discord notifications
        ChatterDiscord.getClient().ifPresent(jda -> {
            // Prepare a message formatter
            final MessageFormat formatter = new MessageFormat()
                    .datetime("datetime")
                    .tokenize("player", player.getDisplayName().getString())
                    .tokenize("origin", ServerUtils.getWorldName(origin))
                    .tokenize("destination", ServerUtils.getWorldName(destination));
            // Dispatch a message to all configured channels
            DiscordDispatcher.dispatch((message, entry) -> message.append(formatter.apply(entry.discord.teleport)),
                    (entry) -> entry.discord.teleport != null);
        });
    }
}
