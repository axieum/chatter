package me.axieum.mcmod.chatter.impl.discord.config;

import me.axieum.mcmod.chatter.impl.discord.config.module.BotConfig;
import me.axieum.mcmod.chatter.impl.discord.config.module.CommandConfig;
import me.axieum.mcmod.chatter.impl.discord.config.module.MessageConfig;
import me.axieum.mcmod.chatter.impl.discord.config.module.ThemeConfig;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry.Category;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.autoconfig.serializer.PartitioningSerializer;

@Config(name = "chatter/discord")
public class DiscordConfig extends PartitioningSerializer.GlobalData
{
    @Category("Discord Bot")
    public BotConfig bot = new BotConfig();

    @Category("Messages")
    public MessageConfig messages = new MessageConfig();

    @Category("Commands")
    public CommandConfig commands = new CommandConfig();

    @Category("Theme")
    public ThemeConfig theme = new ThemeConfig();

    /**
     * Registers and prepares a new configuration instance.
     *
     * @return registered config holder
     */
    public static DiscordConfig init()
    {
        return AutoConfig.register(DiscordConfig.class, PartitioningSerializer.wrap(JanksonConfigSerializer::new))
                         .getConfig();
    }
}
