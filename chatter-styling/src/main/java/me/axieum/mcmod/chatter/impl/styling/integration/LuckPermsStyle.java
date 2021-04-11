package me.axieum.mcmod.chatter.impl.styling.integration;

import me.axieum.mcmod.chatter.api.styling.ChatStyleProvider;
import me.axieum.mcmod.chatter.impl.styling.callback.ReceiveChatCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.event.node.NodeAddEvent;
import net.luckperms.api.event.node.NodeRemoveEvent;
import net.luckperms.api.model.PermissionHolder;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.types.InheritanceNode;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class LuckPermsStyle extends ChatStyleProvider
{
    /**
     * Constructs a new LuckPerms chat style provider.
     */
    public LuckPermsStyle()
    {
        // On server start, begin listening to permission changes to update our cache
        ServerLifecycleEvents.SERVER_STARTING.register((server) -> {
            LuckPermsProvider.get().getEventBus().subscribe(NodeAddEvent.class, LuckPermsStyle::onGroupAdd);
            LuckPermsProvider.get().getEventBus().subscribe(NodeRemoveEvent.class, LuckPermsStyle::onGroupRemove);
        });
    }

    public @NotNull String getGroup(ServerPlayerEntity player)
    {
        return getUser(player).getPrimaryGroup();
    }

    @Override
    public @NotNull String getGroupName(ServerPlayerEntity player)
    {
        final String name = getGroup(player);
        return Optional.ofNullable(LuckPermsProvider.get().getGroupManager().getGroup(name))
                       .map(PermissionHolder::getFriendlyName)
                       .orElse(name);
    }

    @Override
    public @Nullable String getPrefix(ServerPlayerEntity player)
    {
        return getUser(player).getCachedData().getMetaData().getPrefix();
    }

    @Override
    public @Nullable String getSuffix(ServerPlayerEntity player)
    {
        return getUser(player).getCachedData().getMetaData().getSuffix();
    }

    /**
     * Finds and returns the LuckPerms User for the given player.
     *
     * @param player player instance
     * @return LuckPerms user
     */
    public static User getUser(ServerPlayerEntity player)
    {
        return LuckPermsProvider.get().getPlayerAdapter(ServerPlayerEntity.class).getUser(player);
    }

    /**
     * On addition of a new group to a player, bust the style cache.
     *
     * @param e node add event
     */
    private static void onGroupAdd(NodeAddEvent e)
    {
        if (e.isUser() && e.getNode() instanceof InheritanceNode)
            ReceiveChatCallback.PLAYER_CACHE.remove(((User) e.getTarget()).getUniqueId());
    }

    /**
     * On removal of a new group from a player, bust the style cache.
     *
     * @param e node remove event
     */
    private static void onGroupRemove(NodeRemoveEvent e)
    {
        if (e.isUser() && e.getNode() instanceof InheritanceNode)
            ReceiveChatCallback.PLAYER_CACHE.remove(((User) e.getTarget()).getUniqueId());
    }
}
