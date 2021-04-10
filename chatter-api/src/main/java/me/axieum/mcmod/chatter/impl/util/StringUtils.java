package me.axieum.mcmod.chatter.impl.util;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.HashMap;

public class StringUtils
{
    // Mapping of Minecraft world identifiers to their human-readable names
    public static final HashMap<Identifier, String> WORLD_NAMES = new HashMap<>(3);

    /**
     * Returns the identifier value of the given world.
     *
     * @param world Minecraft world/dimension
     * @return identifier of the given world, e.g. minecraft:overworld
     */
    public static String getWorldId(World world)
    {
        return world.getRegistryKey().getValue().toString();
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

    /**
     * Converts bytes to a human readable string, base 10.
     *
     * @param bytes number of bytes
     * @return human readable bytes in base 10
     */
    public static String bytesToHuman(long bytes)
    {
        if (-1000 < bytes && bytes < 1000) return bytes + " B";
        CharacterIterator ci = new StringCharacterIterator("kMGTPE");
        while (bytes <= -999_950 || bytes >= 999_950) {
            bytes /= 1000;
            ci.next();
        }
        return String.format("%.1f %cB", bytes / 1000.0, ci.current());
    }
}
