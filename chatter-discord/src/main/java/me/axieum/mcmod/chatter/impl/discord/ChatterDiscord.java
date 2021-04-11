package me.axieum.mcmod.chatter.impl.discord;

import com.jagrosh.jdautilities.command.CommandClient;
import me.axieum.mcmod.chatter.api.event.chat.ChatEvents;
import me.axieum.mcmod.chatter.api.event.discord.JDAEvents;
import me.axieum.mcmod.chatter.api.event.discord.MinecraftCommandEvents;
import me.axieum.mcmod.chatter.api.event.discord.ServerShutdownCallback;
import me.axieum.mcmod.chatter.api.event.player.PlayerEvents;
import me.axieum.mcmod.chatter.api.event.world.EntityDeathMessageCallback;
import me.axieum.mcmod.chatter.impl.discord.callback.discord.*;
import me.axieum.mcmod.chatter.impl.discord.callback.minecraft.*;
import me.axieum.mcmod.chatter.impl.discord.command.DiscordCommands;
import me.axieum.mcmod.chatter.impl.discord.config.DiscordConfig;
import me.shedaniel.autoconfig.ConfigHolder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import javax.security.auth.login.LoginException;
import java.util.Optional;

public class ChatterDiscord implements DedicatedServerModInitializer, PreLaunchEntrypoint
{
    public static final Logger LOGGER = LogManager.getLogger("Chatter|Discord");
    public static final ConfigHolder<DiscordConfig> CONFIG = DiscordConfig.init();
    public static @Nullable JDA client = null;
    public static @Nullable CommandClient commands = null;

    @Override
    public void onPreLaunch()
    {
        // Check if the module has been configured properly
        if (getConfig().bot.token == null || getConfig().bot.token.isEmpty()) return;

        LOGGER.info("Registered Chatter add-on 'Chatter Discord' - Bring your Minecraft world into your Discord guild");

        try {
            // Prepare the JDA client
            final JDABuilder builder = JDABuilder.createDefault(getConfig().bot.token)
                                                 // Update the bot status
                                                 .setStatus(getConfig().bot.status.starting)
                                                 // Register listeners
                                                 .addEventListeners(
                                                         new DiscordLifecycleListener(),
                                                         new DiscordPresenceListener()
                                                 );

            // Conditionally enable member caching
            if (getConfig().bot.cacheMembers)
                builder.enableIntents(GatewayIntent.GUILD_MEMBERS) // enable required intents
                       .setMemberCachePolicy(MemberCachePolicy.ALL) // cache all members
                       .setChunkingFilter(ChunkingFilter.ALL); // eager-load all members

            // Conditionally prepare and build the command client
            if (getConfig().commands.enabled)
                if ((commands = DiscordCommands.build(getConfig())) != null)
                    builder.addEventListeners(commands);

            // Build and login to the client
            JDAEvents.BUILD_CLIENT.invoker().onBuildClient(builder);
            LOGGER.info("Logging into Discord...");
            client = builder.build();
        } catch (LoginException | IllegalArgumentException e) {
            if (commands != null) { commands.shutdown(); commands = null; }
            LOGGER.error("Unable to login to Discord: {}", e.getMessage());
        }
    }

    @Override
    public void onInitializeServer()
    {
        // Check if the module has been configured properly
        if (client == null) return;

        // Register server lifecycle callbacks
        ServerLifecycleEvents.SERVER_STARTING.register(new ServerLifecycleCallback());
        ServerLifecycleEvents.SERVER_STARTED.register(new ServerLifecycleCallback());
        ServerLifecycleEvents.SERVER_STOPPING.register(new ServerLifecycleCallback());
        ServerShutdownCallback.EVENT.register(new ServerLifecycleCallback());
        // Register player callbacks
        ServerPlayConnectionEvents.JOIN.register(new PlayerConnectionCallback());
        ServerPlayConnectionEvents.DISCONNECT.register(new PlayerConnectionCallback());
        ChatEvents.RECEIVE_CHAT.register(new ReceiveChatCallback());
        ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(new PlayerChangeWorldCallback());
        PlayerEvents.DEATH.register(new PlayerDeathCallback());
        PlayerEvents.GRANT_CRITERION.register(new PlayerAdvancementCallback());
        // Conditionally register module dependant callbacks
        if (FabricLoader.getInstance().isModLoaded("chatter-world"))
            EntityDeathMessageCallback.EVENT.register(new EntityDeathCallback());
        // Register Discord listeners
        getClient().ifPresent(jda -> {
            jda.addEventListener(
                    new MessageUpdateListener(),
                    new MessageReactionListener()
            );
            // NB: Received messages are handled by the command client (on non-commands)
            //     Hence, if the command client was not setup, register here instead
            if (commands == null) jda.addEventListener(new MessageReceivedListener());
        });
        MinecraftCommandEvents.AFTER_EXECUTE.register(new DiscordCommandListener.PlayerSkinInjector());
    }

    /**
     * Returns the config instance.
     *
     * @return config instance
     * @see ConfigHolder#getConfig()
     */
    public static DiscordConfig getConfig()
    {
        return CONFIG.getConfig();
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

    /**
     * Returns the underlying JDA command client.
     *
     * @return JDA command client if built
     */
    public static Optional<CommandClient> getCommandClient()
    {
        return Optional.ofNullable(commands);
    }
}
