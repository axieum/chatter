package me.axieum.mcmod.chatter.api.event.chat.client;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class ClientChatEvents
{
    /**
     * Called when a player chat message is received on the client.
     */
    public static final Event<ReceiveChat> RECEIVE_CHAT =
            EventFactory.createArrayBacked(ReceiveChat.class, callbacks -> (type, text, uuid) -> {
                for (ReceiveChat callback : callbacks) {
                    text = callback.onReceiveMessage(type, text, uuid);
                    if (text == null) break;
                }
                return text;
            });

    /**
     * Called when the client sends a chat message to the server.
     */
    public static final Event<SendChat> SEND_CHAT =
            EventFactory.createArrayBacked(SendChat.class, callbacks -> message -> {
                for (SendChat callback : callbacks) {
                    message = callback.onSendMessage(message);
                    if (message == null) break;
                }
                return message;
            });

    @FunctionalInterface
    public interface ReceiveChat
    {
        /**
         * Called when a player chat message is received on the client.
         *
         * @param type message type
         * @param text chat text component
         * @param uuid sender uuid
         * @return chat text component or null
         */
        @Nullable
        Text onReceiveMessage(MessageType type, Text text, @Nullable UUID uuid);
    }

    @FunctionalInterface
    public interface SendChat
    {
        /**
         * Called when the client sends a chat message to the server.
         *
         * @param message message body
         * @return message contents or null
         */
        @Nullable
        String onSendMessage(String message);
    }
}
