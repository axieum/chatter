package me.axieum.mcmod.chatter.impl.styling.config;

import me.axieum.mcmod.chatter.impl.styling.callback.ReceiveChatCallback;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.ConfigHolder;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;

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
                "Use ${username}, ${player}, ${message}, ${group}, ${prefix}, ${suffix}, ${team}, ${team_color}, " +
                "${world} and ${datetime[:format]}")
        public String template =
                "[\"\",{\"text\":\"${player}\",\"color\":\"yellow\",\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"/tell ${username} \"},\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[\"\",{\"text\":\"Click to direct message\",\"italic\":true}]}},{\"text\":\" > \",\"color\":\"dark_gray\"},{\"text\":\"${message}\"}]";

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
     * Handles a reload of the configuration instance.
     *
     * @param holder registered config holder
     * @param config updated config instance
     * @return reload action result
     * @see ConfigHolder#load()
     */
    public static ActionResult reload(ConfigHolder<StylingConfig> holder, StylingConfig config)
    {
        // Clear all cached player chat styles
        ReceiveChatCallback.PLAYER_CACHE.clear();

        return ActionResult.PASS;
    }

    /**
     * Registers and prepares a new configuration instance.
     *
     * @return registered config holder
     * @see AutoConfig#register
     */
    public static ConfigHolder<StylingConfig> init()
    {
        // Register the config
        ConfigHolder<StylingConfig> holder = AutoConfig.register(StylingConfig.class, JanksonConfigSerializer::new);

        // Listen for when the server is reloading (i.e. /reload), and reload the config
        ServerLifecycleEvents.START_DATA_PACK_RELOAD.register((s, m) ->
                AutoConfig.getConfigHolder(StylingConfig.class).load());

        // Listen for when the config gets loaded
        holder.registerLoadListener(StylingConfig::reload);

        return holder;
    }
}
