package me.axieum.mcmod.chatter.impl.discord.callback.discord;

import com.vdurmont.emoji.EmojiParser;
import me.axieum.mcmod.chatter.impl.discord.util.MinecraftDispatcher;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.CONFIG;
import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.LOGGER;

public class MessageReactionListener extends ListenerAdapter
{
    @Override
    public void onGenericMessageReaction(@NotNull GenericMessageReactionEvent event)
    {
        // Ignore the reaction if it was not issued from a guild
        if (!event.isFromGuild() || event.getMember() == null) return;

        // First, retrieve the message context
        event.getTextChannel().retrieveMessageById(event.getMessageId()).queue(context -> {
            // Compute some useful properties of the event
            final boolean added = event instanceof MessageReactionAddEvent;
            final MessageReaction.ReactionEmote reaction = event.getReactionEmote();
            final String emote = reaction.isEmote() ? ":" + reaction.getName() + ":" : reaction.getName();

            // Prepare a message formatter
            final MessageFormat formatter = new MessageFormat()
                    .datetime("datetime")
                    .tokenize("issuer", event.getMember().getEffectiveName())
                    .tokenize("issuer_tag", event.getMember().getUser().getAsTag())
                    .tokenize("author", context.getMember() != null ? context.getMember().getEffectiveName()
                                                                    : context.getAuthor().getName())
                    .tokenize("author_tag", context.getAuthor().getAsTag())
                    .tokenize("emote", CONFIG.theme.useUnicodeEmojis ? emote : EmojiParser.parseToAliases(emote));

            // Dispatch a message to all players
            MinecraftDispatcher.json(entry -> formatter.apply(added ? entry.minecraft.react : entry.minecraft.unreact),
                    entry -> (added ? entry.minecraft.react : entry.minecraft.unreact) != null);

            // Also, send the message to the server console
            if (added)
                LOGGER.info(formatter.apply("@${issuer_tag} reacted with ${emote} to ${author_tag}'s message"));
            else
                LOGGER.info(formatter.apply("@${issuer_tag} removed their reaction of ${emote} from ${author_tag}'s message"));
        });
    }
}
