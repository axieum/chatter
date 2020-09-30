package me.axieum.mcmod.chatter.api.event.chat;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public final class ChatEvents
{
    /**
     * Called when a player chat message is received on the server.
     */
    public static final Event<ReceiveChat> RECEIVE_CHAT =
            EventFactory.createArrayBacked(ReceiveChat.class, callbacks -> (player, raw, text) -> {
                for (ReceiveChat callback : callbacks) {
                    text = callback.onReceiveMessage(player, raw, text);
                    if (text == null) break;
                }
                return text;
            });

    @FunctionalInterface
    public interface ReceiveChat
    {
        /**
         * Called when a player chat message is received on the server.
         *
         * @param player player author
         * @param raw    message body
         * @param text   text component
         * @return chat text component or null
         */
        @Nullable
        Text onReceiveMessage(ServerPlayerEntity player, String raw, Text text);
    }
}
