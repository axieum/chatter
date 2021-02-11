package me.axieum.mcmod.chatter.impl.discord.util;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class ServerUtils
{
    // Captured Minecraft server instance
    public static @Nullable MinecraftServer INSTANCE;

    /**
     * Returns the captured Minecraft server instance.
     *
     * @return current Minecraft server instance if captured
     */
    public static Optional<MinecraftServer> getInstance()
    {
        return Optional.ofNullable(INSTANCE);
    }

    /**
     * Determines whether the server has finished loading.
     *
     * @return true if the server is loaded
     */
    public static boolean isReady()
    {
        return INSTANCE != null && INSTANCE.getTickTime() > 0;
    }
}
