package me.axieum.mcmod.chatter.impl.discord.callback.minecraft;

import me.axieum.mcmod.chatter.api.event.player.PlayerEvents;
import me.axieum.mcmod.chatter.impl.discord.ChatterDiscord;
import me.axieum.mcmod.chatter.impl.discord.config.module.MessageConfig.DimensionPredicate;
import me.axieum.mcmod.chatter.impl.discord.util.DiscordDispatcher;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import me.axieum.mcmod.chatter.impl.util.StringUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;

import java.awt.*;
import java.time.Duration;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.getConfig;

public class PlayerDeathCallback implements PlayerEvents.Death
{
    @Override
    public void onDeath(ServerPlayerEntity player, Entity attacker, DamageSource source)
    {
        // Send Discord notifications
        ChatterDiscord.getClient().ifPresent(jda -> {
            // Prepare a message formatter
            final String playerName = player.getDisplayName().getString();
            final MessageFormat formatter = new MessageFormat()
                    .datetime("datetime")
                    .tokenize("username", player.getName().getString())
                    .tokenize("player", playerName)
                    .tokenize("cause", source.getDeathMessage(player).getString()
                                             .replaceFirst(playerName, "").trim())
                    .tokenize("world", StringUtils.getWorldName(player.world))
                    .tokenize("x", String.valueOf((int) player.prevX))
                    .tokenize("y", String.valueOf((int) player.prevY))
                    .tokenize("z", String.valueOf((int) player.prevZ))
                    .duration("lifespan", () -> Duration.ofMinutes(
                            player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.TIME_SINCE_DEATH))
                    )) // lazy elapsed
                    .tokenize("score", String.valueOf(player.getScore()))
                    .tokenize("exp", String.valueOf(player.experienceLevel));

            // Dispatch a message to all configured channels
            DiscordDispatcher.embed((embed, entry) -> embed.setColor(Color.RED)
                                                           .setDescription(formatter.apply(entry.discord.death))
                                                           .setThumbnail(getConfig().theme.getAvatarUrl(playerName, 16)),
                    (entry) -> entry.discord.death != null,
                    new DimensionPredicate(StringUtils.getWorldId(player.world)));
        });
    }
}
