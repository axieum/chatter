package me.axieum.mcmod.chatter.impl.discord.config.module;

import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import org.jetbrains.annotations.Nullable;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.LOGGER;

@Config(name = "theme")
public class ThemeConfig implements ConfigData
{
    @Comment("Template URL for retrieving Minecraft avatars, i.e. for used in embeds\n" +
            "Use ${username} for the player's name, and ${size} for the height in pixels")
    public @Nullable String avatarUrl = "https://minotar.net/helm/${username}/${size}";

    @Comment("True if emojis should be treated as unicode - useful if your players' client supports emojis")
    public boolean useUnicodeEmojis = false;

    @Category("Presences")
    @Comment("Bot presences relayed periodically")
    public Presence presence = new Presence();

    public static class Presence
    {
        @Comment("The number of seconds between presence updates (must be >=15)")
        public int interval = 60;

        @Comment("True if the presences should be randomly selected")
        public boolean random = false;

        @Comment("Presences")
        public PresenceEntry[] entries = {new PresenceEntry()};

        public static class PresenceEntry
        {
            @Comment("The type of presence (see https://git.io/Jqkez)")
            public ActivityType type = ActivityType.DEFAULT;

            @Comment("The text value that is put on display")
            public String value = "Minecraft";

            @Comment("If defined, sets the URL of the underlying media, e.g. Twitch stream")
            public @Nullable String url = null;
        }
    }

    @Override
    public void validatePostLoad()
    {
        // Check that the interval is within reasonable bounds
        if (presence.interval < 15) {
            LOGGER.warn("A presence update interval shorter than 15 seconds will lead to rate-limits! Reverting to 15s");
            presence.interval = 15;
        }
    }

    /**
     * Returns a Minecraft avatar URL for the given player's name.
     *
     * @param username Minecraft username
     * @param size     height and width in pixels
     * @return formatted Minecraft avatar URL or null if template is not set
     */
    public @Nullable String getAvatarUrl(final String username, final int size)
    {
        if (this.avatarUrl == null || this.avatarUrl.length() == 0) return null;
        return new MessageFormat().tokenize("username", username)
                                  .tokenize("size", String.valueOf(size))
                                  .apply(this.avatarUrl);
    }
}
