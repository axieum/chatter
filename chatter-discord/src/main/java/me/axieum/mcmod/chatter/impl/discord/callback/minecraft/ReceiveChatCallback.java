package me.axieum.mcmod.chatter.impl.discord.callback.minecraft;

import me.axieum.mcmod.chatter.api.event.chat.ChatEvents;
import me.axieum.mcmod.chatter.impl.discord.ChatterDiscord;
import me.axieum.mcmod.chatter.impl.discord.util.DiscordDispatcher;
import me.axieum.mcmod.chatter.impl.discord.util.ServerUtils;
import me.axieum.mcmod.chatter.impl.discord.util.StringUtils;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
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
                    .tokenize("message", StringUtils.minecraftToDiscord(raw))
                    .tokenize("world", () -> StringUtils.getWorldName(player.world)); // lazy world name
            // Dispatch a message to all configured channels
            DiscordDispatcher.dispatch((message, entry) -> message.append(formatter.apply(entry.discord.chat)),
                    (entry) -> entry.discord.chat != null);
        });

        return text;
    }
}