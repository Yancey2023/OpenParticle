package yancey.openparticle.core.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yancey.openparticle.core.OpenParticle;
import yancey.openparticle.core.core.OpenParticleCore;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(at = @At("TAIL"), method = "render")
    public void render(DrawContext context, float tickDelta, CallbackInfo info) {
        if (OpenParticle.isDebug) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!client.options.hudHidden && !client.getDebugHud().shouldShowDebugHud()) {
                String textFPS = "FPS: " + ((MinecraftClientAccessor) client).getCurrentFps();
                context.drawText(client.textRenderer, textFPS, 5, 7, 0xFFFFFFFF, false);
                String textParticleCount = "P: " + OpenParticleCore.getParticleSize();
                context.drawText(client.textRenderer, textParticleCount, 5, 7 + client.textRenderer.fontHeight, 0xFFFFFFFF, false);
            }
        }
    }
}