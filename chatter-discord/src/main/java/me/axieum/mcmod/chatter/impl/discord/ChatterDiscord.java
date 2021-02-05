package me.axieum.mcmod.chatter.impl.discord;

import me.axieum.mcmod.chatter.api.event.discord.BuildJDACallback;
import me.axieum.mcmod.chatter.impl.discord.callback.discord.DiscordLifecycleListener;
import me.axieum.mcmod.chatter.impl.discord.callback.minecraft.ServerLifecycleCallback;
import me.axieum.mcmod.chatter.impl.discord.config.DiscordConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.security.auth.login.LoginException;
import java.util.Optional;

public class ChatterDiscord implements DedicatedServerModInitializer, PreLaunchEntrypoint
{
    public static final Logger LOGGER = LogManager.getLogger("Chatter|Discord");
    public static final DiscordConfig CONFIG = DiscordConfig.init();
    private static JDA client = null;

    @Override
    public void onPreLaunch()
    {
        try {
            LOGGER.info("Getting ready...");
            final JDABuilder builder = JDABuilder.createDefault(CONFIG.bot.token)
                                                 // Update the bot status
                                                 .setStatus(CONFIG.bot.status.starting)
                                                 // Register listeners
                                                 .addEventListeners(new DiscordLifecycleListener());
            BuildJDACallback.EVENT.invoker().onBuild(builder);

            LOGGER.info("Logging into Discord...");
            client = builder.build();
        } catch (LoginException | IllegalArgumentException e) {
            LOGGER.error("Unable to login to Discord: {}", e.getMessage());
        }
    }

    @Override
    public void onInitializeServer()
    {
        // Register Minecraft callbacks
        ServerLifecycleEvents.SERVER_STARTING.register(new ServerLifecycleCallback());
        ServerLifecycleEvents.SERVER_STARTED.register(new ServerLifecycleCallback());
        ServerLifecycleEvents.SERVER_STOPPING.register(new ServerLifecycleCallback());
        ServerLifecycleEvents.SERVER_STOPPED.register(new ServerLifecycleCallback());
    }

    /**
     * Returns the underlying JDA API client.
     *
     * @return JDA client if built
     */
    public static Optional<JDA> getClient()
    {
        return Optional.ofNullable(client);
    }
}
