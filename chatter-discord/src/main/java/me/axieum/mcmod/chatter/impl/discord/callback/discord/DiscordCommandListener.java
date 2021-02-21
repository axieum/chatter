package me.axieum.mcmod.chatter.impl.discord.callback.discord;

import com.jagrosh.jdautilities.command.CommandListener;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class DiscordCommandListener implements CommandListener
{
    private static final ListenerAdapter MESSAGE_RECEIVED_LISTENER = new MessageReceivedListener();

    @Override
    public void onNonCommandMessage(MessageReceivedEvent event)
    {
        // Forward non-command messages to any message received listeners
        MESSAGE_RECEIVED_LISTENER.onMessageReceived(event);
    }
}
