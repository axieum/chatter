package me.axieum.mcmod.chatter.impl.world.config;

import net.fabricmc.fabric.api.gamerule.v1.GameRuleFactory;
import net.minecraft.world.GameRules;

import static me.axieum.mcmod.chatter.impl.world.ChatterWorld.LOGGER;
import static net.fabricmc.fabric.api.gamerule.v1.GameRuleRegistry.register;

public class WorldGameRules
{
    /**
     * True if named animal/monster (with name tag) death messages are shown.
     */
    public static final GameRules.Key<GameRules.BooleanRule> SHOW_NAMED_ENTITY_DEATH_MESSAGES =
            register("showNamedEntityDeathMessages", GameRules.Category.CHAT, GameRuleFactory.createBooleanRule(true));

    /**
     * Registers and asserts the addition of custom game rules.
     */
    public static void init()
    {
        LOGGER.info("Registered game rule: {}", SHOW_NAMED_ENTITY_DEATH_MESSAGES.getName());
    }
}
