package com.everlasting.mixin;

import com.everlasting.SessionHealthManager;
import net.minecraft.client.gui.hud.DebugHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
    @Inject(method = "getRightText", at = @At("TAIL"))
    private void everlastingfixes$appendSessionHealth(CallbackInfoReturnable<List<String>> cir) {
        List<String> lines = cir.getReturnValue();
        if (lines != null) {
            lines.add("Session Health: " + SessionHealthManager.getSessionHealthLabel());
        }
    }
}
