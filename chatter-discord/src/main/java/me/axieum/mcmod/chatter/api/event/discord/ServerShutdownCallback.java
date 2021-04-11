package me.axieum.mcmod.chatter.api.event.discord;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.crash.CrashReport;
import org.jetbrains.annotations.Nullable;

public interface ServerShutdownCallback
{
    /**
     * Called when the server exits, be it gracefully or not.
     */
    Event<ServerShutdownCallback> EVENT =
            EventFactory.createArrayBacked(ServerShutdownCallback.class, callbacks -> (server, crashReport) -> {
                for (ServerShutdownCallback callback : callbacks)
                    callback.onServerShutdown(server, crashReport);
            });

    /**
     * Called when the server exits, be it gracefully or not.
     *
     * @param server      Minecraft server instance
     * @param crashReport a crash report if the server crashed
     */
    void onServerShutdown(MinecraftServer server, @Nullable CrashReport crashReport);
}
