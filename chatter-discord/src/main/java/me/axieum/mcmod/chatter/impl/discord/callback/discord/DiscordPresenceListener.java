package me.axieum.mcmod.chatter.impl.discord.callback.discord;

import me.axieum.mcmod.chatter.impl.discord.config.module.ThemeConfig;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.ShutdownEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.CONFIG;
import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.LOGGER;

public class DiscordPresenceListener extends ListenerAdapter
{
    // An instance of random to aid in choosing presences
    private static final Random RANDOM = new Random();
    // Timer that is responsible for updating the Discord bot presence
    private static Timer timer;

    @Override
    public void onReady(@NotNull ReadyEvent event)
    {
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
        if (timer != null) timer.cancel();
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
                jda.getPresence().setActivity(Activity.of(cfg.type, cfg.value, cfg.url));
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
