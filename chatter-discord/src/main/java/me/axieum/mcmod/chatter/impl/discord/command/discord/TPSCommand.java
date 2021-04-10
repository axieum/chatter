package me.axieum.mcmod.chatter.impl.discord.command.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.axieum.mcmod.chatter.impl.discord.command.DiscordCommands;
import me.axieum.mcmod.chatter.impl.discord.config.module.CommandConfig;
import me.axieum.mcmod.chatter.impl.discord.util.ServerUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Optional;

/**
 * Command to show the Minecraft server's current ticks per second.
 */
public class TPSCommand extends Command
{
    /**
     * Constructs a new TPS command consumer.
     */
    public TPSCommand(CommandConfig.Builtin.TPS config)
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
        if (server != null) {
            // Compute the server's mean ticks per second
            final double meanTPS = ServerUtils.getAverageTPS(server);
            final double meanTPSTime = ServerUtils.getAverageTPSTime(server);
            // Build and send a message embed
            event.reply(new EmbedBuilder()
                    // Set the message
                    .setDescription(String.format("%.2f TPS @ %.3fms", meanTPS, meanTPSTime))
                    // Set the embed colour on a red to green scale (scale down to a 4-step gradient)
                    .setColor(Color.HSBtoRGB(Math.round(meanTPS / 5d) / 4f / 3f, 1f, 1f))
                    .build());
        } else {
            DiscordCommands.replyUnavailable(event);
        }
    }
}
