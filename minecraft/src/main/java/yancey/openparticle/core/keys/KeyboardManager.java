package yancey.openparticle.core.keys;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.server.network.ServerPlayerEntity;
import org.lwjgl.glfw.GLFW;
import yancey.openparticle.core.core.OpenParticleServerCore;
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
            if (client.isPaused() || !client.isWindowFocused() || client.currentScreen != null) {
                return;
            }
            List<Integer> idList = new ArrayList<>();
            CustomKeyBinding keyBinding;
            for (int id = 0; id < KeyboardManager.keyBindingList.size(); id++) {
                keyBinding = keyBindingList.get(id);
                while (keyBinding.wasPressed()) {
                    if (keyBinding.onKeyPressedListener != null) {
                        keyBinding.onKeyPressedListener.onKeyPressed(client.player);
                    }
                    if (keyBinding.isRunInServer) {
                        idList.add(id);
                    }
                }
            }
            if (!idList.isEmpty()) {
                KeyboardPayloadC2S.ID.sendToServer(new KeyboardPayloadC2S(idList.stream().mapToInt(value -> value).toArray()));
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

    public static void runInServe(int[] idList, ServerPlayerEntity entityPlayerMP) {
        for (int id : idList) {
            onKeyPressedListenerList.get(id).onKeyPressed(entityPlayerMP);
        }
    }
}
