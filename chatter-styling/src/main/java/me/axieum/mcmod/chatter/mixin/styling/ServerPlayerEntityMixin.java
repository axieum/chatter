package me.axieum.mcmod.chatter.mixin.styling;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends Entity
{
    public ServerPlayerEntityMixin(EntityType<?> type, World world) { super(type, world); }

    /**
     * Overwrites player list name retrieval to take into account custom entity names.
     *
     * @author Axieum
     */
    @Overwrite
    @Nullable
    public Text getPlayerListName()
    {
        return this.hasCustomName() ? this.getCustomName() : null;
    }
}
