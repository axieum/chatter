package me.axieum.mcmod.chatter.mixin.event.chat.client;

import me.axieum.mcmod.chatter.api.event.chat.client.ClientChatEvents;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenMixin
{
    /**
     * Intercepts new client chat messages to allow mutating the message before sending.
     */
    @ModifyVariable(method = "sendMessage(Ljava/lang/String;Z)V", at = @At(value = "HEAD"), argsOnly = true)
    public String sendChatMessage(String message, String arg, boolean toHud)
    {
        return ClientChatEvents.SEND_CHAT.invoker().onSendMessage(message);
    }

    /**
     * Cancels empty/null messages - from where the callback is cancelled above.
     */
    @Inject(method = "sendMessage(Ljava/lang/String;Z)V", at = @At(value = "HEAD"), cancellable = true)
    public void broadcastChatMessage(String message, boolean toHud, CallbackInfo ci)
    {
        if (message == null || message.isEmpty())
            ci.cancel();
    }
}
