package yancey.openparticle.core.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @Inject(at = @At("TAIL"), method = "render")
    public void render(DrawContext context, float tickDelta, CallbackInfo info) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (!client.options.hudHidden && !client.getDebugHud().shouldShowDebugHud()) {
            String text = ((MinecraftClientAccessor) client).getCurrentFps() + " FPS";
            context.drawText(client.textRenderer, text, 5, 7, 0xFFFFFFFF, false);
        }
    }
}