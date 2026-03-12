package com.everlasting.mixin;

import com.everlasting.EverlastingFixes;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {

    @Inject(method = "tick", at = @At("TAIL"))
    private void everlastingfixes$onTick(CallbackInfo ci) {
        // Lightweight hook — heavy entity purging removed because
        // 1.21.1 refactored entity storage away from Int2ObjectMap.
        // Future: re-implement via EntityLookup API if needed.
    }
}
