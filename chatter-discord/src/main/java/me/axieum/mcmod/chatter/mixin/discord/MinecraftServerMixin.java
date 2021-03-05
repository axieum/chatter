package me.axieum.mcmod.chatter.mixin.discord;

import me.axieum.mcmod.chatter.impl.discord.util.ServerUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.crash.CrashReport;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Injects into, and captures any server crash reports.
 */
@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin
{
    @Inject(method = "setCrashReport", at = @At("TAIL"))
    private void setCrashReport(@Nullable CrashReport crashReport, CallbackInfo info)
    {
        ServerUtils.CRASH_REPORT = crashReport;
    }
}
