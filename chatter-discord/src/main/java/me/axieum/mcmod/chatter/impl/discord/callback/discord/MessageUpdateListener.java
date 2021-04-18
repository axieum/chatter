package me.axieum.mcmod.chatter.impl.discord.callback.discord;

import com.github.difflib.text.DiffRow;
import com.github.difflib.text.DiffRowGenerator;
import me.axieum.mcmod.chatter.impl.discord.util.FormatUtils;
import me.axieum.mcmod.chatter.impl.discord.util.MinecraftDispatcher;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageDeleteEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.MessageUpdateEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.LOGGER;
import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.getConfig;

public class MessageUpdateListener extends ListenerAdapter
{
    // A circular mapping of message ids, with a fixed size
    private static final CircularLinkedHashMap<String, Message> MESSAGE_CACHE = new CircularLinkedHashMap<>(32);
    // A text diff generator for message updates
    private static final DiffRowGenerator DIFF_GENERATOR = DiffRowGenerator.create()
                                                                           .showInlineDiffs(true)
                                                                           .mergeOriginalRevised(true)
                                                                           .inlineDiffByWord(true)
                                                                           .oldTag(f -> f ? "\u00A7c~~" : "~~\u00A7r")
                                                                           .newTag(f -> f ? "\u00A7a" : "\u00A7r")
                                                                           .build();

    @Override
    public void onMessageUpdate(@NotNull MessageUpdateEvent event)
    {
        // Only listen to message updates if we have the original cached
        MESSAGE_CACHE.computeIfPresent(event.getMessageId(), (id, context) -> {
            // Compute the textual difference
            final String original = context.getContentDisplay(), message = event.getMessage().getContentDisplay();
            final List<DiffRow> diff = DIFF_GENERATOR.generateDiffRows(singletonList(original), singletonList(message));

            // If, and only if there is a visual difference, forward the event
            if (!diff.isEmpty()) {
                // Prepare a message formatter
                final MessageFormat formatter = new MessageFormat()
                        .datetime("datetime")
                        .tokenize("author", event.getMember() != null ? event.getMember().getEffectiveName()
                                                                      : event.getAuthor().getName())
                        .tokenize("tag", event.getAuthor().getAsTag())
                        .tokenize("username", event.getAuthor().getName())
                        .tokenize("discriminator", event.getAuthor().getDiscriminator())
                        .tokenize("original", () -> FormatUtils.discordToMinecraft(original)) // lazy original
                        .tokenize("message", FormatUtils.discordToMinecraft(message))
                        .tokenize("diff", FormatUtils.discordToMinecraft(diff.get(0).getOldLine()));
                // Dispatch a message to all players
                final long channelId = event.getChannel().getIdLong();
                MinecraftDispatcher.json((entry) -> formatter.apply(entry.minecraft.edit),
                        (entry) -> entry.minecraft.edit != null && entry.id == channelId);
                // Also, send the message to the server console
                LOGGER.info(formatter.apply("@${tag} > ${message}"));
            }

            // Update the message cache
            return event.getMessage();
        });
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        // Update the message cache
        if (!event.getAuthor().isBot() && getConfig().messages.hasChannel(event.getChannel().getIdLong()))
            MESSAGE_CACHE.put(event.getMessageId(), event.getMessage());
    }

    @Override
    public void onMessageDelete(@NotNull MessageDeleteEvent event)
    {
        // Update the message cache
        MESSAGE_CACHE.remove(event.getMessageId());
    }

    /**
     * A circular {@link LinkedHashMap} with a fixed capacity that removes
     * the eldest entries to make room for newer ones.
     *
     * @param <K> the type of keys maintained by this map
     * @param <V> the type of mapped values
     */
    private static class CircularLinkedHashMap<K, V> extends LinkedHashMap<K, V>
    {
        private final int capacity;

        /**
         * Constructs a new Linked Circular Hash Map with a maximum capacity.
         *
         * @param capacity maximum capacity
         */
        private CircularLinkedHashMap(int capacity)
        {
            super(capacity, 1.0f);
            this.capacity = capacity;
        }

        @Override
        protected boolean removeEldestEntry(Map.Entry eldest)
        {
            return size() > capacity;
        }
    }
}
