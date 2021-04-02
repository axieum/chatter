package me.axieum.mcmod.chatter.impl.discord.callback.minecraft;

import me.axieum.mcmod.chatter.api.event.player.PlayerEvents;
import me.axieum.mcmod.chatter.impl.discord.ChatterDiscord;
import me.axieum.mcmod.chatter.impl.discord.config.module.MessageConfig.DimensionPredicate;
import me.axieum.mcmod.chatter.impl.discord.util.DiscordDispatcher;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import me.axieum.mcmod.chatter.impl.util.StringUtils;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.server.network.ServerPlayerEntity;

public class PlayerAdvancementCallback implements PlayerEvents.GrantCriterion
{
    @Override
    public void onGrantCriterion(ServerPlayerEntity player, Advancement advancement)
    {
        // Send Discord notifications
        ChatterDiscord.getClient().ifPresent(jda -> {
            // Only listen for advancements that should be announced
            final AdvancementDisplay info = advancement.getDisplay();
            if (info == null || !info.shouldAnnounceToChat()) return;

            // Prepare a message formatter
            final MessageFormat formatter = new MessageFormat()
                    .datetime("datetime")
                    .tokenize("player", player.getDisplayName().getString())
                    .tokenize("type", info.getFrame().getId())
                    .tokenize("title", info.getTitle().getString())
                    .tokenize("description", info.getDescription().getString());

            // Dispatch a message to all configured channels
            DiscordDispatcher.embed((embed, entry) -> embed.setDescription(formatter.apply(entry.discord.advancement)),
                    (entry) -> entry.discord.advancement != null,
                    new DimensionPredicate(StringUtils.getWorldId(player.world)));
        });
    }
}
