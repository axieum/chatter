package me.axieum.mcmod.chatter.impl.filtering.config;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "chatter/filter")
public class FilteringConfig implements ConfigData
{
    @Comment("True if filtered messages should be rejected")
    boolean reject = false;

    @Comment("Number of rejected messages within one minute before kicking the player")
    int threshold = 3;

    @Comment("Character used for censoring filtered words or phrases")
    char censor = '*';

    /**
     * Registers and prepares a new configuration instance.
     *
     * @return registered config holder
     */
    public static FilteringConfig init()
    {
        return AutoConfig.register(FilteringConfig.class, JanksonConfigSerializer::new)
                         .getConfig();
    }
}
