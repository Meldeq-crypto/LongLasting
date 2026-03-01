package com.everlasting.mixin;

import com.everlasting.EverlastingFixes;
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
        EverlastingFixes.LOGGER.debug("World swapped; renderer state will be rebuilt.");
    }
}
