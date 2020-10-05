package me.axieum.mcmod.chatter.mixin.styling;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends Entity
{
    @Shadow
    @Final
    private GameProfile gameProfile;

    public PlayerEntityMixin(EntityType<?> type, World world) { super(type, world); }

    /**
     * Overwrites player name retrieval to take into account custom entity names.
     *
     * @author Axieum
     */
    @Overwrite
    public Text getName()
    {
        return this.hasCustomName() ? this.getCustomName() : new LiteralText(this.gameProfile.getName());
    }
}
