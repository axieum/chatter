package me.axieum.mcmod.chatter.impl.styling.config;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.minecraft.text.Text;

import java.util.Arrays;

@Config(name = "chatter/style")
public class StylingConfig implements ConfigData
{
    @Comment("Chat Styles")
    public ChatStyle[] chat = {new ChatStyle()};

    public static class ChatStyle
    {
        @Comment("Reduces the scope of messages to players belonging to the listed groups\n" +
                "Use 'player' and 'operator' if you do not have a permissions mod")
        public String[] groups = {};

        @Comment("Reduces the scope of messages to players with the listed UUIDs (see https://minecraftuuid.com)")
        public String[] uuids = {};

        @Comment("The in-game chat message JSON template (see https://minecraftjson.com)\n" +
                "Use ${player}, ${message}, ${group}, ${team}, ${prefix}, ${suffix} and ${datetime[:format]}")
        public String template =
                "[\"\",{\"text\":\"${player}\",\"color\":\"yellow\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tell ${player} \"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[\"\",{\"text\":\"Click to direct message\",\"italic\":true}]}},{\"text\":\" > \",\"color\":\"dark_gray\"},{\"text\":\"${message}\"}]";

        @Comment("True if players can use colour codes in their messages, i.e. &[0-9a-fk-or]")
        public boolean color = false;
    }

    @Override
    public void validatePostLoad() throws ValidationException
    {
        // Check the validity of any JSON message templates
        try {
            // Collect all JSON templates, and attempt to parse them as JSON into valid Minecraft text
            Arrays.stream(chat).map(entry -> entry.template).forEach(Text.Serializer::fromJson);
        } catch (Exception e) {
            throw new ValidationException("Invalid text JSON template", e);
        }
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
