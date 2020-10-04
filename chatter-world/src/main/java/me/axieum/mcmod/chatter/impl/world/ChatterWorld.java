package me.axieum.mcmod.chatter.impl.world;

import me.axieum.mcmod.chatter.impl.world.config.WorldConfig;
import me.axieum.mcmod.chatter.impl.world.config.WorldGameRules;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatterWorld implements DedicatedServerModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger("Chatter|World");
    public static final WorldConfig CONFIG = WorldConfig.init();

    @Override
    public void onInitializeServer()
    {
        // Register game rules
        WorldGameRules.init();
    }
}
