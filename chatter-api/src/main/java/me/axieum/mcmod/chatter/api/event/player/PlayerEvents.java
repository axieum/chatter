package me.axieum.mcmod.chatter.api.event.player;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.advancement.Advancement;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.Nullable;

public final class PlayerEvents
{
    /**
     * Called when a player is granted an advancement criterion.
     */
    public static final Event<GrantCriterion> GRANT_CRITERION =
            EventFactory.createArrayBacked(GrantCriterion.class, callbacks -> (player, adv) -> {
                for (GrantCriterion callback : callbacks)
                    callback.onGrantCriterion(player, adv);
            });

    /**
     * Called when a player dies.
     */
    public static final Event<Death> DEATH =
            EventFactory.createArrayBacked(Death.class, callbacks -> (player, attacker, source) -> {
                for (Death callback : callbacks)
                    callback.onDeath(player, attacker, source);
            });

    @FunctionalInterface
    public interface GrantCriterion
    {
        /**
         * Called when a player is granted an advancement criterion.
         *
         * @param player      player whom was granted the criterion
         * @param advancement advancement granted
         */
        void onGrantCriterion(ServerPlayerEntity player, Advancement advancement);
    }

    @FunctionalInterface
    public interface Death
    {
        /**
         * Called when a player dies.
         *
         * @param player   player whom died
         * @param attacker entity whom killed the player
         * @param source   damage source
         */
        void onDeath(ServerPlayerEntity player, @Nullable Entity attacker, DamageSource source);
    }
}
