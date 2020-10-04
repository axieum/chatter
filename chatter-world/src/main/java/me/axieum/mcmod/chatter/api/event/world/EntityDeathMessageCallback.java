package me.axieum.mcmod.chatter.api.event.world;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

public interface EntityDeathMessageCallback
{
    /**
     * Called when a named entity (with name tag) death message is sent.
     */
    Event<EntityDeathMessageCallback> EVENT =
            EventFactory.createArrayBacked(EntityDeathMessageCallback.class, callbacks -> (entity, source, text) -> {
                for (EntityDeathMessageCallback callback : callbacks) {
                    text = callback.onDeathMessage(entity, source, text);
                    if (text == null) break;
                }
                return text;
            });

    /**
     * Called when a named entity (with name tag) death message is sent.
     *
     * @param entity entity who died
     * @param source damage source
     * @param text   death message
     * @return death message or null
     */
    @Nullable
    Text onDeathMessage(LivingEntity entity, DamageSource source, Text text);
}
