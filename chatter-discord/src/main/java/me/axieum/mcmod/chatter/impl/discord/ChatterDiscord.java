package me.axieum.mcmod.chatter.impl.discord;

import me.axieum.mcmod.chatter.impl.discord.config.DiscordConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatterDiscord implements DedicatedServerModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger("Chatter|Discord");
    public static final DiscordConfig CONFIG = DiscordConfig.init();

    @Override
    public void onInitializeServer() {}
}
