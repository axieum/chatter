package me.axieum.mcmod.chatter.impl.discord.command.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.axieum.mcmod.chatter.impl.discord.command.DiscordCommands;
import me.axieum.mcmod.chatter.impl.discord.config.module.CommandConfig;
import me.axieum.mcmod.chatter.impl.discord.util.ServerUtils;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.util.Optional;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.getConfig;

/**
 * Command to show how long the Minecraft server has been online for.
 */
public class UptimeCommand extends Command
{
    // Prepare a reusable message formatter for all uptime commands
    private static final MessageFormat FORMATTER = new MessageFormat()
            .duration("uptime", () -> Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime()));

    /**
     * Constructs a new Uptime command consumer.
     */
    public UptimeCommand(CommandConfig.Builtin.Uptime config)
    {
        this.name = config.name;
        this.aliases = config.aliases;
        this.arguments = config.usage;
        this.help = config.help;
        this.hidden = config.hidden;
        this.requiredRole = config.role;
        this.cooldown = config.cooldown;
        this.cooldownScope = Optional.ofNullable(config.cooldownScope).orElse(CooldownScope.USER);
    }

    @Override
    protected void execute(CommandEvent event)
    {
        // Retrieve the Minecraft server if it is present and ready
        // NB: Update to Java 9's `Optional#ifPresentOrElse` when Java 8 support is dropped
        final @Nullable MinecraftServer server = ServerUtils.getInstance().filter(ServerUtils::isReady).orElse(null);

        // If the server is present and ready, handle the command
        if (server != null)
            event.reply(new EmbedBuilder().setDescription(FORMATTER.apply(getConfig().commands.builtin.uptime.message))
                                          .build());
        else
            DiscordCommands.replyUnavailable(event);
    }
}
