package me.axieum.mcmod.chatter.impl.discord.util;

import net.minecraft.server.MinecraftServer;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.stream.LongStream;

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
     * Determines whether a Minecraft server has finished loading.
     *
     * @param server minecraft server
     * @return true if the given server is loaded
     */
    public static boolean isReady(@Nullable MinecraftServer server)
    {
        return server != null && server.getTickTime() > 0;
    }

    /**
     * Computes a server's average TPS.
     *
     * @param server Minecraft server
     * @return average ticks per second (upper bound of 20)
     */
    public static double getAverageTPS(MinecraftServer server)
    {
        return Math.min(1000f / getAverageTPSTime(server), 20);
    }

    /**
     * Computes a server's average TPS time.
     *
     * @param server Minecraft server
     * @return average tick time in milliseconds
     */
    public static double getAverageTPSTime(MinecraftServer server)
    {
        // Average the last tick lengths, and convert from nanoseconds to milliseconds
        return LongStream.of(server.lastTickLengths).average().orElse(0) * 1e-6d;
    }
}
