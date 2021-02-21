package me.axieum.mcmod.chatter.impl.discord.config.module;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry.Category;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "commands")
public class CommandConfig implements ConfigData
{
    @Comment("Message prefix used in Discord to trigger commands")
    public String prefix = "!";

    @Comment("If defined, exposes a command to display help")
    public String helpWord = "help";

    @Comment("Identifiers for users within Discord to whom to grant all permissions")
    public String[] admins = {};

    @Category("Messages")
    @Comment("Feedback provided to the user whom triggered a command")
    public Messages messages = new Messages();

    public static class Messages
    {
        @Comment("The error message used when the server is unavailable")
        public String unavailable = "The server is not yet ready - please wait. :warning:";

        @Comment("The error message used when a user is denied permission to a command")
        public String denied = "You don't have permission to do that! :no_good:";
    }
}
