package com.everlasting;

import net.minecraft.client.MinecraftClient;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * MemoryManager — Silent Background Memory Management.
 *
 * Uses async garbage collection to reclaim memory without visible
 * loading screens or frame freezes. Avoids clearing Minecraft's
 * internal model/texture caches (which would break rendering).
 *
 * Key design:
 *  - No calls to MinecraftClient.reloadResources()
 *  - No clearing of model or texture caches (unsafe — breaks rendering)
 *  - A background timer runs every 15 minutes to trigger async GC
 *  - Pause-menu GC is also available (when Esc menu is open)
 *  - System.gc() is always invoked asynchronously to prevent frame freezes
 */
public final class MemoryManager {

    /** Interval for the automatic background GC timer. */
    private static final long GC_INTERVAL_MINUTES = 15L;

    /** Minimum time (ms) between pause-menu GC triggers. */
    private static final long PAUSE_GC_COOLDOWN_MS = 10L * 60L * 1000L; // 10 minutes

    /** Guard to prevent overlapping async GC calls. */
    private static final AtomicBoolean gcInFlight = new AtomicBoolean(false);

    /** Daemon scheduler for the background GC timer. */
    private static ScheduledExecutorService scheduler;

    /** Timestamp of the last GC trigger. */
    private static volatile long lastGcMs = System.currentTimeMillis();

    private MemoryManager() {
    }

    // ── Lifecycle ──────────────────────────────────────────────────────

    /**
     * Start the background GC timer. Called once from mod initializer.
     */
    public static void start() {
        if (scheduler != null && !scheduler.isShutdown()) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "EverlastingFixes-MemoryManager");
            t.setDaemon(true);
            return t;
        });
        scheduler.scheduleAtFixedRate(() -> {
            try {
                triggerAsyncGc("background-timer");
            } catch (Exception e) {
                EverlastingFixes.LOGGER.warn("Background GC timer failed.", e);
            }
        }, GC_INTERVAL_MINUTES, GC_INTERVAL_MINUTES, TimeUnit.MINUTES);
        EverlastingFixes.LOGGER.info("MemoryManager started — async GC every {} min.", GC_INTERVAL_MINUTES);
    }

    /**
     * Stop the background timer (e.g. on mod unload).
     */
    public static void stop() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    // ── Silent Purge ───────────────────────────────────────────────────

    /**
     * Perform a silent memory reclamation. This triggers an async GC
     * to reclaim unused memory without touching Minecraft's internal
     * caches (which would break rendering).
     *
     * Must NOT clear model/texture caches — Minecraft does not re-bake
     * models on demand and clearing them causes invisible blocks/crashes.
     */
    public static void silentPurge(MinecraftClient client) {
        if (client == null) {
            return;
        }
        triggerAsyncGc("silent-purge");
    }

    // ── Async Garbage Collection ───────────────────────────────────────

    /**
     * Request a garbage collection on a background thread.
     * This prevents the GC pause from appearing as a frame freeze
     * or loading screen artifact.
     */
    public static void triggerAsyncGc(String reason) {
        if (!gcInFlight.compareAndSet(false, true)) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                EverlastingFixes.LOGGER.debug("Async GC triggered: {}", reason);
                System.gc();
                lastGcMs = System.currentTimeMillis();
            } finally {
                gcInFlight.set(false);
            }
        });
    }

    // ── Pause Menu GC ──────────────────────────────────────────────────

    /**
     * Called when the player has the pause (Esc) menu open for an extended
     * period. Good opportunity for async GC since the player is idle.
     */
    public static void onPauseMenuIdle(MinecraftClient client) {
        long sinceLastGc = System.currentTimeMillis() - lastGcMs;
        if (sinceLastGc >= PAUSE_GC_COOLDOWN_MS) {
            EverlastingFixes.LOGGER.debug("Pause-menu idle GC (last GC was {} ms ago).", sinceLastGc);
            triggerAsyncGc("pause-menu-idle");
        }
    }

    // ── Diagnostics ────────────────────────────────────────────────────

    /**
     * Get time since last GC in milliseconds.
     */
    public static long getTimeSinceLastGcMs() {
        return System.currentTimeMillis() - lastGcMs;
    }
}
