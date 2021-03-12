package me.axieum.mcmod.chatter.impl.discord.util;

import me.axieum.mcmod.chatter.impl.discord.ChatterDiscord;
import me.axieum.mcmod.chatter.impl.discord.config.DiscordConfig;
import me.axieum.mcmod.chatter.impl.discord.config.module.MessageConfig;
import me.axieum.mcmod.chatter.impl.discord.config.module.MessageConfig.MessageEntry;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.requests.restaction.MessageAction;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.CONFIG;
import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.LOGGER;

public final class DiscordDispatcher
{
    /**
     * Builds and queues embed messages for each configured message entry.
     *
     * @param builder    consumer to modify the Discord embed builder for the current entry before queuing
     * @param predicates predicates used to filter configured message entries
     * @see DiscordDispatcher#embed(BiConsumer, BiConsumer, Predicate...)
     */
    @SafeVarargs
    public static void embed(BiConsumer<EmbedBuilder, MessageEntry> builder, Predicate<MessageEntry>... predicates)
    {
        embed(builder, (a, e) -> a.queue(), predicates);
    }

    /**
     * Builds and acts on embed messages for each configured message entry.
     *
     * @param builder    consumer to modify the Discord embed builder for the current entry before queuing
     * @param action     consumer to act upon the resulting Discord message action before it is queued
     * @param predicates predicates used to filter configured message entries
     * @see DiscordDispatcher#dispatch(BiConsumer, BiConsumer, Predicate...)
     */
    @SafeVarargs
    public static void embed(
            BiConsumer<EmbedBuilder, MessageEntry> builder,
            BiConsumer<MessageAction, MessageEntry> action,
            Predicate<MessageEntry>... predicates
    )
    {
        dispatch((message, entry) -> builder.andThen((m, e) -> message.setEmbed(m.build()))
                                            .accept(new EmbedBuilder(), entry),
                action, predicates);
    }

    /**
     * Builds and queues messages for each configured message entry.
     *
     * @param builder    consumer to modify the Discord message builder for the current entry before queuing
     * @param predicates predicates used to filter configured message entries
     * @see DiscordDispatcher#dispatch(BiConsumer, BiConsumer, Predicate...)
     */
    @SafeVarargs
    public static void dispatch(BiConsumer<MessageBuilder, MessageEntry> builder, Predicate<MessageEntry>... predicates)
    {
        dispatch(builder, (a, e) -> a.queue(), predicates);
    }

    /**
     * Builds and acts on messages for each configured message entry.
     *
     * @param builder    consumer to modify the Discord message builder for the current entry before queuing
     * @param action     consumer to act upon the resulting Discord message action before it is queued
     * @param predicates predicates used to filter configured message entries
     * @see DiscordConfig#messages
     * @see MessageConfig#entries
     */
    @SafeVarargs
    public static void dispatch(
            BiConsumer<MessageBuilder, MessageEntry> builder,
            BiConsumer<MessageAction, MessageEntry> action,
            Predicate<MessageEntry>... predicates
    )
    {
        ChatterDiscord.getClient().ifPresent(jda -> {
            // Prepare a stream of configured message entries
            Stream<MessageEntry> stream = Arrays.stream(CONFIG.messages.entries).parallel();
            // Filter the stream
            if (predicates != null)
                for (Predicate<MessageEntry> predicate : predicates)
                    stream = stream.filter(predicate);
            // Build and queue each message entry
            stream.forEach(entry -> {
                // Fetch the channel
                final TextChannel channel = jda.getTextChannelById(entry.id);
                // Check validity of the channel
                if (channel == null) {
                    LOGGER.warn("Could not find Discord channel with identifier {}", entry.id);
                } else if (!channel.canTalk()) {
                    LOGGER.warn("Missing permissions for Discord channel with identifier {}", entry.id);
                }
                // Build and dispatch the message
                else builder.andThen((m, e) -> action.accept(channel.sendMessage(m.build()), e))
                            .accept(new MessageBuilder(), entry);
            });
        });
    }
}
