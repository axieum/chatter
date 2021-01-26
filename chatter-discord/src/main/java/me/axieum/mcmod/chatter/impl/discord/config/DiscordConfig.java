package me.axieum.mcmod.chatter.impl.discord.config;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;

@Config(name = "chatter/discord")
public class DiscordConfig implements ConfigData
{
    /**
     * Registers and prepares a new configuration instance.
     *
     * @return registered config holder
     */
    public static DiscordConfig init()
    {
        return AutoConfig.register(DiscordConfig.class, JanksonConfigSerializer::new)
                         .getConfig();
    }
}
