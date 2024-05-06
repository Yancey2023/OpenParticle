package yancey.openparticle.core.keys;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import org.lwjgl.glfw.GLFW;
import yancey.openparticle.core.core.OpenParticleServerCore;
import yancey.openparticle.core.mixin.KeyBindingAccessor;
import yancey.openparticle.core.network.KeyboardPayloadC2S;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class KeyboardManager {

    @Environment(EnvType.CLIENT)
    public static final List<CustomKeyBinding> keyBindingList = new ArrayList<>();
    public static final List<OnKeyPressedListener> onKeyPressedListenerList = new ArrayList<>();

    public static void init(boolean isClient) {
        register(isClient, "run", GLFW.GLFW_KEY_V, true, playerEntity ->
                OpenParticleServerCore.run(Objects.requireNonNull(playerEntity.getServer()), true));
        if (!isClient) {
            return;
        }
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.isPaused() ||//游戏暂停了
                    !client.isWindowFocused() ||//玩家不在游戏窗口
                    client.currentScreen != null//打开着其他gui
            ) {
                return;
            }
            List<Integer> idList = null;
            CustomKeyBinding keyBinding;
            for (int id = 0; id < KeyboardManager.keyBindingList.size(); id++) {
                keyBinding = keyBindingList.get(id);
                if (keyBinding.isPressed() && ((KeyBindingAccessor) keyBinding).getTimesPressed() > 0) {
                    if (idList == null) {
                        idList = new ArrayList<>();
                    }
                    if (keyBinding.onKeyPressedListener != null) {
                        keyBinding.onKeyPressedListener.onKeyPressed(client.player);
                    }
                    if (keyBinding.isRunInServer) {
                        idList.add(id);
                    }
                    ((KeyBindingAccessor) keyBinding).invokerReset();
                }
            }
            if (idList != null) {
                ClientPlayNetworking.send(new KeyboardPayloadC2S(idList));
            }
        });
    }


    public static void register(boolean isClient, String description, int keyCode, boolean isRunInServer, OnKeyPressedListener onKeyPressedListener) {
        if (isClient) {
            keyBindingList.add(new CustomKeyBinding(description, keyCode, isRunInServer, isRunInServer ? null : onKeyPressedListener));
        } else {
            onKeyPressedListenerList.add(isRunInServer ? onKeyPressedListener : null);
        }
    }

    public static void runInServe(List<Integer> idList, ServerPlayerEntity entityPlayerMP) {
        for (int id : idList) {
            onKeyPressedListenerList.get(id).onKeyPressed(entityPlayerMP);
        }
    }
}
