package me.axieum.mcmod.chatter.impl.styling.config;

import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.ConfigData;
import me.sargunvohra.mcmods.autoconfig1u.annotation.Config;
import me.sargunvohra.mcmods.autoconfig1u.serializer.JanksonConfigSerializer;
import me.sargunvohra.mcmods.autoconfig1u.shadowed.blue.endless.jankson.Comment;

@Config(name = "chatter/style")
public class StylingConfig implements ConfigData
{
    @Comment("Chat Styles")
    public ChatStyle[] chat = {};

    public static class ChatStyle
    {
        @Comment("Author permission groups\n" +
                 "Allowed Values: 'player' or 'op'")
        public String[] groups = {};

        @Comment("Author UUIDs\n" +
                 "Check out https://minecraftuuid.com/ (third party)")
        public String[] uuids = {};

        @Comment("In-game chat message JSON template\n" +
                 "Check out https://minecraftjson.com/ (third party)\n" +
                 "Allowed Substitutions: ${player}, ${message}, ${group}, ${datetime|format}")
        public String template =
                "[\"\",{\"text\":\"${player}\",\"color\":\"yellow\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tell ${player} \"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[\"\",{\"text\":\"Click to direct message\",\"italic\":true}]}},{\"text\":\" > \",\"color\":\"dark_gray\"},{\"text\":\"${message}\"}]";

        @Comment("True if players can use colour codes, i.e. &[0-9a-fk-or]")
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
