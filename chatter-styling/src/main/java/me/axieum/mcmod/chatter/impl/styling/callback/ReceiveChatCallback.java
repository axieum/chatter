package me.axieum.mcmod.chatter.impl.styling.callback;

import me.axieum.mcmod.chatter.api.event.chat.ChatEvents;
import me.axieum.mcmod.chatter.api.styling.ChatStyleProvider;
import me.axieum.mcmod.chatter.impl.styling.config.StylingConfig;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import me.axieum.mcmod.chatter.impl.util.StringUtils;
import me.axieum.mcmod.chatter.mixin.styling.TeamAccessor;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Pattern;

import static me.axieum.mcmod.chatter.impl.styling.ChatterStyling.LOGGER;
import static me.axieum.mcmod.chatter.impl.styling.ChatterStyling.getConfig;

/**
 * Tweaks server received messages with configured formats.
 */
public class ReceiveChatCallback implements ChatEvents.ReceiveChat
{
    public static final HashMap<UUID, StylingConfig.ChatStyle> PLAYER_CACHE = new HashMap<>();
    private static final Pattern COLOR_CODE_PATTERN = Pattern.compile("(?i)&([0-9a-fk-or])");

    @Override
    @Nullable
    public Text onReceiveMessage(ServerPlayerEntity player, String raw, Text text)
    {
        // Are there any styles configured?
        if (getConfig().chat.length < 1 || player == null) return text;

        // Fetch the registered chat style provider
        final ChatStyleProvider provider = ChatStyleProvider.getProvider();
        if (provider == null) return text;

        // Find the player's group
        final String group = provider.getGroup(player);

        // Determine the message style for the player
        final StylingConfig.ChatStyle style = PLAYER_CACHE.computeIfAbsent(player.getUuid(), uuid ->
                Arrays.stream(getConfig().chat)
                      .filter(s -> {
                          // First, try to match the player's UUID
                          if (Arrays.asList(s.uuids).contains(uuid.toString()))
                              return true;

                          // Next, try to match against a group
                          return Arrays.asList(s.groups).contains(group);
                      })
                      .findFirst()
                      .orElse(null)
        );
        if (style == null) return text;

        // Apply styling rules
        if (style.color && provider.canUseColor(player))
            raw = COLOR_CODE_PATTERN.matcher(raw).replaceAll("\u00A7$1");

        // Define a message formatter
        final MessageFormat formatter = new MessageFormat()
                .tokenize("username", provider.getPlayer(player))
                .tokenize("player", provider.getPlayerName(player))
                .tokenize("group", provider.getGroupName(player))
                .optional("prefix", provider.getPrefix(player))
                .optional("suffix", provider.getSuffix(player))
                .tokenize("world", () -> StringUtils.getWorldName(player.world)) // lazy world name
                .tokenize("team", () -> player.getScoreboardTeam() != null ? // lazy team name
                                        ((Team) player.getScoreboardTeam()).getDisplayName().getString() : "")
                .tokenize("team_color", () -> player.getScoreboardTeam() != null ? // lazy team color
                                              ((TeamAccessor) player.getScoreboardTeam()).getColor().getName() : "")
                .datetime("datetime")
                .regex(COLOR_CODE_PATTERN, m -> "\u00A7" + m.get(1)) // replace all colour codes up to this point
                .tokenize("message", provider.getMessage(player, raw));

        // Parse the JSON template into a Text component ready for dispatching
        try {
            return Text.Serializer.fromJson(formatter.apply(style.template));
        } catch (Exception e) {
            LOGGER.error("Unable to parse invalid text JSON: {}", e.getMessage());
            return text;
        }
    }
}
