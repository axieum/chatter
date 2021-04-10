package me.axieum.mcmod.chatter.impl.discord.config.module;

import com.jagrosh.jdautilities.command.Command;
import com.jagrosh.jdautilities.command.CommandClient;
import me.axieum.mcmod.chatter.impl.discord.ChatterDiscord;
import me.axieum.mcmod.chatter.impl.discord.callback.discord.MessageReceivedListener;
import me.axieum.mcmod.chatter.impl.discord.command.DiscordCommands;
import me.axieum.mcmod.chatter.impl.discord.config.DiscordConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.Nullable;

@Config(name = "commands")
public class CommandConfig implements ConfigData
{
    @Comment("True if any commands should be available for use")
    public boolean enabled = true;

    @Comment("Message prefix used in Discord to trigger commands")
    public String prefix = "!";

    @Comment("If defined, exposes a command to display help")
    public @Nullable String helpWord = "help";

    @Comment("Identifiers for users within Discord to whom to grant all permissions")
    public String[] admins = {};

    @Category("Messages")
    @Comment("Feedback provided to the user who triggered a command")
    public Messages messages = new Messages();

    public static class Messages
    {
        @Comment("The error message used when the server is unavailable")
        public String unavailable = "The server is not yet ready - please wait. :warning:";

//        @Comment("The error message used when a user is denied permission to a command\n" +
//                "Use ${role} for the required role name")
//        public String denied = "You don't have permission to do that! :no_good:";

//        @Comment("The error message used when a user must wait before executing a command\n" +
//                "Use ${cooldown[:format]} for the remaining duration")
//        public String cooldown = "Please wait another ${cooldown} before doing that! :alarm_clock:";
    }

    @Category("Built-in Commands")
    @Comment("Built-in Discord commands")
    public Builtin builtin = new Builtin();

    public static class Builtin
    {
        @Category("Uptime Command")
        @Comment("Displays for how long the Minecraft server has been online")
        public Uptime uptime = new Uptime();

        public static class Uptime extends Base
        {
            public Uptime()
            {
                name = "uptime";
                help = "Shows for how long the server has been online";
            }

            @Comment("A message template that is formatted and sent for the server's uptime\n" +
                    "Use ${uptime[:format]} for the duration")
            public String message = "The server has been online for ${uptime} :hourglass_flowing_sand:";
        }

        @Category("TPS Command")
        @Comment("Displays the Minecraft server's current ticks per second")
        public TPS tps = new TPS();

        public static class TPS extends Base
        {
            public TPS()
            {
                name = "tps";
                aliases = new String[]{"ticks"};
                help = "Shows the server's current ticks per second";
                cooldown = 10;
                cooldownScope = Command.CooldownScope.CHANNEL;
            }
        }
    }

    @Category("Custom Commands")
    @Comment("Custom Discord commands")
    public Custom[] custom = new Custom[]{new Custom()};

    public static class Custom extends Base
    {
        public Custom()
        {
            name = "whitelist";
            aliases = new String[]{"wl"};
            help = "Manages the whitelist for the server";
            usage = "<list/add/remove> [username]";
            role = "Admin";
            enabled = false; // example custom commands should not be enabled by default
        }

        @Comment("True if the execution should not provide any feedback")
        public boolean quiet = false;

        @Comment("A Minecraft command to execute\n" +
                "Use {n} for the nth argument, and {} for all")
        public String command = "/whitelist {}";
    }

    /**
     * Base command configuration schema.
     */
    public static abstract class Base
    {
        @Comment("True if the command should be available for use")
        public boolean enabled = true;

        @Comment("Trigger name for the command")
        public String name;

        @Comment("Any alternative trigger names for the command")
        public String[] aliases = {};

        @Comment("A brief description of what the command does")
        public String help;

        @Comment("Details the correct usage of the command, e.g. <username> [count]")
        public String usage;

        @Comment("True if the command should be hidden from help messages")
        public boolean hidden = false;

        @Comment("If defined, restricts access to Discord users with the given role identifier or name")
        public @Nullable String role = null;

        @Comment("The number of seconds a user must wait before using the command again")
        public int cooldown = 0;

        @Comment("To whom the cooldown applies (see https://git.io/JtpsJ)")
        public Command.CooldownScope cooldownScope = Command.CooldownScope.USER;
    }

    /**
     * Handles a reload of the configuration instance.
     *
     * @param holder registered config holder
     * @param config updated config instance
     * @return reload action result
     * @see ConfigHolder#load()
     */
    public static ActionResult reload(ConfigHolder<DiscordConfig> holder, DiscordConfig config)
    {
        return ChatterDiscord.getClient().map(jda -> {
            // Build a new JDA command client, and validate it
            final @Nullable CommandClient commands = config.commands.enabled ? DiscordCommands.build(config) : null;

            // If commands are enabled, and the command client failed to build, fail
            if (config.commands.enabled && commands == null)
                return ActionResult.FAIL;

            // Shutdown the current JDA command client, if present
            ChatterDiscord.getCommandClient().ifPresent(client -> {
                jda.removeEventListener(client);
                client.shutdown();
            });

            // Set the new command client
            ChatterDiscord.commands = commands;

            // Register the command listener, and re-route received messages through the client if present
            if (commands != null) {
                jda.addEventListener(commands);
                // Remove any separate message received listeners, if present
                // NB: Received messages are handled by the command client (on non-commands)
                jda.getRegisteredListeners()
                   .stream()
                   .filter(l -> l instanceof MessageReceivedListener)
                   .forEach(jda::removeEventListener);
            } else {
                // Ensure there is only one separate message received listener
                if (jda.getRegisteredListeners().stream().noneMatch(l -> l instanceof MessageReceivedListener))
                    jda.addEventListener(new MessageReceivedListener());
            }

            return ActionResult.PASS;
        }).orElse(ActionResult.PASS);
    }
}
