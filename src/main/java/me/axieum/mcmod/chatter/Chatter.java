package me.axieum.mcmod.chatter;

import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Chatter implements DedicatedServerModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger("Chatter");

    @Override
    public void onInitializeServer()
    {
        LOGGER.info("Getting ready...");
    }
}
