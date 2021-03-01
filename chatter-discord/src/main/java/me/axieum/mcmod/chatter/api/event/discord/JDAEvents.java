package me.axieum.mcmod.chatter.api.event.discord;

import com.jagrosh.jdautilities.command.CommandClientBuilder;
import net.dv8tion.jda.api.JDABuilder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class JDAEvents
{
    /**
     * Called before building the JDA client.
     */
    public static final Event<BuildClient> BUILD_CLIENT =
            EventFactory.createArrayBacked(BuildClient.class, callbacks -> (builder) -> {
                for (BuildClient callback : callbacks)
                    callback.onBuildClient(builder);
            });

    /**
     * Called before building the JDA command client.
     */
    public static final Event<BuildCommandClient> BUILD_COMMAND_CLIENT =
            EventFactory.createArrayBacked(BuildCommandClient.class, callbacks -> (builder) -> {
                for (BuildCommandClient callback : callbacks)
                    callback.onBuildCommandClient(builder);
            });

    @FunctionalInterface
    public interface BuildClient
    {
        /**
         * Called before building the JDA client.
         *
         * @param builder JDA client builder
         */
        void onBuildClient(JDABuilder builder);
    }

    @FunctionalInterface
    public interface BuildCommandClient
    {
        /**
         * Called before building the JDA command client.
         *
         * @param builder JDA command client builder
         */
        void onBuildCommandClient(CommandClientBuilder builder);
    }
}
