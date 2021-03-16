package me.axieum.mcmod.chatter.impl.styling.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;

@Config(name = "chatter/style")
public class StylingConfig implements ConfigData
{
    @Comment("Chat Styles")
    public ChatStyle[] chat = {new ChatStyle()};

    public static class ChatStyle
    {
        @Comment("Reduces the scope of messages to players belonging to the listed groups\n" +
                "Allowed values: 'player' and 'operator'")
        public String[] groups = {};

        @Comment("Reduces the scope of messages to players with the listed UUIDs (see https://minecraftuuid.com)")
        public String[] uuids = {};

        @Comment("The in-game chat message JSON template (see https://minecraftjson.com)\n" +
                "Use ${player}, ${message}, ${group} and ${datetime[:format]}")
        public String template =
                "[\"\",{\"text\":\"${player}\",\"color\":\"yellow\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tell ${player} \"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[\"\",{\"text\":\"Click to direct message\",\"italic\":true}]}},{\"text\":\" > \",\"color\":\"dark_gray\"},{\"text\":\"${message}\"}]";

        @Comment("True if players can use colour codes in their messages, i.e. &[0-9a-fk-or]")
        public boolean color = false;
    }

    /**
     * Registers and prepares a new configuration instance.
     *
     * @return registered config holder
     */
    public static StylingConfig init()
    {
        return AutoConfig.register(StylingConfig.class, JanksonConfigSerializer::new)
                         .getConfig();
    }
}
