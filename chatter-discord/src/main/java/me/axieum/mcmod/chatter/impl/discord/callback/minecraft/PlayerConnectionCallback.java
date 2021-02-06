package me.axieum.mcmod.chatter.impl.discord.callback.minecraft;

import me.axieum.mcmod.chatter.impl.discord.ChatterDiscord;
import me.axieum.mcmod.chatter.impl.discord.util.DiscordDispatcher;
import me.axieum.mcmod.chatter.impl.discord.util.ServerUtils;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.time.Duration;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.CONFIG;

public class PlayerConnectionCallback implements Join, Disconnect
{
    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server)
    {
        // Update the player connection caches
        ServerUtils.PLAYER_LOGINS.put(handler.player.getUuid(), System.currentTimeMillis());

        // Send Discord notifications
        ChatterDiscord.getClient().ifPresent(jda -> {
            // Prepare a message formatter
            final MessageFormat formatter = new MessageFormat()
                    .datetime("datetime")
                    .tokenize("player", handler.player.getDisplayName().getString())
                    .tokenize("world", ServerUtils.getWorldName(handler.player.world));
            // Dispatch a message to all configured channels
            DiscordDispatcher.embed((embed, entry) -> embed.setDescription(formatter.apply(entry.discord.join))
                                                           .setThumbnail(CONFIG.theme.getAvatarUrl(handler.player, 16)),
                    (entry) -> entry.discord.join != null);
        });
    }

    @Override
    public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server)
    {
        // Send Discord notifications
        ChatterDiscord.getClient().ifPresent(jda -> {
            final ServerPlayerEntity player = handler.player;
            // Prepare a message formatter
            final MessageFormat formatter = new MessageFormat()
                    .datetime("datetime")
                    .tokenize("player", player.getDisplayName().getString())
                    .tokenize("world", ServerUtils.getWorldName(player.world))
                    .duration("elapsed", Duration.ofMillis(ServerUtils.getPlayerElapsed(player)));
            // Dispatch a message to all configured channels
            DiscordDispatcher.embed((embed, entry) -> embed.setDescription(formatter.apply(entry.discord.leave))
                                                           .setThumbnail(CONFIG.theme.getAvatarUrl(handler.player, 16)),
                    (entry) -> entry.discord.leave != null);
        });

        // Update the player connection caches
        ServerUtils.PLAYER_LOGINS.remove(handler.player.getUuid());
    }
}
