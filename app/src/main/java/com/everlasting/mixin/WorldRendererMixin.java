package com.everlasting.mixin;

import com.everlasting.EverlastingFixes;
import com.everlasting.SessionHealthManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(method = "setWorld", at = @At("HEAD"))
    private void everlastingfixes$clearRenderDataOnWorldSwap(ClientWorld world, CallbackInfo ci) {
        try {
            EverlastingFixes.LOGGER.debug("World swapped; renderer state will be rebuilt.");
            if (world != null) {
                SessionHealthManager.onWorldChanged(world, net.minecraft.client.MinecraftClient.getInstance());
            }
        } catch (Exception e) {
            EverlastingFixes.LOGGER.warn("Error in world swap hook", e);
        }
    }
}
