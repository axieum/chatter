package me.axieum.mcmod.chatter.api.event.discord;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface BuildCommandClientCallback
{
    /**
     * Called before building the JDA command client.
     */
    Event<BuildCommandClientCallback> EVENT =
            EventFactory.createArrayBacked(BuildCommandClientCallback.class, callbacks -> (builder) -> {
                for (BuildCommandClientCallback callback : callbacks)
                    callback.onBuild(builder);
            });

    /**
     * Called before building the JDA command client.
     *
     * @param builder JDA command client builder
     */
    void onBuild(CommandClientBuilder builder);
}
