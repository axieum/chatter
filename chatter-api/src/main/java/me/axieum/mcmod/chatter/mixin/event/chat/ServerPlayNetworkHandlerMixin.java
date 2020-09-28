package me.axieum.mcmod.chatter.mixin.event.chat;

import me.axieum.mcmod.chatter.api.event.chat.ChatEvents;
import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin
{
    @Shadow
    public ServerPlayerEntity player;

    /**
     * Redirects received player chat messages on the server to allow mutating the message.
     */
    @Redirect(method = "onGameMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/PlayerManager;broadcastChatMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/MessageType;Ljava/util/UUID;)V"))
    private void onChatMessage(PlayerManager manager, Text text, MessageType type, UUID uuid)
    {
        final String raw = (String) ((TranslatableText) text).getArgs()[1];
        text = ChatEvents.RECEIVE_CHAT.invoker().onReceiveMessage(player, raw, text);
        if (text != null)
            manager.broadcastChatMessage(text, type, uuid);
    }
}
