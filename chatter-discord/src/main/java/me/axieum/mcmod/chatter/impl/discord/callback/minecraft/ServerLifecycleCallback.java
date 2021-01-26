package me.axieum.mcmod.chatter.impl.discord.callback.minecraft;

import me.axieum.mcmod.chatter.impl.discord.ChatterDiscord;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopped;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopping;
import net.minecraft.server.MinecraftServer;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.CONFIG;
import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.LOGGER;

public class ServerLifecycleCallback implements ServerStarted, ServerStopping, ServerStopped
{
    @Override
    public void onServerStarted(MinecraftServer server)
    {
        // Update the Discord bot status if available
        ChatterDiscord.getClient().ifPresent(jda -> jda.getPresence().setStatus(CONFIG.bot.status.started));
    }

    @Override
    public void onServerStopping(MinecraftServer minecraftServer)
    {
        // Update the Discord bot status if available
        ChatterDiscord.getClient().ifPresent(jda -> jda.getPresence().setStatus(CONFIG.bot.status.stopping));
    }

    @Override
    public void onServerStopped(MinecraftServer minecraftServer)
    {
        // Update the Discord bot status, and shutdown JDA if available
        ChatterDiscord.getClient().ifPresent(jda -> {
            LOGGER.info("Logging out of Discord...");
            jda.getPresence().setStatus(CONFIG.bot.status.stopped);
            jda.shutdown();
        });
    }
}
