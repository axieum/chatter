package me.axieum.mcmod.chatter.mixin.event.player;

import me.axieum.mcmod.chatter.api.event.player.PlayerEvents;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerAdvancementTracker.class)
public abstract class PlayerAdvancementTrackerMixin
{
    @Shadow
    private ServerPlayerEntity owner;

    /**
     * Intercepts the granting of advancement criterion.
     */
    @Inject(method = "grantCriterion", at = @At(value = "INVOKE", target = "Lnet/minecraft/advancement/AdvancementRewards;apply(Lnet/minecraft/server/network/ServerPlayerEntity;)V"))
    public void grantCriterion(Advancement advancement, String name, CallbackInfoReturnable<Boolean> cir)
    {
        PlayerEvents.GRANT_CRITERION.invoker().onGrantCriterion(this.owner, advancement);
    }
}
