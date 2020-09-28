package me.axieum.mcmod.chatter.mixin.event.chat.client;

import me.axieum.mcmod.chatter.api.event.chat.client.ClientChatEvents;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin
{
    /**
     * Redirects received player chat messages on the client to allow mutating the message.
     */
    @Redirect(method = "onGameMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/InGameHud;addChatMessage(Lnet/minecraft/network/MessageType;Lnet/minecraft/text/Text;Ljava/util/UUID;)V"))
    private void onChatMessage(InGameHud inGameHud, MessageType type, Text text, UUID uuid)
    {
        text = ClientChatEvents.RECEIVE_CHAT.invoker().onReceiveMessage(type, text, uuid);
        if (text != null)
            inGameHud.addChatMessage(type, text, uuid);
    }
}
