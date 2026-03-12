package com.everlasting;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.world.ClientWorld;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

// Entity purge methods removed — 1.21.1 refactored client entity storage
// away from Int2ObjectMap, so direct lookup/purge is no longer viable.

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
    private static final AtomicBoolean backgroundGcInFlight = new AtomicBoolean(false);

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
                boolean cleared = ModelManagerSoftReset.softReset(client);
                EverlastingFixes.LOGGER.debug("Soft reset model cache after {} ms in {}. Cleared: {}",
                        durationMs, lastDimensionId, cleared);
            }
        }
        lastDimensionId = dimensionId;
        lastDimensionEnterMs = now;
    }

    public static void onScreenOpened(Screen screen, MinecraftClient client) {
        if (!(screen instanceof HandledScreen<?>)) {
            return;
        }
        long now = System.currentTimeMillis();

        if (needsDeepFlush) {
            needsDeepFlush = false;
            performDeepFlush(client);
        }

        if (getSessionDurationMs() >= ONE_HOUR_MS && now - lastScreenGcMs >= THIRTY_MINUTES_MS) {
            lastScreenGcMs = now;
            triggerBackgroundGc("screen-open");
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

    private static void performDeepFlush(MinecraftClient client) {
        EverlastingFixes.LOGGER.debug("Performing deep flush after GC pause.");
        ModelManagerSoftReset.softReset(client);
        triggerBackgroundGc("deep-flush");
    }

    private static void triggerBackgroundGc(String reason) {
        if (!backgroundGcInFlight.compareAndSet(false, true)) {
            return;
        }
        CompletableFuture.runAsync(() -> {
            try {
                EverlastingFixes.LOGGER.debug("Triggering background GC: {}", reason);
                System.gc();
            } finally {
                backgroundGcInFlight.set(false);
            }
        });
    }
}
