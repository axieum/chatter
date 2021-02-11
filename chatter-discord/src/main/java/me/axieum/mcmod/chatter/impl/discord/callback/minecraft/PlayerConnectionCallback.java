package me.axieum.mcmod.chatter.impl.discord.callback.minecraft;

import me.axieum.mcmod.chatter.impl.discord.ChatterDiscord;
import me.axieum.mcmod.chatter.impl.discord.util.DiscordDispatcher;
import me.axieum.mcmod.chatter.impl.discord.util.StringUtils;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Disconnect;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.Join;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.CONFIG;

public class PlayerConnectionCallback implements Join, Disconnect
{
    // Mapping of player UUIDs to login time in milliseconds used for computing session play-time
    private static final HashMap<UUID, Long> PLAYER_LOGINS = new HashMap<>();

    @Override
    public void onPlayReady(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server)
    {
        // Update the player connection caches
        PLAYER_LOGINS.put(handler.player.getUuid(), System.currentTimeMillis());

        // Send Discord notifications
        ChatterDiscord.getClient().ifPresent(jda -> {
            // Prepare a message formatter
            final MessageFormat formatter = new MessageFormat()
                    .datetime("datetime")
                    .tokenize("player", handler.player.getDisplayName().getString())
                    .tokenize("world", StringUtils.getWorldName(handler.player.world));
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
                    .tokenize("world", StringUtils.getWorldName(player.world))
                    .duration("elapsed", getPlayerElapsed(player));
            // Dispatch a message to all configured channels
            DiscordDispatcher.embed((embed, entry) -> embed.setDescription(formatter.apply(entry.discord.leave))
                                                           .setThumbnail(CONFIG.theme.getAvatarUrl(handler.player, 16)),
                    (entry) -> entry.discord.leave != null);
        });

        // Update the player connection caches
        PLAYER_LOGINS.remove(handler.player.getUuid());
    }

    /**
     * Computes the duration in milliseconds since the player logged in.
     *
     * @param player player
     * @return duration since login or zero if unavailable
     */
    private static Duration getPlayerElapsed(ServerPlayerEntity player)
    {
        return Optional.ofNullable(PLAYER_LOGINS.get(player.getUuid()))
                       .map(login -> Duration.ofMillis(System.currentTimeMillis() - login))
                       .orElse(Duration.ZERO);
    }
}
