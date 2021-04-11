package me.axieum.mcmod.chatter.api.styling;

import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class ChatStyleProvider
{
    private static ChatStyleProvider provider = null;

    /**
     * Returns the provider used for styling.
     *
     * @return chat style provider
     */
    public static ChatStyleProvider getProvider()
    {
        return provider;
    }

    /**
     * Sets the provider to be used for styling.
     *
     * @param provider chat style provider
     */
    public static void useProvider(ChatStyleProvider provider)
    {
        ChatStyleProvider.provider = provider;
    }

    /**
     * Returns the username of the player.
     *
     * @param player player
     * @return player username
     */
    public @NotNull String getPlayer(ServerPlayerEntity player)
    {
        return player.getName().getString();
    }

    /**
     * Returns the display name of the player.
     *
     * @param player player
     * @return player name
     */
    public @NotNull String getPlayerName(ServerPlayerEntity player)
    {
        return player.getDisplayName().getString();
    }

    /**
     * Returns the identifier of the group that the player belongs to.
     *
     * @param player player
     * @return group identifier
     */
    public abstract @NotNull String getGroup(ServerPlayerEntity player);

    /**
     * Returns the display name of the group that the player belongs to.
     *
     * @param player player
     * @return group display name
     */
    public abstract @NotNull String getGroupName(ServerPlayerEntity player);

    /**
     * Returns the prefix for the player.
     *
     * @param player player
     * @return prefix text or null
     */
    public @Nullable String getPrefix(ServerPlayerEntity player)
    {
        return null;
    }

    /**
     * Returns the suffix for the player.
     *
     * @param player player
     * @return suffix text or null
     */
    public @Nullable String getSuffix(ServerPlayerEntity player)
    {
        return null;
    }

    /**
     * Transforms and returns the chat message.
     *
     * @param player  player
     * @param message message body
     * @return transformed message body
     */
    public @NotNull String getMessage(ServerPlayerEntity player, String message)
    {
        return message;
    }

    /**
     * Determines whether the given player can use chat formatting.
     *
     * @param player player
     * @return true if the player can use chat formatting codes
     */
    public boolean canUseColor(ServerPlayerEntity player)
    {
        return true;
    }
}
