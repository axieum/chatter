package me.axieum.mcmod.chatter.impl.discord.callback.discord;

import me.axieum.mcmod.chatter.impl.discord.util.MinecraftDispatcher;
import me.axieum.mcmod.chatter.impl.discord.util.StringUtils;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.LOGGER;

public class MessageReceivedListener extends ListenerAdapter
{
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event)
    {
        // Ignore the message if the author is a bot
        if (event.getAuthor().isBot()) return;

        // Capture common message details
        final String author = event.getMember() != null ? event.getMember().getEffectiveName()
                                                        : event.getAuthor().getName();
        final String tag = event.getAuthor().getAsTag();

        // Push any message content
        if (!event.getMessage().getContentRaw().isEmpty()) {
            // Prepare a message formatter
            final MessageFormat formatter = new MessageFormat()
                    .datetime("datetime")
                    .tokenize("author", author)
                    .tokenize("tag", tag)
                    .tokenize("message", StringUtils.discordToMinecraft(event.getMessage().getContentDisplay()));
            // Dispatch a message to all players
            MinecraftDispatcher.json((entry) -> formatter.apply(entry.minecraft.chat),
                    (entry) -> entry.minecraft.chat != null);
            // Also, send the message to the server console
            LOGGER.info(formatter.apply("@${tag} > ${message}"));
        }

        // Link any attachments
        for (Message.Attachment attachment : event.getMessage().getAttachments()) {
            // Prepare an attachment message formatter
            final MessageFormat formatter = new MessageFormat()
                    .datetime("datetime")
                    .tokenize("author", author)
                    .tokenize("tag", tag)
                    .tokenize("url", attachment.getUrl())
                    .tokenize("name", attachment.getFileName())
                    .tokenize("ext", attachment.getFileExtension())
                    .tokenize("size", StringUtils.bytesToHuman(attachment.getSize()));
            // Dispatch a message to all players
            MinecraftDispatcher.json((entry) -> formatter.apply(entry.minecraft.attachment),
                    (entry) -> entry.minecraft.attachment != null);
            // Also, send the message to the server console
            LOGGER.info(formatter.apply("@${tag} attached ${name} (${size})"));
        }
    }
}