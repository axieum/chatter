package me.axieum.mcmod.chatter.impl.discord.callback.discord;

import me.axieum.mcmod.chatter.impl.discord.config.module.ThemeConfig;
import me.axieum.mcmod.chatter.impl.discord.util.ServerUtils;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.NotNull;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.CONFIG;
import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.LOGGER;

public class DiscordPresenceListener extends ListenerAdapter
{
    // A message formatter for the presence values
    public static final MessageFormat FORMATTER = new MessageFormat();
    // An instance of random to aid in choosing presences
    private static final Random RANDOM = new Random();
    // Timer that is responsible for updating the Discord bot presence
    private static Timer timer;

    static {
        FORMATTER
                // Minecraft server version, e.g. 1.16.5
                .tokenize("version", () -> ServerUtils.getInstance().map(MinecraftServer::getVersion).orElse("unknown"))
                // Server's ip address, e.g. 192.168.1.90
                .tokenize("ip", () -> ServerUtils.getInstance().map(MinecraftServer::getServerIp).orElse(""))
                // Server's port, e.g. 25565
                .tokenize("port", () -> String.valueOf(ServerUtils.getInstance()
                                                                  .map(MinecraftServer::getServerPort)
                                                                  .orElse(25565)))
                // Message of the day (MOTD), e.g. A Minecraft Server
                .tokenize("motd", () -> ServerUtils.getInstance().map(MinecraftServer::getServerMotd).orElse(""))
                // Server world difficulty, e.g. normal
                .tokenize("difficulty", () -> ServerUtils.getInstance()
                                                         .map(s -> s.getSaveProperties().getDifficulty().getName())
                                                         .orElse("easy"))
                // Max player count
                .tokenize("max_players", () -> String.valueOf(ServerUtils.getInstance()
                                                                         .map(MinecraftServer::getMaxPlayerCount)
                                                                         .orElse(0)))
                // Current player count
                .tokenize("player_count", () -> String.valueOf(ServerUtils.getInstance()
                                                                          .map(MinecraftServer::getCurrentPlayerCount)
                                                                          .orElse(0)))
                // Average ticks per second (TPS), e.g. 20.00
                .tokenize("tps", () -> String.format("%.2f", ServerUtils.getInstance()
                                                                        .map(ServerUtils::getAverageTPS)
                                                                        .orElse(0d)))
                // Average ticks per second (TPS) timing, e.g. 12.513
                .tokenize("tps_time", () -> String.format("%.3f", ServerUtils.getInstance()
                                                                             .map(ServerUtils::getAverageTPSTime)
                                                                             .orElse(0d)))
                // Server's current uptime
                .duration("uptime", () -> Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime()))
                // Current date & time
                .datetime("datetime");
    }

    @Override
    public void onReady(@NotNull ReadyEvent event)
    {
        // Skip presence scheduling if there are none configured yet
        if (CONFIG.theme.presence.entries.length == 0) return;

        // Prepare a new timer that we can schedule tasks on
        if (timer != null) timer.cancel();
        timer = new Timer("Chatter-Discord-Presence-Timer", true);

        // Check how long the update interval should be, and apply reasonable bounds
        final long interval = Math.max(CONFIG.theme.presence.interval, 15);

        // Schedule the presence update task
        LOGGER.info("Scheduling bot presence updates for every {} second(s)", interval);
        timer.schedule(new PresenceUpdateTask(event.getJDA()), 0, interval * 1000L);
    }

    @Override
    public void onShutdown(@NotNull ShutdownEvent event)
    {
        // Clear the timer
        if (timer == null) return;
        timer.cancel();
        timer = null;

        // Reset the presence
        event.getJDA().getPresence().setActivity(null);
    }

    /**
     * Timer task that rotates through Discord bot presences.
     */
    private static class PresenceUpdateTask extends TimerTask
    {
        private final JDA jda;
        private int next = 0;

        public PresenceUpdateTask(JDA jda)
        {
            this.jda = jda;
        }

        @Override
        public void run()
        {
            try {
                // Attempt to create a new activity from the configured presence entry and set it
                final ThemeConfig.Presence.PresenceEntry cfg = CONFIG.theme.presence.entries[next];
                jda.getPresence().setActivity(Activity.of(cfg.type, FORMATTER.apply(cfg.value), cfg.url));
            } catch (Exception e) {
                // The configured presence entry is invalid!
                LOGGER.warn("Unable to update the Discord bot presence to entry {}: {}", next, e.getMessage());
            } finally {
                // Choose the next presence index
                if (CONFIG.theme.presence.random)
                    next = RANDOM.nextInt(CONFIG.theme.presence.entries.length);
                else
                    next = (next + 1) % CONFIG.theme.presence.entries.length;
            }
        }
    }
}
