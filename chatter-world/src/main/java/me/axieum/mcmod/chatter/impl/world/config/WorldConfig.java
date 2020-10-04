package me.axieum.mcmod.chatter.impl.world.config;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "chatter/world")
public class WorldConfig implements ConfigData
{
    /** @noinspection unused */
    @Comment("True if named animal/monster (with name tag) death messages are shown\n" +
             "NOTE: This option has no effect, rather use the game rule: showNamedEntityDeathMessages")
    public boolean showNamedEntityDeathMessages = true;

    /**
     * Registers and prepares a new configuration instance.
     *
     * @return registered config holder
     */
    public static WorldConfig init()
    {
        return AutoConfig.register(WorldConfig.class, JanksonConfigSerializer::new)
                         .getConfig();
    }
}
