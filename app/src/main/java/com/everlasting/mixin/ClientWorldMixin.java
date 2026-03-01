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
    @Inject(method = "removeEntity", at = @At("HEAD"))
    private void everlastingfixes$preRemoveEntity(int id, Entity.RemovalReason reason, CallbackInfo ci) {
        EverlastingFixes.LOGGER.debug("Removing client entity {} with reason {}", id, reason);
    }
}
