package yancey.openparticle.core.mixin;

import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ParticleManager.class)
public interface ParticleManagerAccessor {

    @Accessor("spriteAwareFactories")
    Map<Identifier, SpriteProvider> getSpriteAwareFactories();

}
