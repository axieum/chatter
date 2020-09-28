package me.axieum.mcmod.chatter.impl.styling;

import me.axieum.mcmod.chatter.api.event.chat.ChatEvents;
import me.axieum.mcmod.chatter.impl.styling.callback.ReceiveChatCallback;
import me.axieum.mcmod.chatter.impl.styling.config.StylingConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatterStyling implements DedicatedServerModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger("Chatter|Styling");
    public static final StylingConfig CONFIG = StylingConfig.init();

    @Override
    public void onInitializeServer()
    {
        // Register listeners
        ChatEvents.RECEIVE_CHAT.register(new ReceiveChatCallback());
    }
}
