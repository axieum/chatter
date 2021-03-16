package me.axieum.mcmod.chatter.impl.discord.callback.minecraft;

import me.axieum.mcmod.chatter.api.event.world.EntityDeathMessageCallback;
import me.axieum.mcmod.chatter.impl.discord.ChatterDiscord;
import me.axieum.mcmod.chatter.impl.discord.config.module.MessageConfig.DimensionPredicate;
import me.axieum.mcmod.chatter.impl.discord.util.DiscordDispatcher;
import me.axieum.mcmod.chatter.impl.discord.util.StringUtils;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class EntityDeathCallback implements EntityDeathMessageCallback
{
    @Override
    public @Nullable Text onDeathMessage(LivingEntity entity, DamageSource source, Text text)
    {
        // Send Discord notifications
        ChatterDiscord.getClient().ifPresent(jda -> {
            // Prepare a message formatter
            final String entityName = entity.getDisplayName().getString();
            final MessageFormat formatter = new MessageFormat()
                    .datetime("datetime")
                    .tokenize("name", entityName)
                    .tokenize("cause", source.getDeathMessage(entity).getString()
                                             .replaceFirst(entityName, "").trim())
                    .tokenize("world", StringUtils.getWorldName(entity.world))
                    .tokenize("x", String.valueOf((int) entity.prevX))
                    .tokenize("y", String.valueOf((int) entity.prevY))
                    .tokenize("z", String.valueOf((int) entity.prevZ));

            // Dispatch a message to all configured channels
            DiscordDispatcher.embed((embed, entry) -> embed.setColor(Color.RED)
                                                           .setDescription(formatter.apply(entry.discord.grief)),
                    (entry) -> entry.discord.grief != null,
                    new DimensionPredicate(StringUtils.getWorldId(entity.world)));
        });

        return text;
    }
}
