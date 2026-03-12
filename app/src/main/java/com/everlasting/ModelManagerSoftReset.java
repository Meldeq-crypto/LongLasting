package com.everlasting;

import net.minecraft.client.MinecraftClient;
import java.util.concurrent.CompletableFuture;

public final class ModelManagerSoftReset {
    private ModelManagerSoftReset() {
    }

    public static boolean softReset(MinecraftClient client) {
        if (client == null) {
            return false;
        }
        try {
            CompletableFuture<Void> future = client.reloadResources();
            future.exceptionally(ex -> {
                EverlastingFixes.LOGGER.warn("Failed to soft reset ModelManager.", ex);
                return null;
            });
            return true;
        } catch (Exception ex) {
            EverlastingFixes.LOGGER.warn("Failed to soft reset ModelManager.", ex);
            return false;
        }
    }
}
