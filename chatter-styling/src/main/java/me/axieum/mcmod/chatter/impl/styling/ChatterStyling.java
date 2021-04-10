package me.axieum.mcmod.chatter.impl.styling;

import me.axieum.mcmod.chatter.api.event.chat.ChatEvents;
import me.axieum.mcmod.chatter.api.styling.ChatStyleProvider;
import me.axieum.mcmod.chatter.impl.styling.callback.ReceiveChatCallback;
import me.axieum.mcmod.chatter.impl.styling.config.StylingConfig;
import me.axieum.mcmod.chatter.impl.styling.integration.LuckPermsStyle;
import me.axieum.mcmod.chatter.impl.styling.integration.MinecraftStyle;
import me.shedaniel.autoconfig.ConfigHolder;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatterStyling implements DedicatedServerModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger("Chatter|Styling");
    public static final ConfigHolder<StylingConfig> CONFIG = StylingConfig.init();

    @Override
    public void onInitializeServer()
    {
        LOGGER.info("Registered Chatter add-on 'Chatter Styling' - Change how players see each other's messages");

        // Set the chat style provider
        if (FabricLoader.getInstance().isModLoaded("luckperms")) {
            // LuckPerms
            LOGGER.info("Integrating with LuckPerms for chat styling");
            ChatStyleProvider.useProvider(new LuckPermsStyle());
        } else {
            // Minecraft/Vanilla
            ChatStyleProvider.useProvider(new MinecraftStyle());
        }

        // Register listeners
        ChatEvents.RECEIVE_CHAT.register(new ReceiveChatCallback());
    }

    /**
     * Returns the config instance.
     *
     * @return config instance
     * @see ConfigHolder#getConfig()
     */
    public static StylingConfig getConfig()
    {
        return CONFIG.getConfig();
    }
}
