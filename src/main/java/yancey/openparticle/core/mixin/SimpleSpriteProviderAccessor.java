package yancey.openparticle.core.mixin;

import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(targets = "net.minecraft.client.particle.ParticleManager$SimpleSpriteProvider")
public interface SimpleSpriteProviderAccessor {
    @Accessor
    List<Sprite> getSprites();
}
