package me.axieum.mcmod.chatter.impl.discord.config.module;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry.Category;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "messages")
public class MessageConfig implements ConfigData
{
    @Comment("Messages")
    public MessageEntry[] entries = {new MessageEntry()};

    public static class MessageEntry
    {
        @Comment("Identifier for a channel within Discord to observe")
        public long id;

        @Comment("If defined, reduces the scope of all events to the listed Minecraft dimension IDs (empty for all)")
        public int[] dimensions = {};

        @Category("Discord")
        @Comment("Minecraft events relayed to Discord")
        public Discord discord = new Discord();

        public static class Discord
        {
            @Comment("A player sent an in-game message")
            public String chat;

            @Comment("A player died")
            public String death;

            @Comment("A player unlocked an advancement")
            public String advancement;

            @Comment("A player teleported to another dimension")
            public String teleport;

            @Comment("A player joined the game")
            public String join;

            @Comment("A player left the game")
            public String leave;

            @Comment("The server began to start")
            public String starting;

            @Comment("The server started and is accepting connections")
            public String started;

            @Comment("The server began to stop")
            public String stopping;

            @Comment("The server stopped and is offline")
            public String stopped;

            @Comment("The server stopped unexpectedly and is inaccessible")
            public String crashed;

            @Comment("True if a crash report is attached to the server crashed message")
            public boolean uploadCrashReport = false;
        }

        @Category("Minecraft")
        @Comment("Discord events relayed to Minecraft")
        public Minecraft minecraft = new Minecraft();

        public static class Minecraft
        {
            @Comment("A user sent a message")
            public String chat;

            @Comment("A user edited their recently sent message")
            public String edit;

            @Comment("A user reacted to a recent message")
            public String react;

            @Comment("A user removed their reaction from a recent message")
            public String unreact;

            @Comment("A user sent a message that contained attachments")
            public String attachment;
        }
    }
}
