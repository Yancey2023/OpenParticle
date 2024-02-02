package yancey.openparticle.core.keys;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;

import static yancey.openparticle.core.OpenParticle.MOD_ID;

@Environment(EnvType.CLIENT)
public class CustomKeyBinding extends KeyBinding {

    private static final String category = "key.category." + MOD_ID;
    private static final String descriptionFront = "key." + MOD_ID + '.';

    public final boolean isRunInServer;
    public final OnKeyPressedListener onKeyPressedListener;

    public CustomKeyBinding(String description, int keyCode, boolean isRunInServer, OnKeyPressedListener onKeyPressedListener) {
        super(descriptionFront + description, keyCode, category);
        KeyBindingHelper.registerKeyBinding(this);
        this.isRunInServer = isRunInServer;
        this.onKeyPressedListener = onKeyPressedListener;
    }
}
