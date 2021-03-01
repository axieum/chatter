package me.axieum.mcmod.chatter.impl.discord.command.discord;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandEvent;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import me.axieum.mcmod.chatter.api.event.discord.MinecraftCommandEvents;
import me.axieum.mcmod.chatter.impl.discord.command.DiscordCommands;
import me.axieum.mcmod.chatter.impl.discord.util.ServerUtils;
import net.dv8tion.jda.api.EmbedBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.BiConsumer;

import static me.axieum.mcmod.chatter.impl.discord.ChatterDiscord.LOGGER;

public class MinecraftCommand implements BiConsumer<Command, CommandEvent>
{
    private static final int PERMISSION_LEVEL = 4;
    private final String command;
    private final boolean quiet;

    /**
     * Constructs a new Minecraft proxy command executor.
     *
     * @param command a Minecraft command to execute (with or without a leading '/')
     * @param quiet   true if the execution should not provide any feedback
     */
    public MinecraftCommand(final @NotNull String command, final boolean quiet)
    {
        this.command = command;
        this.quiet = quiet;
    }

    @Override
    public void accept(Command command, CommandEvent event)
    {
        // Retrieve the Minecraft server if it is present and ready
        // NB: Update to Java 9's `Optional#ifPresentOrElse` when Java 8 support is dropped
        final @Nullable MinecraftServer server = ServerUtils.getInstance().filter(ServerUtils::isReady).orElse(null);

        // If the server is present and ready, handle the command
        if (server != null) {
            // Let them know that their request is in-progress
            if (!quiet) event.getChannel().sendTyping().queue();

            // Prepare the Minecraft command
            final String mcCommand = formatMinecraftCommand(this.command, event.getArgs().split("\\s"));

            // Fire a proxying event to let listeners cancel the command execution
            if (!MinecraftCommandEvents.BEFORE_EXECUTE.invoker().beforeExecute(mcCommand, command, event)) return;
            LOGGER.info("@{} is proxying the command: '{}' -> '/{}'", event.getAuthor().getAsTag(),
                    event.getMessage().getContentRaw(), mcCommand);

            // Create a temporary command source and hence output, to relay command feedback
            final String username = event.getMember().getEffectiveName();
            final ServerCommandSource source = new ServerCommandSource(
                    new DiscordCommandOutput(server, mcCommand, command, event),
                    Vec3d.ZERO, Vec2f.ZERO, null,
                    PERMISSION_LEVEL, username, new LiteralText(username),
                    server, null
            );

            // Try to proxy the Minecraft command
            try {
                server.getCommandManager().getDispatcher().execute(mcCommand, source.withConsumer((c, s, r) -> {
                    // NB: The command was proxied, however the actual feedback was sent to the source's command output
                    LOGGER.info("@{} proxied the command: '/{}' with result {}",
                            event.getAuthor().getAsTag(), c.getInput(), r);
                }));
            } catch (CommandSyntaxException e) {
                // NB: The command was indeed proxied, but the result of the command was not,
                // e.g. trying to whitelist a player who is already whitelisted
                reply(event, command, mcCommand, e.getMessage(), false);
            }
        } else {
            if (!quiet) DiscordCommands.replyUnavailable(event);
        }
    }

    /**
     * Replies with the command execution result.
     *
     * @param event     JDA command event
     * @param command   JDA command instance from Discord
     * @param mcCommand Minecraft command that was executed
     * @param result    Minecraft command execution feedback
     * @param success   true if the command was a success
     */
    public void reply(CommandEvent event, Command command, String mcCommand, String result, boolean success)
    {
        // Bail if this is a quiet command
        if (quiet) return;

        // Build an initial embed for the result
        EmbedBuilder embed = new EmbedBuilder()
                .setColor(success ? 0x00ff00 : 0xff0000) // green for success, red for failure
                .setDescription(result);

        // Fire a post-proxy event to let listeners intercept the result
        if ((embed = MinecraftCommandEvents.AFTER_EXECUTE.invoker().afterExecute(
                event, command, mcCommand, result, success, embed)) == null)
            return;

        // Build and reply with the resulting embed
        event.reply(embed.build());
    }

    /**
     * Virtual Minecraft Command Output for Discord.
     */
    private class DiscordCommandOutput implements CommandOutput
    {
        private final MinecraftServer server;
        private final String mcCommand;
        private final Command command;
        private final CommandEvent event;

        /**
         * Constructs a new virtual command output for relaying feedback to Discord.
         *
         * @param server    Minecraft server
         * @param mcCommand command to be executed in Minecraft (without leading '/')
         * @param command   JDA command instance from Discord
         * @param event     JDA command event to reply to
         */
        public DiscordCommandOutput(MinecraftServer server, String mcCommand, Command command, CommandEvent event)
        {
            this.server = server;
            this.mcCommand = mcCommand;
            this.command = command;
            this.event = event;
        }

        @Override
        public void sendSystemMessage(Text message, UUID senderUuid)
        {
            reply(event, command, mcCommand, message.getString(), true);
        }

        @Override
        public boolean shouldReceiveFeedback()
        {
            return !quiet;
        }

        @Override
        public boolean shouldTrackOutput()
        {
            return false;
        }

        @Override
        public boolean shouldBroadcastConsoleToOps()
        {
            return server.getGameRules().getBoolean(GameRules.COMMAND_BLOCK_OUTPUT);
        }
    }

    /**
     * Substitutes a Minecraft command template with its arguments.
     *
     * @param template command template using {n} for the nth argument, and {} for all
     * @param args     arguments to substitute into the template
     * @return command template with {n} and {} patterns substituted for arguments
     */
    public static String formatMinecraftCommand(@NotNull String template, String... args)
    {
        if (template.length() == 0) return template;
        // Replace any arguments
        if (args != null && args.length > 0) {
            // Replace {n} with nth argument
            for (int i = 0; i < args.length; i++)
                template = template.replaceAll("\\{" + i + "}", args[i]);
            // Replace {} with all arguments
            template = template.replaceAll("\\{}", String.join(" ", args));
        }
        // Replace any left-overs, and trim
        template = template.replaceAll("\\{\\d*}", "").trim();
        // Strip any leading '/' if present, and return
        return template.length() > 0 && template.charAt(0) == '/' ? template.substring(1) : template;
    }
}
