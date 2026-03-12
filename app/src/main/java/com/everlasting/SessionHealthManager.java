package com.everlasting;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.world.ClientWorld;

// Entity purge methods removed — 1.21.1 refactored client entity storage
// away from Int2ObjectMap, so direct lookup/purge is no longer viable.

/**
 * SessionHealthManager — REFACTORED.
 *
 * Changes:
 *  - Removed all calls to ModelManagerSoftReset.softReset() (which triggered reloadResources()).
 *  - Deep flush now delegates to MemoryManager.silentPurge() — zero loading screens.
 *  - All System.gc() calls are now async via MemoryManager.triggerAsyncGc().
 *  - The "memory flush" is no longer tied to screen-open events (right-click interaction).
 *    Instead, purging is handled by MemoryManager's 15-minute background timer
 *    and the pause-menu idle hook.
 */
public final class SessionHealthManager {
    private static final long SESSION_START_MS = System.currentTimeMillis();
    private static final long ONE_HOUR_MS = 60L * 60L * 1000L;
    private static final long TWO_HOURS_MS = 2L * ONE_HOUR_MS;
    private static final long THIRTY_MINUTES_MS = 30L * 60L * 1000L;
    private static final long TEN_MINUTES_MS = 10L * 60L * 1000L;
    private static final long GC_PAUSE_THRESHOLD_MS = 500L;

    private static long lastTickNs = System.nanoTime();
    private static volatile long lastLongPauseMs = -1L;
    private static volatile boolean needsDeepFlush = false;
    private static long lastScreenGcMs = -1L;
    private static long lastEntityPurgeMs = -1L;
    private static long lastDimensionEnterMs = SESSION_START_MS;
    private static String lastDimensionId = null;

    private SessionHealthManager() {
    }

    public static void onClientTick() {
        long nowNs = System.nanoTime();
        long deltaMs = (nowNs - lastTickNs) / 1_000_000L;
        lastTickNs = nowNs;
        if (deltaMs > GC_PAUSE_THRESHOLD_MS) {
            lastLongPauseMs = System.currentTimeMillis();
            needsDeepFlush = true;
        }
    }

    public static void onWorldChanged(ClientWorld world, MinecraftClient client) {
        if (world == null) {
            return;
        }
        String dimensionId = world.getRegistryKey().getValue().toString();
        long now = System.currentTimeMillis();
        if (lastDimensionId != null && !lastDimensionId.equals(dimensionId)) {
            long durationMs = now - lastDimensionEnterMs;
            if (durationMs >= TWO_HOURS_MS) {
                // REFACTORED: Use silent purge instead of reloadResources()
                MemoryManager.silentPurge(client);
                EverlastingFixes.LOGGER.debug("Silent purge after {} ms in {}.",
                        durationMs, lastDimensionId);
            }
        }
        lastDimensionId = dimensionId;
        lastDimensionEnterMs = now;
    }

    /**
     * Called when a screen is opened. REFACTORED: No longer triggers a
     * model reload or deep flush on screen open. The memory flush logic
     * is now handled by the MemoryManager background timer and pause-menu
     * idle hook, not by opening inventory/chest screens (right-click).
     */
    public static void onScreenOpened(Screen screen, MinecraftClient client) {
        // Deep flush on screen open is REMOVED to prevent the loading screen
        // glitch that occurred when right-clicking to open a container.
        //
        // The old behavior:
        //   if (needsDeepFlush) { performDeepFlush(client); }
        //   if (session >= 1hr && ...) { triggerBackgroundGc(); }
        //
        // This is now handled by:
        //   - MemoryManager's 15-minute background timer
        //   - MemoryManager.onPauseMenuIdle() when Esc menu is open
        //
        // We still check for deep flush, but only trigger an async GC, NOT a reload.
        if (needsDeepFlush) {
            needsDeepFlush = false;
            MemoryManager.triggerAsyncGc("deep-flush-deferred");
        }
    }

    public static boolean shouldRunEntityPurge() {
        long now = System.currentTimeMillis();
        return now - lastEntityPurgeMs >= THIRTY_MINUTES_MS;
    }

    public static void markEntityPurge() {
        lastEntityPurgeMs = System.currentTimeMillis();
    }

    public static String getSessionHealthLabel() {
        long now = System.currentTimeMillis();
        if (needsDeepFlush) {
            return "Degraded";
        }
        if (lastLongPauseMs > 0 && now - lastLongPauseMs <= TEN_MINUTES_MS) {
            return "Degraded";
        }
        return "Stable";
    }

    private static long getSessionDurationMs() {
        return System.currentTimeMillis() - SESSION_START_MS;
    }
}
