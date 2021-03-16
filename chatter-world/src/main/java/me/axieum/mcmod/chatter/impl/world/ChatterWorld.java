package me.axieum.mcmod.chatter.impl.world;

import me.axieum.mcmod.chatter.impl.world.config.WorldGameRules;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatterWorld implements DedicatedServerModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger("Chatter|World");

    @Override
    public void onInitializeServer()
    {
        // Register game rules
        WorldGameRules.init();
    }
}
