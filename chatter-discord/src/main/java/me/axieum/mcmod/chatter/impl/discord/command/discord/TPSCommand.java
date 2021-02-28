package me.axieum.mcmod.chatter.impl.discord.command.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import me.axieum.mcmod.chatter.impl.discord.command.DiscordCommands;
import me.axieum.mcmod.chatter.impl.discord.util.ServerUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.CONFIG;

/**
 * Command to show the Minecraft server's current ticks per second.
 */
public class TPSCommand extends Command
{
    /**
     * Constructs a new TPS command consumer.
     */
    public TPSCommand()
    {
        this.name = CONFIG.commands.builtin.tps.name;
        this.aliases = CONFIG.commands.builtin.tps.aliases;
        this.arguments = CONFIG.commands.builtin.tps.usage;
        this.help = CONFIG.commands.builtin.tps.help;
        this.hidden = CONFIG.commands.builtin.tps.hidden;
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
