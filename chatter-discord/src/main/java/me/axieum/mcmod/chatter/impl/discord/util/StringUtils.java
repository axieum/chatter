package me.axieum.mcmod.chatter.impl.discord.util;

import com.vdurmont.emoji.EmojiParser;
import me.axieum.mcmod.chatter.impl.discord.ChatterDiscord;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import net.dv8tion.jda.api.entities.IMentionable;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;
import java.util.Optional;
import java.util.regex.Pattern;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.CONFIG;

public final class StringUtils
{
    // Message formatters for transforming between Minecraft and Discord styling syntax
    private static final MessageFormat D_TO_M_FORMAT, M_TO_D_FORMAT;
    // Mapping of Minecraft world identifiers to their human-readable names
    public static final HashMap<Identifier, String> WORLD_NAMES = new HashMap<>(3);

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

    /**
     * Returns the identifier value of the given world.
     *
     * @param world Minecraft world/dimension
     * @return identifier of the given world, e.g. minecraft:overworld
     */
    public static String getWorldId(World world)
    {
        return world.getRegistryKey().getValue().toString();
    }

    /**
     * Computes, caches and returns the name of the given world.
     *
     * @param world Minecraft world/dimension
     * @return name of the given world
     */
    public static String getWorldName(World world)
    {
        // Attempt to retrieve the name from cache, otherwise compute it once!
        // NB: At present, the world name is not stored in any resources, apart
        // from in the identifier (e.g. 'the_nether'), good enough but hacky :(
        return WORLD_NAMES.computeIfAbsent(world.getRegistryKey().getValue(), identifier -> {
            // Space delimited identifier path, with leading 'the' keywords removed
            final String path = identifier.getPath().replace('_', ' ').replaceFirst("(?i)the\\s", "");
            // Capitalise the first character in each word
            char[] chars = path.toCharArray();
            boolean capitalizeNext = true;
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == ' ') {
                    capitalizeNext = true;
                } else if (capitalizeNext) {
                    chars[i] = Character.toTitleCase(chars[i]);
                    capitalizeNext = false;
                }
            }
            // Return the computed world name
            return new String(chars);
        });
    }

    /**
     * Converts bytes to a human readable string, base 10.
     *
     * @param bytes number of bytes
     * @return human readable bytes in base 10
     */
    public static String bytesToHuman(long bytes)
    {
        if (-1000 < bytes && bytes < 1000) return bytes + " B";
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }
}
