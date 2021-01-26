package me.axieum.mcmod.chatter.api.event.discord;

import net.dv8tion.jda.api.JDABuilder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface BuildJDACallback
{
    /**
     * Called before building the JDA client.
     */
    Event<BuildJDACallback> EVENT =
            EventFactory.createArrayBacked(BuildJDACallback.class, callbacks -> (builder) -> {
                for (BuildJDACallback callback : callbacks)
                    callback.onBuild(builder);
            });

    /**
     * Called before building the JDA client.
     *
     * @param builder JDA client builder
     */
    void onBuild(JDABuilder builder);
}
