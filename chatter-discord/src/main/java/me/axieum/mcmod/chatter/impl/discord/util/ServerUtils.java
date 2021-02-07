package me.axieum.mcmod.chatter.impl.discord.util;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.UUID;

public final class ServerUtils
{
    public static final HashMap<UUID, Long> PLAYER_LOGINS = new HashMap<>();
    public static final HashMap<Identifier, String> WORLD_NAMES = new HashMap<>(3);

    /**
     * Computes the duration in milliseconds since the player logged in.
     *
     * @param player player
     * @return milliseconds since login or 0 if unavailable
     */
    public static long getPlayerElapsed(ServerPlayerEntity player)
    {
        final Long login = PLAYER_LOGINS.get(player.getUuid());
        return login != null ? System.currentTimeMillis() - login : 0;
    }

    /**
     * Computes, caches and returns the name of the given world.
     *
     * @param world Minecraft world/dimension
     * @return name of the given world
     */
    public static String getWorldName(World world)
    {
        // Attempt to retrieve the name from cache, otherwise compute it once!
        // NB: At present, the world name is not stored in any resources, apart
        // from in the identifier (e.g. 'the_nether'), good enough but hacky :(
        return WORLD_NAMES.computeIfAbsent(world.getRegistryKey().getValue(), identifier -> {
            // Space delimited identifier path, with leading 'the' keywords removed
            final String path = identifier.getPath().replace('_', ' ').replaceFirst("(?i)the\\s", "");
            // Capitalise the first character in each word
            char[] chars = path.toCharArray();
            boolean capitalizeNext = true;
            for (int i = 0; i < chars.length; i++) {
                if (chars[i] == ' ') {
                    capitalizeNext = true;
                } else if (capitalizeNext) {
                    chars[i] = Character.toTitleCase(chars[i]);
                    capitalizeNext = false;
                }
            }
            // Return the computed world name
            return new String(chars);
        });
    }
}
