package me.axieum.mcmod.chatter.mixin.styling;

import com.mojang.authlib.GameProfile;
import me.axieum.mcmod.chatter.impl.styling.callback.ReceiveChatCallback;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects into player permission changes, expiring cached player styles.
 */
@Mixin(PlayerManager.class)
public class PlayerManagerMixin
{
    @Inject(method = "remove", at = @At(value = "TAIL"))
    public void remove(ServerPlayerEntity player, CallbackInfo ci)
    {
        ReceiveChatCallback.PLAYER_CACHE.remove(player.getUuid());
    }

    @Inject(method = "addToOperators", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/OperatorList;add(Lnet/minecraft/server/ServerConfigEntry;)V"))
    public void addToOperators(GameProfile profile, CallbackInfo ci)
    {
        ReceiveChatCallback.PLAYER_CACHE.remove(profile.getId());
    }

    @Inject(method = "removeFromOperators", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/OperatorList;remove(Ljava/lang/Object;)V"))
    public void removeFromOperators(GameProfile profile, CallbackInfo ci)
    {
        ReceiveChatCallback.PLAYER_CACHE.remove(profile.getId());
    }
}
