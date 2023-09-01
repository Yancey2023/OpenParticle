package yancey.openparticle.core.util;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

public class CommonUtil {

    @Environment(EnvType.CLIENT)
    public static final MinecraftClient mc = MinecraftClient.getInstance();

    private CommonUtil() {
    }

    @Environment(EnvType.CLIENT)
    public static void openGui(Screen guiScreen) {
        mc.execute(() -> mc.setScreen(guiScreen));
    }

    @Environment(EnvType.CLIENT)
    public static void closeGui() {
        mc.execute(() -> mc.setScreen(null));
    }
}
