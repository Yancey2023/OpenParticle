package yancey.openparticle.core.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import yancey.openparticle.api.particle.Particles;
import yancey.openparticle.core.mixin.ParticleManagerAccessor;

import java.util.Map;

public class SpriteProviderUtil {

    public static Map<Identifier, SpriteProvider> map = ((ParticleManagerAccessor) MinecraftClient.getInstance().particleManager).getSpriteAwareFactories();
    public static SpriteProvider END_ROD = null;

    public static SpriteProvider getSpriteProvider(int id) {
        if (id == Particles.END_ROD) {
            if(END_ROD == null){
                END_ROD = map.get(Registries.PARTICLE_TYPE.getId(ParticleTypes.END_ROD));
            }
            return END_ROD;
        }
        return null;
    }

}
