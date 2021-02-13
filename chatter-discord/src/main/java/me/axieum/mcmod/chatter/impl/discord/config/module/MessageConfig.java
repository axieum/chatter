package me.axieum.mcmod.chatter.impl.discord.config.module;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry.Category;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

import java.util.Arrays;
import java.util.function.Predicate;

@Config(name = "messages")
public class MessageConfig implements ConfigData
{
    @Comment("Messages")
    public MessageEntry[] entries = {new MessageEntry()};

    public static class MessageEntry
    {
        @Comment("Identifier for a channel within Discord to observe")
        public long id;

        @Comment("If defined, reduces the scope of all events to the listed Minecraft dimension IDs (empty for all)")
        public String[] dimensions = {};

        @Category("Discord")
        @Comment("Minecraft events relayed to Discord")
        public Discord discord = new Discord();

        public static class Discord
        {
            @Comment("A player sent an in-game message\n" +
                    "Usages: ${player}, ${message}, ${world}, ${datetime[:format]}")
            public String chat = "`${world}` **${player}** > ${message}";

            @Comment("A player died\n" +
                    "Usages: ${player}, ${cause}, ${world}, ${x}, ${y}, ${z}, " +
                    "${score}, ${exp}, ${lifespan[:format]}, ${datetime[:format]}")
            public String death = "**${player}** ${cause}!\n:skull: _${world} | ${x}, ${y}, ${z}_";

            @Comment("A player unlocked an advancement\n" +
                    "Usages: ${player}, ${type}, ${title}, ${description}, ${datetime[:format]}")
            public String advancement = "**${player}** completed the ${type} **${title}**! :clap:\n_${description}_";

            @Comment("A player teleported to another dimension\n" +
                    "Usages: ${player}, ${origin}, ${destination}, ${datetime[:format]}")
            public String teleport = "**${player}** entered ${destination}. :cyclone:";

            @Comment("A player joined the game\n" +
                    "Usages: ${player}, ${world}, ${datetime[:format]}")
            public String join = "**${player}** joined!";

            @Comment("A player left the game\n" +
                    "Usages: ${player}, ${world}, ${elapsed[:format]}, ${datetime[:format]}")
            public String leave = "**${player}** left!";

            @Comment("The server began to start\n" +
                    "Usages: ${datetime[:format]}")
            public String starting = "Server is starting... :fingers_crossed:";

            @Comment("The server started and is accepting connections\n" +
                    "Usages: ${uptime[:format]}, ${datetime[:format]}")
            public String started = "Server started (took ${uptime:s.SSS}s) :ok_hand:";

            @Comment("The server began to stop\n" +
                    "Usages: ${uptime[:format]}, ${datetime[:format]}")
            public String stopping = "Server is stopping... :raised_hand:";

            @Comment("The server stopped and is offline\n" +
                    "Usages: ${uptime[:format]}, ${datetime[:format]}")
            public String stopped = "Server stopped! :no_entry:";

            @Comment("The server stopped unexpectedly and is inaccessible\n" +
                    "Usages: ${reason}, ${uptime[:format]}, ${datetime[:format]}")
            public String crashed = "Server crash detected! :warning:\n_${reason}_";

            @Comment("True if a crash report is attached to the server crashed message")
            public boolean uploadCrashReport = false;
        }

        @Category("Minecraft")
        @Comment("Discord events relayed to Minecraft")
        public Minecraft minecraft = new Minecraft();

        public static class Minecraft
        {
            @Comment("A user sent a message\n" +
                    "NB: Use JSON style, check out https://minecraftjson.com/ (third party)\n" +
                    "Usages: ${author}, ${tag}, ${message}, ${datetime[:format]}")
            public String chat = "[\"\",{\"text\":\"${author}\",\"color\":\"#00aaff\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"@${tag} \"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[\"\",{\"text\":\"Sent from Discord\",\"italic\":true}]}},{\"text\":\" > \",\"color\":\"dark_gray\"},{\"text\":\"${message}\"}]";

            @Comment("A user edited their recently sent message")
            public String edit;

            @Comment("A user reacted to a recent message\n" +
                    "Usages: ${issuer}, ${issuer_tag}, ${author}, ${author_tag}, ${emote}, ${datetime[:format]}")
            public String react = "[\"\",{\"text\":\"${issuer}\",\"color\":\"#00aaff\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"@${issuer_tag} \"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[\"\",{\"text\":\"Sent from Discord\",\"italic\":true}]}},{\"text\":\" reacted with \"},{\"text\":\"${emote}\",\"color\":\"green\"},{\"text\": \" to \"},{\"text\":\"${author}\",\"color\":\"#00aaff\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"@${author_tag} \"}},{\"text\":\"'s message\"}]";

            @Comment("A user removed their reaction from a recent message\n" +
                    "Usages: ${issuer}, ${issuer_tag}, ${author}, ${author_tag}, ${emote}, ${datetime[:format]}")
            public String unreact = "[\"\",{\"text\":\"${issuer}\",\"color\":\"#00aaff\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"@${issuer_tag} \"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[\"\",{\"text\":\"Sent from Discord\",\"italic\":true}]}},{\"text\":\" removed their reaction of \"},{\"text\":\"${emote}\",\"color\":\"red\"},{\"text\": \" from \"},{\"text\":\"${author}\",\"color\":\"#00aaff\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"@${author_tag} \"}},{\"text\":\"'s message\"}]";

            @Comment("A user sent a message that contained attachments\n" +
                    "Usages: ${author}, ${tag}, ${url}, ${name}, ${ext}, ${size}, ${datetime[:format]}")
            public String attachment = "[\"\",{\"text\":\"${author}\",\"color\":\"#00aaff\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"@${tag} \"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[\"\",{\"text\":\"Sent from Discord\",\"italic\":true}]}},{\"text\":\" > \",\"color\":\"dark_gray\"},{\"text\":\"${name}\",\"color\":\"blue\",\"underlined\":true,\"clickEvent\":{\"action\":\"open_url\",\"value\":\"${url}\"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":{\"text\":\"${ext} (${size})\"}}}]";
        }
    }

    /**
     * Predicate for testing whether a dimension identifier is within scope of a message entry.
     */
    public static class DimensionPredicate implements Predicate<MessageEntry>
    {
        private final String dimension;

        /**
         * Constructs a new dimension predicate.
         *
         * @param dimension dimension identifier
         */
        public DimensionPredicate(String dimension)
        {
            this.dimension = dimension;
        }

        /**
         * Tests whether a dimension identifier is within scope of a message entry.
         *
         * @param entry configured message entry with list of in-scope dimension identifiers
         * @return true if the dimension identifier is contained in the entry's dimensions
         */
        @Override
        public boolean test(MessageEntry entry)
        {
            return entry.dimensions != null
                    && entry.dimensions.length > 0
                    && Arrays.asList(entry.dimensions).contains(dimension);
        }
    }
}
