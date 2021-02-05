package me.axieum.mcmod.chatter.impl.discord.callback.minecraft;

import me.axieum.mcmod.chatter.impl.discord.ChatterDiscord;
import me.axieum.mcmod.chatter.impl.discord.util.DiscordDispatcher;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopped;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopping;
import net.minecraft.server.MinecraftServer;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.LocalDateTime;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.CONFIG;
import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.LOGGER;

public class ServerLifecycleCallback implements ServerStarting, ServerStarted, ServerStopping, ServerStopped
{
    // Prepare a reusable message formatter for all server lifecycle events
    private static final MessageFormat FORMATTER = new MessageFormat()
            .datetime("datetime", LocalDateTime::now)
            .duration("elapsed", () -> Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime()));

    @Override
    public void onServerStarting(MinecraftServer server)
    {
        // Send Discord notifications
        ChatterDiscord.getClient().ifPresent(jda -> {
            // Update the Discord bot status
            jda.getPresence().setStatus(CONFIG.bot.status.starting);
            // Dispatch a message to all configured channels
            DiscordDispatcher.embed((embed, entry) -> embed.setDescription(FORMATTER.apply(entry.discord.starting)),
                    (entry) -> entry.discord.starting != null);
        });
    }

    @Override
    public void onServerStarted(MinecraftServer server)
    {
        // Send Discord notifications
        ChatterDiscord.getClient().ifPresent(jda -> {
            // Update the Discord bot status
            jda.getPresence().setStatus(CONFIG.bot.status.started);
            // Dispatch a message to all configured channels
            DiscordDispatcher.embed((embed, entry) -> embed.setDescription(FORMATTER.apply(entry.discord.started)),
                    (entry) -> entry.discord.started != null);
        });
    }

    @Override
    public void onServerStopping(MinecraftServer minecraftServer)
    {
        // Send Discord notifications
        ChatterDiscord.getClient().ifPresent(jda -> {
            // Update the Discord bot status
            jda.getPresence().setStatus(CONFIG.bot.status.stopping);
            // Dispatch a message to all configured channels
            DiscordDispatcher.embed((embed, entry) -> embed.setDescription(FORMATTER.apply(entry.discord.stopping)),
                    (entry) -> entry.discord.stopping != null);
        });
    }

    @Override
    public void onServerStopped(MinecraftServer minecraftServer)
    {
        // Send Discord notifications
        ChatterDiscord.getClient().ifPresent(jda -> {
            // Update the Discord bot status
            jda.getPresence().setStatus(CONFIG.bot.status.stopped);
            // Dispatch a message to all configured channels
            DiscordDispatcher.embed((embed, entry) -> embed.setDescription(FORMATTER.apply(entry.discord.stopped)),
                    (entry) -> entry.discord.stopped != null);
            // Shutdown the JDA client
            LOGGER.info("Wrapping up...");
            jda.shutdown();
        });
    }
}
