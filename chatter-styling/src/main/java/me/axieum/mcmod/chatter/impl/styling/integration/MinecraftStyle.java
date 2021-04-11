package me.axieum.mcmod.chatter.impl.styling.integration;

import me.axieum.mcmod.chatter.api.styling.ChatStyleProvider;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MinecraftStyle extends ChatStyleProvider
{
    public @NotNull String getGroup(ServerPlayerEntity player)
    {
        // Returns "operator" if they are a server operator, else "player"
        return player.hasPermissionLevel(2) ? "operator" : "player";
    }

    public @NotNull String getGroupName(ServerPlayerEntity player)
    {
        // Returns "Operator" if they are a server operator, else "Player"
        return player.hasPermissionLevel(2) ? "Operator" : "Player";
    }

    @Override
    public @Nullable String getPrefix(ServerPlayerEntity player)
    {
        // Return the player's team prefix, if present
        return player.getScoreboardTeam() != null ? ((Team) player.getScoreboardTeam()).getPrefix().getString()
                                                  : null;
    }

    @Override
    public @Nullable String getSuffix(ServerPlayerEntity player)
    {
        // Return the player's team suffix, if present
        return player.getScoreboardTeam() != null ? ((Team) player.getScoreboardTeam()).getSuffix().getString()
                                                  : null;
    }
}
