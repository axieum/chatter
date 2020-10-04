package me.axieum.mcmod.chatter.mixin.world;

import me.axieum.mcmod.chatter.api.event.world.EntityDeathMessageCallback;
import me.axieum.mcmod.chatter.impl.world.config.WorldGameRules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.network.MessageType;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity
{
    @Shadow
    public abstract DamageTracker getDamageTracker();

    public LivingEntityMixin(EntityType<?> type, World world) { super(type, world); }

    /**
     * Injects into living entity deaths, broadcasting their death messages if named with a name tag.
     */
    @Inject(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageTracker;update()V"))
    public void onDeath(DamageSource source, CallbackInfo ci)
    {
        if (this.getServer() == null) return;
        if (!this.world.getGameRules().getBoolean(GameRules.SHOW_DEATH_MESSAGES)) return;
        if (!this.world.getGameRules().getBoolean(WorldGameRules.SHOW_NAMED_ENTITY_DEATH_MESSAGES)) return;

        // Determine if this is an entity worth grieving
        if (this.hasCustomName()) {
            // Derive a death message for the living entity
            Text text = this.getDamageTracker().getDeathMessage();

            // Fire an event to let listeners mutate/cancel the death message
            text = EntityDeathMessageCallback.EVENT.invoker()
                                                   .onDeathMessage((LivingEntity) (Object) this, source, text);

            // Send the named entity death message
            if (text != null)
                this.getServer().getPlayerManager().broadcastChatMessage(text, MessageType.SYSTEM, Util.NIL_UUID);
        }
    }
}
