package me.axieum.mcmod.chatter.impl.discord.util;

import me.axieum.mcmod.chatter.impl.discord.config.DiscordConfig;
import me.axieum.mcmod.chatter.impl.discord.config.module.MessageConfig;
import me.axieum.mcmod.chatter.impl.discord.config.module.MessageConfig.MessageEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.CONFIG;
import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.LOGGER;

public final class MinecraftDispatcher
{
    /**
     * Builds and sends JSON messages for each configured message entry.
     *
     * @param supplier   supplier that provides the text component as JSON to be sent for the current entry
     * @param predicates predicates used to filter configured message entries
     * @see MinecraftDispatcher#dispatch(Function, Predicate...)
     */
    @SafeVarargs
    public static void json(Function<MessageEntry, @NotNull String> supplier, Predicate<MessageEntry>... predicates)
    {
        dispatch(entry -> {
            try {
                return supplier.andThen(Text.Serializer::fromJson).apply(entry);
            } catch (Exception e) {
                LOGGER.error("Unable to parse invalid text JSON: {}", e.getMessage());
                return null;
            }
        }, predicates);
    }

    /**
     * Builds and sends messages for each configured message entry.
     *
     * @param supplier   supplier that provides the text component to be sent for the current entry
     * @param predicates predicates used to filter configured message entries
     * @see DiscordConfig#messages
     * @see MessageConfig#entries
     */
    @SafeVarargs
    public static void dispatch(Function<MessageEntry, @Nullable Text> supplier, Predicate<MessageEntry>... predicates)
    {
        if (!ServerUtils.isReady()) return;
        ServerUtils.getInstance().ifPresent(server -> {
            // Prepare a stream of configured message entries
            Stream<MessageEntry> stream = Arrays.stream(CONFIG.messages.entries).parallel();
            // Filter the stream
            if (predicates != null)
                for (Predicate<MessageEntry> predicate : predicates)
                    stream = stream.filter(predicate);
            // Send each message entry
            stream.forEach(entry -> {
                // Supply and dispatch the message
                final Text text = supplier.apply(entry);
                if (text != null) {
                    // Fetch all players
                    Stream<ServerPlayerEntity> players = server.getPlayerManager().getPlayerList().stream();
                    // Conditionally filter players to the in-scope dimensions
                    if (entry.dimensions != null && entry.dimensions.length > 0) {
                        final List<String> dims = Arrays.asList(entry.dimensions);
                        players = players.filter(player -> dims.contains(StringUtils.getWorldId(player.world)));
                    }
                    // Send the message to all relevant players
                    players.forEach(player -> player.sendMessage(text, false));
                }
            });
        });
    }
}
