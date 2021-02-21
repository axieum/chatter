package me.axieum.mcmod.chatter.impl.discord.command.discord;

import com.jagrosh.jdautilities.command.CommandClient;
import com.jagrosh.jdautilities.command.CommandClientBuilder;
import me.axieum.mcmod.chatter.api.event.discord.BuildCommandClientCallback;
import me.axieum.mcmod.chatter.impl.discord.callback.discord.DiscordCommandListener;

import java.util.Arrays;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.CONFIG;
import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.LOGGER;

public final class DiscordCommands
{
    /**
     * Builds a JDA command client instance.
     *
     * @return JDA command client instance
     */
    public static CommandClient build()
    {
        try {
            final CommandClientBuilder builder = new CommandClientBuilder();

            // General command configurations
            builder.setPrefix(CONFIG.commands.prefix)
                   .setHelpWord(CONFIG.commands.helpWord)
                   .useHelpBuilder(CONFIG.commands.helpWord != null)
                   .setStatus(CONFIG.bot.status.starting)
                   .setActivity(null);

            // Conditionally apply owner identifiers
            final String[] ownerIds = CONFIG.commands.admins;
            if (ownerIds.length > 0) {
                // Assume the first identifier is the primary owner
                builder.setOwnerId(ownerIds[0]);
                // And the rest are co-owners
                if (ownerIds.length > 1)
                    builder.setCoOwnerIds(Arrays.copyOfRange(ownerIds, 1, ownerIds.length));
            } else {
                throw new IllegalArgumentException("At least one command admin must be configured!");
            }

            // Set the command listener
            builder.setListener(new DiscordCommandListener());

            // Build the client
            BuildCommandClientCallback.EVENT.invoker().onBuild(builder);
            return builder.build();
        } catch (Exception e) {
            LOGGER.warn("Unable to prepare the command client: {}", e.getMessage());
            return null;
        }
    }
}
