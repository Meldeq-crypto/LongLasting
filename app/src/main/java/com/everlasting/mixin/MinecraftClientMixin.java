package com.everlasting.mixin;

import com.everlasting.EverlastingFixes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow public Screen currentScreen;

    private long everlastingfixes$lastPauseStart = -1L;

    @Inject(method = "tick", at = @At("HEAD"))
    private void everlastingfixes$trackPauseMenu(CallbackInfo ci) {
        if (this.currentScreen != null && this.currentScreen.shouldPause()) {
            if (this.everlastingfixes$lastPauseStart == -1L) {
                this.everlastingfixes$lastPauseStart = System.currentTimeMillis();
            }
            long elapsed = System.currentTimeMillis() - this.everlastingfixes$lastPauseStart;
            if (elapsed > 30_000L) {
                EverlastingFixes.LOGGER.debug("Triggering GC after extended pause menu.");
                System.gc();
                this.everlastingfixes$lastPauseStart = System.currentTimeMillis();
            }
        } else {
            this.everlastingfixes$lastPauseStart = -1L;
        }
    }

    @Inject(method = "setScreen", at = @At("HEAD"))
    private void everlastingfixes$gcOnLoadingScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof DownloadingTerrainScreen) {
            EverlastingFixes.LOGGER.debug("Triggering GC during loading screen.");
            System.gc();
        }
    }
}
