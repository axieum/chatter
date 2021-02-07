package me.axieum.mcmod.chatter.impl.discord;

import me.axieum.mcmod.chatter.api.event.chat.ChatEvents;
import me.axieum.mcmod.chatter.api.event.discord.BuildJDACallback;
import me.axieum.mcmod.chatter.api.event.player.PlayerEvents;
import me.axieum.mcmod.chatter.impl.discord.callback.discord.DiscordLifecycleListener;
import me.axieum.mcmod.chatter.impl.discord.callback.minecraft.*;
import me.axieum.mcmod.chatter.impl.discord.config.DiscordConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
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
            // Prepare the JDA client
            LOGGER.info("Getting ready...");
            final JDABuilder builder = JDABuilder.createDefault(CONFIG.bot.token)
                                                 // Update the bot status
                                                 .setStatus(CONFIG.bot.status.starting)
                                                 // Register listeners
                                                 .addEventListeners(new DiscordLifecycleListener());

            // Conditionally enable member caching
            if (CONFIG.bot.cacheMembers)
                builder.enableIntents(GatewayIntent.GUILD_MEMBERS) // enable required intents
                       .setMemberCachePolicy(MemberCachePolicy.ALL) // cache all members
                       .setChunkingFilter(ChunkingFilter.ALL); // eager-load all members

            // Build and login to the client
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
        // Register server lifecycle callbacks
        ServerLifecycleEvents.SERVER_STARTING.register(new ServerLifecycleCallback());
        ServerLifecycleEvents.SERVER_STARTED.register(new ServerLifecycleCallback());
        ServerLifecycleEvents.SERVER_STOPPING.register(new ServerLifecycleCallback());
        ServerLifecycleEvents.SERVER_STOPPED.register(new ServerLifecycleCallback());
        // Register player callbacks
        ServerPlayConnectionEvents.JOIN.register(new PlayerConnectionCallback());
        ServerPlayConnectionEvents.DISCONNECT.register(new PlayerConnectionCallback());
        ChatEvents.RECEIVE_CHAT.register(new ReceiveChatCallback());
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(new PlayerChangeWorldCallback());
        PlayerEvents.DEATH.register(new PlayerDeathCallback());
        PlayerEvents.GRANT_CRITERION.register(new PlayerAdvancementCallback());
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
