package me.axieum.mcmod.chatter.impl.styling.integration;

import me.axieum.mcmod.chatter.api.styling.ChatStyleProvider;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class MinecraftStyle extends ChatStyleProvider
{
    @Override
    public @NotNull String getPlayerName(ServerPlayerEntity player)
    {
        // Get the player name
        String name = super.getPlayerName(player);
        // Check if there a team associated, and get its colour
        Formatting color = Optional.ofNullable(player.getScoreboardTeam())
                                   .map(Team.class::cast) // the AbstractTeam#getColor is client-only
                                   .map(Team::getColor)
                                   .orElse(null);
        // Combine the team colour with the display name
        return color != null ? color.toString() + name : name;
    }

    public @NotNull String getGroupName(ServerPlayerEntity player)
    {
        // Returns "Operator" if they are a server operator, else "Player"
        return player.hasPermissionLevel(2) ? "Operator" : "Player";
    }

    public @NotNull String getGroup(ServerPlayerEntity player)
    {
        // Returns "operator" if they are a server operator, else "player"
        return player.hasPermissionLevel(2) ? "operator" : "player";
    }
}
