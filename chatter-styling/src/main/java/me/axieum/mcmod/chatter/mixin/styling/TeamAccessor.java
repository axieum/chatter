package me.axieum.mcmod.chatter.mixin.styling;

import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Team.class)
public interface TeamAccessor
{
    @Accessor(value = "color")
    Formatting getColor();
}
