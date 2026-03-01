package com.everlasting.mixin;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Frustum;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.client.render.VertexConsumerProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntityRenderDispatcher.class)
public abstract class BlockEntityRenderDispatcherMixin {
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private <T extends BlockEntity> void everlastingfixes$frustumCull(
            T blockEntity,
            float tickDelta,
            MatrixStack matrices,
            VertexConsumerProvider vertexConsumers,
            CallbackInfo ci) {
        if (blockEntity.getWorld() == null) {
            return;
        }
        WorldRenderer worldRenderer = MinecraftClient.getInstance().worldRenderer;
        if (worldRenderer == null) {
            return;
        }
        Frustum frustum = ((WorldRendererAccessor) worldRenderer).everlastingfixes$getFrustum();
        if (frustum == null) {
            return;
        }
        BlockPos pos = blockEntity.getPos();
        Box box = new Box(pos).expand(0.5);
        if (!frustum.isVisible(box)) {
            ci.cancel();
        }
    }
}
