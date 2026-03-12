package com.everlasting.mixin;

import com.everlasting.EverlastingFixes;
import com.everlasting.MemoryManager;
import com.everlasting.SessionHealthManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.DownloadingTerrainScreen;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * MinecraftClientMixin — REFACTORED.
 *
 * Changes:
 *  - Removed ALL synchronous System.gc() calls that caused frame freezes.
 *  - Pause menu now triggers MemoryManager.onPauseMenuIdle() for silent
 *    cache purging instead of raw GC.
 *  - Loading screen hook now uses async GC via MemoryManager.triggerAsyncGc().
 */
@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
    @Shadow public Screen currentScreen;

    private long everlastingfixes$lastPauseStart = -1L;

    /**
     * Tick hook: tracks pause-menu duration and delegates to
     * MemoryManager for idle purge when the Esc menu has been open
     * for an extended period.
     *
     * REFACTORED: No longer calls System.gc() synchronously.
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void everlastingfixes$trackPauseMenu(CallbackInfo ci) {
        SessionHealthManager.onClientTick();
        if (this.currentScreen != null && this.currentScreen.shouldPause()) {
            if (this.everlastingfixes$lastPauseStart == -1L) {
                this.everlastingfixes$lastPauseStart = System.currentTimeMillis();
            }
            long elapsed = System.currentTimeMillis() - this.everlastingfixes$lastPauseStart;
            if (elapsed > 30_000L) {
                // REFACTORED: Use silent purge + async GC instead of synchronous System.gc()
                EverlastingFixes.LOGGER.debug("Triggering silent purge after extended pause menu.");
                MemoryManager.onPauseMenuIdle((MinecraftClient) (Object) this);
                this.everlastingfixes$lastPauseStart = System.currentTimeMillis();
            }
        } else {
            this.everlastingfixes$lastPauseStart = -1L;
        }
    }

    /**
     * Screen-set hook: handles loading screen transitions and screen-open events.
     *
     * REFACTORED: Loading screen (DownloadingTerrainScreen) now uses async GC
     * instead of synchronous System.gc() to prevent the appearance of a freeze.
     */
    @Inject(method = "setScreen", at = @At("HEAD"))
    private void everlastingfixes$gcOnLoadingScreen(Screen screen, CallbackInfo ci) {
        if (screen instanceof DownloadingTerrainScreen) {
            // REFACTORED: Async GC instead of synchronous System.gc()
            EverlastingFixes.LOGGER.debug("Triggering async GC during loading screen.");
            MemoryManager.triggerAsyncGc("loading-screen");
        }
        SessionHealthManager.onScreenOpened(screen, (MinecraftClient) (Object) this);
    }
}
