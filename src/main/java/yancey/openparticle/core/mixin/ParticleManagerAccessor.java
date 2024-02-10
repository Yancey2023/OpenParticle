package yancey.openparticle.core.mixin;


import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import java.util.Queue;

@Mixin(ParticleManager.class)
public interface ParticleManagerAccessor {

    @Accessor("spriteAwareFactories")
    Map<Identifier, SpriteProvider> getSpriteAwareFactories();

    @Invoker("clearParticles")
    void invokeClearParticles();

    @Accessor
    Map<ParticleTextureSheet, Queue<Particle>> getParticles();

    @Accessor
    Queue<Particle> getNewParticles();
}
