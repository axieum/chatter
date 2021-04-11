package me.axieum.mcmod.chatter.impl.discord.callback.minecraft;

import me.axieum.mcmod.chatter.api.event.discord.ServerShutdownCallback;
import me.axieum.mcmod.chatter.impl.discord.ChatterDiscord;
import me.axieum.mcmod.chatter.impl.discord.util.DiscordDispatcher;
import me.axieum.mcmod.chatter.impl.discord.util.ServerUtils;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarting;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopping;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.crash.CrashReport;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.LOGGER;
import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.getConfig;

public class ServerLifecycleCallback implements ServerStarting, ServerStarted, ServerStopping, ServerShutdownCallback
{
    // Prepare a reusable message formatter for all server lifecycle events
    private static final MessageFormat FORMATTER = new MessageFormat()
            .datetime("datetime", LocalDateTime::now)
            .duration("uptime", () -> Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime()));

    @Override
    public void onServerStarting(MinecraftServer server)
    {
        // Capture a reference to the server instance
        ServerUtils.instance = server;
        // Send Discord notifications
        ChatterDiscord.getClient().ifPresent(jda -> {
            // Update the Discord bot status
            jda.getPresence().setStatus(getConfig().bot.status.starting);
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
            jda.getPresence().setStatus(getConfig().bot.status.started);
            // Dispatch a message to all configured channels
            DiscordDispatcher.embed((embed, entry) -> embed.setColor(Color.GREEN)
                                                           .setDescription(FORMATTER.apply(entry.discord.started)),
                    (entry) -> entry.discord.started != null);
        });
    }

    @Override
    public void onServerStopping(MinecraftServer server)
    {
        // Send Discord notifications
        ChatterDiscord.getClient().ifPresent(jda -> {
            // Update the Discord bot status
            jda.getPresence().setStatus(getConfig().bot.status.stopping);
            // Dispatch a message to all configured channels
            DiscordDispatcher.embed((embed, entry) -> embed.setDescription(FORMATTER.apply(entry.discord.stopping)),
                    (entry) -> entry.discord.stopping != null);
        });
    }

    @Override
    public void onServerShutdown(MinecraftServer server, @Nullable CrashReport crashReport)
    {
        // Send Discord notifications
        ChatterDiscord.getClient().ifPresent(jda -> {
            // Update the Discord bot status
            jda.getPresence().setStatus(getConfig().bot.status.stopped);
            // Determine whether the server stopped unexpectedly
            if (crashReport == null) {
                // Dispatch a normal shutdown message to all configured channels
                DiscordDispatcher.embed((embed, entry) -> embed.setColor(Color.RED)
                                                               .setDescription(FORMATTER.apply(entry.discord.stopped)),
                        (entry) -> entry.discord.stopped != null);
            } else {
                // Fetch the crash report file
                final Optional<File> file = ServerUtils.getCrashReportFile(crashReport);
                // Add any additional message formats
                FORMATTER.tokenize("reason", crashReport.getMessage());
                // Dispatch an unexpected shutdown message to all configured channels
                DiscordDispatcher.embed((embed, entry) -> embed.setColor(Color.RED)
                                                               .setDescription(FORMATTER.apply(entry.discord.crashed)),
                        (action, entry) -> {
                            // Attempt to attach the crash report
                            if (entry.discord.uploadCrashReport)
                                file.ifPresent(action::addFile);
                            action.queue();
                        },
                        (entry) -> entry.discord.crashed != null);
            }
            // Shutdown the JDA client
            LOGGER.info("Wrapping up...");
            jda.shutdown();
        });
    }
}
