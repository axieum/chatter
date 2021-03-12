package me.axieum.mcmod.chatter.mixin.event.player;

import me.axieum.mcmod.chatter.api.event.player.PlayerEvents;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin
{
    @Shadow
    public abstract ServerWorld getServerWorld();

    /**
     * Injects into living entity deaths, broadcasting their death messages if named with a name tag.
     */
    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;getPrimeAdversary()Lnet/minecraft/entity/LivingEntity;"))
    public void onDeath(DamageSource source, CallbackInfo ci)
    {
        // Fire an event to let listeners mutate/cancel the death message
        PlayerEvents.DEATH.invoker().onDeath((ServerPlayerEntity) (Object) this, source.getAttacker(), source);
    }
}
