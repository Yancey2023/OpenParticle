package yancey.openparticle.core.client;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.math.RoundingMode;
import java.text.NumberFormat;

import org.slf4j.Logger;
import yancey.openparticle.core.versions.TextUtil;

//#if MC>=12000
import net.minecraft.client.gui.DrawContext;
import org.jetbrains.annotations.NotNull;
//#else
//$$ import net.minecraft.client.util.math.MatrixStack;
//#endif

//#if MC>=11900
import net.minecraft.screen.ScreenTexts;
//#endif

@Environment(EnvType.CLIENT)
public class GuiProgressBar extends Screen {

    public static final Logger LOGGER = LogUtils.getLogger();

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
        //#if MC>=11900
        super(ScreenTexts.EMPTY);
        //#else
        //$$ super(TextUtil.empty());
        //#endif
        this.title = TextUtil.literal(title);
        this.max = max;
        progress = 0;
    }

    @Override
    public boolean shouldPause() {
        return false;
    }

    @Override
    public void render(
            //#if MC>=12000
            DrawContext context,
            //#else
            //$$ MatrixStack matrices,
            //#endif
            int mouseX,
            int mouseY,
            float delta
    ) {
        //#if MC>=12002
        renderBackground(context, mouseX, mouseY, delta);
        //#else
        //# renderBackground(context);
        //#endif

        //#if MC>=12000
        context.drawCenteredTextWithShadow(textRenderer, title, width / 2, height / 2 - textRenderer.fontHeight - 20, 0xFFFFFFFF);
        //#else
        //$$ drawCenteredTextWithShadow(matrices, textRenderer, title, width / 2, height / 2 - textRenderer.fontHeight - 20, 0xFFFFFFFF);
        //#endif

        if (progress > max || progress < 0) {
            LOGGER.warn("progress bar can't show,the max progress is {}, but the progress is {}", max, progress);
            close();
            return;
        }
        float a = progress * 100f / max;
        drawProgress(
                //#if MC>=12000
                context,
                //#else
                //$$ matrices,
                //#endif
                width / 2 - 100,
                width / 2 - 100 + (int) a * 2,
                width / 2 + 100,
                (height - 10) / 2,
                (height + 10) / 2
        );
        drawString(
                //#if MC>=12000
                context,
                //#else
                //$$ matrices,
                //#endif
                numberFormat.format(a) + '%',
                width / 2 - 100,
                height / 2 + 20
        );
        String b = String.valueOf(progress) + '/' + max;
        drawString(
                //#if MC>=12000
                context,
                //#else
                //$$ matrices,
                //#endif,
                b,
                width / 2 + 100 - textRenderer.getWidth(b),
                height / 2 + 20
        );
        if (progress == max) {
            close();
        }
    }

    public void drawString(
            //#if MC>=12000
            @NotNull DrawContext context,
            //#else
            //$$ MatrixStack matrices,
            //#endif
            String text,
            int x,
            int y
    ) {
        //#if MC>=12000
        context.drawText(textRenderer, text, x, y, 0xFFFFFFFF, true);
        //#else
        //$$ drawTextWithShadow(matrices, textRenderer, text, x, y, 0xFFFFFFFF);
        //#endif
    }

    private void drawProgress(
            //#if MC>=12000
            @NotNull DrawContext context,
            //#else
            //$$ MatrixStack matrices,
            //#endif
            int startX,
            int middleX,
            int endX,
            int startY,
            int endY
    ) {
        //#if MC>=12000
        context.fill(startX - 1, startY - 1, middleX, endY + 1, 0xFFFFFFFF);//left
        context.fill(middleX, startY - 1, endX + 1, startY, 0xFFFFFFFF);//top
        context.fill(endX, startY - 1, endX + 1, endY + 1, 0xFFFFFFFF);//right
        context.fill(middleX, endY, endX + 1, endY + 1, 0xFFFFFFFF);//bottom
        //#else
        //$$ fill(matrices, startX - 1, startY - 1, middleX, endY + 1, 0xFFFFFFFF);//left
        //$$ fill(matrices, middleX, startY - 1, endX + 1, startY, 0xFFFFFFFF);//top
        //$$ fill(matrices, endX, startY - 1, endX + 1, endY + 1, 0xFFFFFFFF);//right
        //$$ fill(matrices, middleX, endY, endX + 1, endY + 1, 0xFFFFFFFF);//bottom
        //#endif
    }
}
