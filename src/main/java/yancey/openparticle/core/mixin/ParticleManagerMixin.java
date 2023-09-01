package yancey.openparticle.core.mixin;

import net.minecraft.client.particle.ParticleManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin({ParticleManager.class})
public abstract class ParticleManagerMixin {

    @ModifyArg(method = {"method_18125"}, at = @At(value = "INVOKE", target = "com.google.common.collect.EvictingQueue.create(I)Lcom/google/common/collect/EvictingQueue;", remap = false))
    private static int modifyArgTick(int maxParticleCount) {
        return 65536;
    }
}