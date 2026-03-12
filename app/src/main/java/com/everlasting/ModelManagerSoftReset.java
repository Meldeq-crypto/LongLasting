package com.everlasting;

import net.minecraft.client.MinecraftClient;

/**
 * ModelManagerSoftReset — REFACTORED.
 *
 * Previously called client.reloadResources() which triggered a full
 * resource reload screen. Now delegates to MemoryManager.silentPurge()
 * for invisible, targeted cache clearing with zero loading screens.
 *
 * @deprecated Use {@link MemoryManager#silentPurge(MinecraftClient)} directly.
 *             This class is retained only for backward compatibility.
 */
@Deprecated
public final class ModelManagerSoftReset {
    private ModelManagerSoftReset() {
    }

    /**
     * Perform a silent cache purge instead of a full resource reload.
     *
     * Old behavior (REMOVED):
     *   client.reloadResources() — caused loading screen glitch.
     *
     * New behavior:
     *   MemoryManager.silentPurge(client) — clears stale caches silently.
     */
    public static boolean softReset(MinecraftClient client) {
        if (client == null) {
            return false;
        }
        try {
            MemoryManager.silentPurge(client);
            return true;
        } catch (Exception ex) {
            EverlastingFixes.LOGGER.warn("Failed to perform silent cache purge.", ex);
            return false;
        }
    }
}
