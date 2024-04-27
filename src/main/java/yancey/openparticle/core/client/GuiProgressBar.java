package yancey.openparticle.core.client;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.math.RoundingMode;
import java.text.NumberFormat;

@Environment(EnvType.CLIENT)
public class GuiProgressBar extends Screen {
    public static final Logger logger = LogUtils.getLogger();
    public static final NumberFormat numberFormat;

    static {
        numberFormat = NumberFormat.getNumberInstance();
        numberFormat.setMinimumFractionDigits(2);
        numberFormat.setMaximumFractionDigits(2);
        numberFormat.setRoundingMode(RoundingMode.HALF_UP);
        numberFormat.setGroupingUsed(false);
    }

    private final Text title;
    private final int max;
    public int progress;

    public GuiProgressBar(String title, int max) {
        super(ScreenTexts.EMPTY);
        this.title = Text.literal(title);
        this.max = max;
        progress = 0;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - textRenderer.fontHeight - 20, 0xFFFFFFFF);
        if (progress > max || progress < 0) {
            logger.warn("progress bar can't show,the max progress is {},but the progress {}", max, progress);
            close();
            return;
        }
        float a = progress * 100f / max;
        drawProgress(
                context,
                width / 2 - 100,
                width / 2 - 100 + (int) a * 2,
                width / 2 + 100,
                (height - 10) / 2,
                (height + 10) / 2
        );
        drawString(context, numberFormat.format(a) + '%', width / 2 - 100, height / 2 + 20);
        String b = String.valueOf(progress) + '/' + max;
        drawString(context, b, width / 2 + 100 - textRenderer.getWidth(b), height / 2 + 20);
        if (progress == max) {
            close();
        }
    }

    public void drawString(@NotNull DrawContext context, String text, int x, int y) {
        context.drawText(textRenderer, text, x, y, 0xFFFFFFFF, true);
    }

    private void drawProgress(DrawContext context, int startX, int middleX, int endX, int startY, int endY) {
        context.fill(startX - 1, startY - 1, middleX, endY + 1, 0xFFFFFFFF);//left
        context.fill(middleX, startY - 1, endX + 1, startY, 0xFFFFFFFF);//top
        context.fill(endX, startY - 1, endX + 1, endY + 1, 0xFFFFFFFF);//right
        context.fill(middleX, endY, endX + 1, endY + 1, 0xFFFFFFFF);//bottom
    }
}
