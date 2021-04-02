package me.axieum.mcmod.chatter.impl.discord.callback.minecraft;

import me.axieum.mcmod.chatter.api.event.chat.ChatEvents;
import me.axieum.mcmod.chatter.impl.discord.ChatterDiscord;
import me.axieum.mcmod.chatter.impl.discord.config.module.MessageConfig.DimensionPredicate;
import me.axieum.mcmod.chatter.impl.discord.util.DiscordDispatcher;
import me.axieum.mcmod.chatter.impl.discord.util.FormatUtils;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import me.axieum.mcmod.chatter.impl.util.StringUtils;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public class ReceiveChatCallback implements ChatEvents.ReceiveChat
{
    @Override
    public @Nullable Text onReceiveMessage(ServerPlayerEntity player, String raw, Text text)
    {
        // Send Discord notifications
        ChatterDiscord.getClient().ifPresent(jda -> {
            // Prepare a message formatter
            final MessageFormat formatter = new MessageFormat()
                    .datetime("datetime")
                    .tokenize("player", player.getDisplayName().getString())
                    .tokenize("message", FormatUtils.minecraftToDiscord(raw))
                    .tokenize("world", StringUtils.getWorldName(player.world));

            // Dispatch a message to all configured channels
            DiscordDispatcher.dispatch((message, entry) -> message.append(formatter.apply(entry.discord.chat)),
                    (entry) -> entry.discord.chat != null,
                    new DimensionPredicate(StringUtils.getWorldId(player.world)));
        });

        return text;
    }
}
