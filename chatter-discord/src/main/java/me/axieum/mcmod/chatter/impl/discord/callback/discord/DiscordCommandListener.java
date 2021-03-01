package me.axieum.mcmod.chatter.impl.discord.callback.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.command.CommandListener;
import me.axieum.mcmod.chatter.api.event.discord.MinecraftCommandEvents;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.CONFIG;

public class DiscordCommandListener implements CommandListener
{
    private static final ListenerAdapter MESSAGE_RECEIVED_LISTENER = new MessageReceivedListener();

    @Override
    public void onNonCommandMessage(MessageReceivedEvent event)
    {
        // Forward non-command messages to any message received listeners
        MESSAGE_RECEIVED_LISTENER.onMessageReceived(event);
    }

    /**
     * Attempts to inject a player's avatar into the Minecraft command feedback embed.
     */
    public static class PlayerSkinInjector implements MinecraftCommandEvents.AfterExecute
    {
        // List of compiled regex for commands that mention a player, under capture group "user"
        private static final Pattern[] PATTERNS = new Pattern[]{
                // A player was opped, deopped, banned, kicked or killed
                Pattern.compile("^/?(?:op|deop|ban|kick|kill) (?<user>[^@][^\\s]*)", Pattern.CASE_INSENSITIVE),
                // A player was added/removed from the whitelist
                Pattern.compile("^/?whitelist (?:add|remove) (?<user>[^\\s]+)$", Pattern.CASE_INSENSITIVE),
                // A player was teleported
                Pattern.compile("^/?(?:tp|teleport|give) (?<user>[^\\d@][^\\s]*)", Pattern.CASE_INSENSITIVE),
                // A player was given an item
                Pattern.compile("^/?give (?<user>[^@][^\\s]*)", Pattern.CASE_INSENSITIVE),
                // A player's game mode was changed
                Pattern.compile("^/?gamemode \\w+ (?<user>[^@][^\\s]*)", Pattern.CASE_INSENSITIVE),
        };

        @Override
        public EmbedBuilder afterExecute(CommandEvent e, Command dc, String mc, String r, boolean s, EmbedBuilder embed)
        {
            // We'll only add an avatar if the command was a success
            if (s && embed != null) {
                // Attempt to match the command against a list of known commands
                Arrays.stream(PATTERNS)
                      // Prepare a matcher for the patterns
                      .map(pattern -> pattern.matcher(mc))
                      // Find the first pattern that matches
                      .filter(Matcher::find)
                      .findFirst()
                      // Extract the username from the match
                      .map(matcher -> matcher.group("user"))
                      // If it's found, set the embed's thumbnail to the avatar
                      .filter(username -> !username.isEmpty())
                      .ifPresent(username -> embed.setThumbnail(CONFIG.theme.getAvatarUrl(username, 16)));
            }
            return embed;
        }
    }
}
