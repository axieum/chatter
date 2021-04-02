package me.axieum.mcmod.chatter.impl.discord.util;

import com.vdurmont.emoji.EmojiParser;
import me.axieum.mcmod.chatter.impl.discord.ChatterDiscord;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import net.dv8tion.jda.api.entities.IMentionable;

import java.util.Optional;
import java.util.regex.Pattern;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.CONFIG;

public final class FormatUtils
{
    // Message formatters for transforming between Minecraft and Discord styling syntax
    private static final MessageFormat D_TO_M_FORMAT, M_TO_D_FORMAT;

    static {
        // Discord to Minecraft message formatter
        D_TO_M_FORMAT = new MessageFormat()
                // Translate bold to Minecraft codes
                .regex(Pattern.compile("\\*\\*(.+?)\\*\\*"), g -> "\u00A7l" + g.get(1) + "\u00A7r")
                // Translate underline to Minecraft codes
                .regex(Pattern.compile("__(.+?)__"), g -> "\u00A7n" + g.get(1) + "\u00A7r")
                // Translate italics to Minecraft codes
                .regex(Pattern.compile("_(.+?)_"), g -> "\u00A7o" + g.get(1) + "\u00A7r")
                .regex(Pattern.compile("\\*(.+?)\\*"), g -> "\u00A7o" + g.get(1) + "\u00A7r")
                // Translate strikethrough to Minecraft codes
                .regex(Pattern.compile("~~(.+?)~~"), g -> "\u00A7m" + g.get(1) + "\u00A7r")
                // Obfuscate spoilers
                .regex(Pattern.compile("\\|\\|(.+?)\\|\\|"), g -> "\u00A7k" + g.get(1) + "\u00A7r")
                // Darken code blocks
                .regex(Pattern.compile("```(\\w*)\\n(.*?)\\n?```"),
                        g -> "(" + g.get(1) + ") " + "\u00A77" + g.get(2) + "\u00A7r")
                .regex(Pattern.compile("```(.*?)```"), g -> "\u00A77" + g.get(1) + "\u00A7r")
                .regex(Pattern.compile("`(.*?)`"), g -> "\u00A77" + g.get(1) + "\u00A7r");

        // Minecraft to Discord message formatter
        M_TO_D_FORMAT = new MessageFormat()
                // Remove any escape sequences
                .substitute("\\n", " ") // new line
                // Translate bold to markdown
                .regex(Pattern.compile("(?<=[\u00A7]l)(.+?)(?=\\s?[\u00A7]r|$)"), g -> "**" + g.get(1) + "**")
                // Translate underline to markdown
                .regex(Pattern.compile("(?<=[\u00A7]n)(.+?)(?=\\s?[\u00A7]r|$)"), g -> "__" + g.get(1) + "__")
                // Translate italics to markdown
                .regex(Pattern.compile("(?<=[\u00A7]o)(.+?)(?=\\s?[\u00A7]r|$)"), g -> "_" + g.get(1) + "_")
                // Translate strikethrough to markdown
                .regex(Pattern.compile("(?<=[\u00A7]m)(.+?)(?=\\s?[\u00A7]r|$)"), g -> "~~" + g.get(1) + "~~")
                // Resolve @mention#discriminator
                .regex(Pattern.compile("@(\\w+?)#(\\d{4})"), g ->
                        ChatterDiscord.getClient()
                                      .flatMap(jda -> Optional.ofNullable(jda.getUserByTag(g.get(1), g.get(2))))
                                      .map(IMentionable::getAsMention)
                                      .orElse(g.get(0)))
                // Resolve @mention
                .regex(Pattern.compile("@((?!everyone|here)\\w+)(?!#\\d{4})\\b"), groups ->
                        ChatterDiscord.getClient()
                                      .flatMap(jda -> jda.getGuilds()
                                                         .stream()
                                                         .flatMap(g -> g.getMembersByEffectiveName(groups.get(1), true)
                                                                        .stream())
                                                         .findFirst())
                                      .map(IMentionable::getAsMention)
                                      .orElse(groups.get(0)))
                // Resolve #channel
                .regex(Pattern.compile("#([^\\s]+)"), groups ->
                        ChatterDiscord.getClient()
                                      .flatMap(jda -> jda.getTextChannelsByName(groups.get(1), true)
                                                         .stream()
                                                         .findFirst())
                                      .map(IMentionable::getAsMention)
                                      .orElse(groups.get(0)))
                // Suppress @everyone and @here mentions
                .substitute("@everyone", "@_everyone_")
                .substitute("@here", "@_here_")
                // Strip any left over formatting and return
                .regex(Pattern.compile("(?i)§[0-9A-FK-OR]"), g -> "");
    }

    /**
     * Translate a Minecraft formatted message to Discord formatting.
     *
     * @param message message with colour codes
     * @return message ready for Discord
     */
    public static String minecraftToDiscord(String message)
    {
        return M_TO_D_FORMAT.apply(message);
    }

    /**
     * Translate a Discord formatted message to Minecraft formatting.
     *
     * @param message message with markdown formatting
     * @return message ready for Minecraft
     */
    public static String discordToMinecraft(String message)
    {
        // Apply the initial translation
        final String translation = D_TO_M_FORMAT.apply(message);
        // Conditionally apply emoji translations
        return CONFIG.theme.useUnicodeEmojis ? translation : EmojiParser.parseToAliases(translation);
    }
}
