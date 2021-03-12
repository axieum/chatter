package me.axieum.mcmod.chatter.api.event.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import net.dv8tion.jda.api.EmbedBuilder;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import org.jetbrains.annotations.Nullable;

public final class MinecraftCommandEvents
{
    /**
     * Called before executing a Minecraft command through Discord.
     */
    public static final Event<BeforeExecute> BEFORE_EXECUTE =
            EventFactory.createArrayBacked(BeforeExecute.class, callbacks -> (mc, dc, ev) -> {
                for (BeforeExecute callback : callbacks)
                    if (!callback.beforeExecute(mc, dc, ev))
                        return false;
                return true;
            });

    /**
     * Called after the execution of a Minecraft command through Discord.
     */
    public static final Event<AfterExecute> AFTER_EXECUTE =
            EventFactory.createArrayBacked(AfterExecute.class, callbacks -> (ev, dc, mc, r, s, e) -> {
                EmbedBuilder builder = e;
                for (AfterExecute callback : callbacks)
                    if ((builder = callback.afterExecute(ev, dc, mc, r, s, e)) == null)
                        break;
                return builder;
            });

    @FunctionalInterface
    public interface BeforeExecute
    {
        /**
         * Called after the execution of a Minecraft command through Discord.
         *
         * @param mcCommand command to be executed in Minecraft (without leading '/')
         * @param command   JDA command instance from Discord
         * @param event     JDA command event
         * @return true if the command should be proxied, otherwise cancel
         */
        boolean beforeExecute(String mcCommand, Command command, CommandEvent event);
    }

    @FunctionalInterface
    public interface AfterExecute
    {
        /**
         * Executes after a Minecraft command was proxied, but before the result is sent to Discord.
         *
         * @param event     JDA command event
         * @param command   JDA command instance from Discord
         * @param mcCommand command that was executed in Minecraft (without leading '/')
         * @param result    command execution feedback (from Minecraft) to be sent to Discord
         * @param success   true if the command was a success
         * @param embed     embed builder used to build the embed that will be sent to Discord, or null if quiet command
         * @return embed builder used to build the embed that will be sent to Discord, or null to cancel
         */
        @Nullable
        EmbedBuilder afterExecute(
                CommandEvent event,
                Command command,
                String mcCommand,
                String result,
                boolean success,
                @Nullable EmbedBuilder embed
        );
    }
}
