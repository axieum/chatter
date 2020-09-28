package me.axieum.mcmod.chatter.impl.filtering;

import me.axieum.mcmod.chatter.impl.filtering.config.FilteringConfig;
import net.fabricmc.api.DedicatedServerModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ChatterFiltering implements DedicatedServerModInitializer
{
    public static final Logger LOGGER = LogManager.getLogger("Chatter|Filtering");
    public static final FilteringConfig CONFIG = FilteringConfig.init();

    @Override
    public void onInitializeServer()
    {
        //
    }
}
