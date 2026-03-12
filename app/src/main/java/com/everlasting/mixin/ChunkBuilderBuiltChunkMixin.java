package com.everlasting.mixin;

import com.everlasting.EverlastingFixes;
import net.minecraft.client.render.chunk.ChunkBuilder.BuiltChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BuiltChunk.class)
public abstract class ChunkBuilderBuiltChunkMixin {

    @Inject(method = "delete", at = @At("HEAD"), require = 0)
    private void everlastingfixes$logChunkDelete(CallbackInfo ci) {
        // In 1.21.1 vertex buffers are stored per-RenderLayer in a Map,
        // not as a single field. The base delete() already closes them,
        // so we only log here to help debug leaks.
        EverlastingFixes.LOGGER.debug("BuiltChunk.delete() called — vertex buffers will be released.");
    }
}
