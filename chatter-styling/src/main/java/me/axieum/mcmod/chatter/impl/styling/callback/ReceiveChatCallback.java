package me.axieum.mcmod.chatter.impl.styling.callback;

import me.axieum.mcmod.chatter.api.event.chat.ChatEvents;
import me.axieum.mcmod.chatter.impl.styling.config.StylingConfig;
import me.axieum.mcmod.chatter.impl.util.MessageFormat;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;
import java.util.regex.Pattern;

import static me.axieum.mcmod.chatter.impl.styling.ChatterStyling.CONFIG;
import static me.axieum.mcmod.chatter.impl.styling.ChatterStyling.LOGGER;

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
        if (CONFIG.chat.length < 1) return text;

        // Determine the message style for the player
        final StylingConfig.ChatStyle style = PLAYER_CACHE.computeIfAbsent(player.getUuid(), uuid ->
                Arrays.stream(CONFIG.chat)
                      .filter(s -> {
                          // First, try to match the player's UUID
                          if (Arrays.asList(s.uuids).contains(uuid.toString()))
                              return true;

                          // Next, try to match against a group
                          // NB: At writing, there is no widely available permissions api
                          return Arrays.asList(s.groups).contains(player.hasPermissionLevel(2) ? "op" : "player");
                      })
                      .findFirst()
                      .orElse(null)
        );
        if (style == null) return text;

        // Apply styling rules
        if (style.color)
            raw = COLOR_CODE_PATTERN.matcher(raw).replaceAll("\u00A7$1");

        // Define a message formatter
        final MessageFormat formatter = new MessageFormat()
                .tokenize("player", player.getDisplayName().getString())
                .tokenize("message", raw)
                .datetime("datetime");

        // Parse the JSON template into a Text component ready for dispatching
        try {
            return Text.Serializer.fromJson(formatter.apply(style.template));
        } catch (Exception e) {
            LOGGER.error("Unable to parse invalid text JSON: {}", e.getMessage());
            return text;
        }
    }
}
