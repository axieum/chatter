package me.axieum.mcmod.chatter.impl.discord.config.module;

import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;
import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.Nullable;

@Config(name = "theme")
public class ThemeConfig implements ConfigData
{
    @Comment("Template URL for retrieving Minecraft avatars, i.e. for used in embeds\n" +
            "Use ${username} for the player's name, and ${size} for the height in pixels")
    public @Nullable String avatarUrl = "https://minotar.net/helm/${username}/${size}";

    /**
     * Returns Minecraft avatar URL for a given player.
     *
     * @param player Minecraft player
     * @param size   height and width in pixels
     * @return formatted Minecraft avatar URL or null if template is not set
     */
    @Nullable
    public String getAvatarUrl(PlayerEntity player, int size)
    {
        if (this.avatarUrl != null && this.avatarUrl.length() == 0) return null;
        return new MessageFormat().tokenize("username", player.getDisplayName().getString())
                                  .tokenize("size", String.valueOf(size))
                                  .apply(this.avatarUrl);
    }
}
