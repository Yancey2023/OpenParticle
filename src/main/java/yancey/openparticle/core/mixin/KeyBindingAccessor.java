package yancey.openparticle.core.mixin;

import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyBinding.class)
public interface KeyBindingAccessor {

    @Accessor("timesPressed")
    int getTimesPressed();

    @Invoker("reset")
    void invokerReset();

}
