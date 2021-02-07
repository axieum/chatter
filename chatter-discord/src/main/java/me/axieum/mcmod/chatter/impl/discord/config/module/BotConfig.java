package me.axieum.mcmod.chatter.impl.discord.config.module;

import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry.Category;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry.Gui.RequiresRestart;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;
import net.dv8tion.jda.api.OnlineStatus;

@Config(name = "bot")
public class BotConfig implements ConfigData
{
    @Comment("Token used to authenticate against your Discord bot")
    @RequiresRestart
    public String token = "";

    @Category("Bot Status")
    @Comment("Bot statuses relayed during the lifecycle of the server")
    public Status status = new Status();

    public static class Status
    {
        @Comment("Status while the server is starting")
        public OnlineStatus starting = OnlineStatus.IDLE;

        @Comment("Status after the server has started")
        public OnlineStatus started = OnlineStatus.ONLINE;

        @Comment("Status while the server is stopping")
        public OnlineStatus stopping = OnlineStatus.DO_NOT_DISTURB;

        @Comment("Status after the server has stopped")
        public OnlineStatus stopped = OnlineStatus.OFFLINE;
    }

    @Comment("True if all guild members should be cached, in turn allowing @mentions\n" +
            "NB: This requires the Privileged Gateway Intent 'Server Members' to be enabled on your Discord bot!")
    @RequiresRestart
    public boolean cacheMembers = false;
}
