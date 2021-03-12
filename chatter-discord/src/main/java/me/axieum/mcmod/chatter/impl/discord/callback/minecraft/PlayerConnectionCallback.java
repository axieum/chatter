package me.axieum.mcmod.chatter.impl.discord.callback.minecraft;

import me.axieum.mcmod.chatter.impl.discord.ChatterDiscord;
import me.axieum.mcmod.chatter.impl.discord.config.module.MessageConfig.DimensionPredicate;
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
            final String playerName = handler.player.getDisplayName().getString();
            final MessageFormat formatter = new MessageFormat()
                    .datetime("datetime")
                    .tokenize("player", playerName)
                    .tokenize("world", StringUtils.getWorldName(handler.player.world));

            // Dispatch a message to all configured channels
            DiscordDispatcher.embed((embed, entry) -> embed.setDescription(formatter.apply(entry.discord.join))
                                                           .setThumbnail(CONFIG.theme.getAvatarUrl(playerName, 16)),
                    (entry) -> entry.discord.join != null,
                    new DimensionPredicate(StringUtils.getWorldId(handler.player.world)));
        });
    }

    @Override
    public void onPlayDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server)
    {
        // Send Discord notifications
        ChatterDiscord.getClient().ifPresent(jda -> {
            // Prepare a message formatter
            final String playerName = handler.player.getDisplayName().getString();
            final MessageFormat formatter = new MessageFormat()
                    .datetime("datetime")
                    .tokenize("player", playerName)
                    .tokenize("world", StringUtils.getWorldName(handler.player.world))
                    .duration("elapsed", getPlayerElapsed(handler.player));

            // Dispatch a message to all configured channels
            DiscordDispatcher.embed((embed, entry) -> embed.setDescription(formatter.apply(entry.discord.leave))
                                                           .setThumbnail(CONFIG.theme.getAvatarUrl(playerName, 16)),
                    (entry) -> entry.discord.leave != null,
                    new DimensionPredicate(StringUtils.getWorldId(handler.player.world)));
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
