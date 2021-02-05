package me.axieum.mcmod.chatter.impl.discord.config;

import me.axieum.mcmod.chatter.impl.discord.config.module.BotConfig;
import me.axieum.mcmod.chatter.impl.discord.config.module.MessageConfig;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.annotation.ConfigEntry.Category;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1u.serializer.PartitioningSerializer;

@Config(name = "chatter/discord")
public class DiscordConfig extends PartitioningSerializer.GlobalData
{
    @Category("Discord Bot")
    public BotConfig bot = new BotConfig();

    @Category("Messages")
    public MessageConfig messages = new MessageConfig();

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
