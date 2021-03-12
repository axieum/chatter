package me.axieum.mcmod.chatter.mixin.discord;

import net.minecraft.util.crash.CrashReport;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.io.File;

@Mixin(CrashReport.class)
public interface CrashReportAccessor
{
    @Accessor(value = "file")
    File getFile();
}
