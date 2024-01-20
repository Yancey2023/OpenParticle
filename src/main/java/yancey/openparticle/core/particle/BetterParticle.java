package yancey.openparticle.core.particle;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import yancey.openparticle.core.util.Vec3;

@Environment(EnvType.CLIENT)
public class BetterParticle extends SpriteBillboardParticle {
    private final SpriteProvider spriteProvider;
    public final Vec3[] positions;
    public final int[] colors;

    private BetterParticle(ClientWorld world, Vec3 posStart, Vec3[] positions, int[] colors, SpriteProvider spriteProvider) {
        super(world, posStart.x, posStart.y, posStart.z);
        this.velocityMultiplier = 0.91F;
        this.gravityStrength = 0.0125F;
        this.spriteProvider = spriteProvider;
        this.positions = positions;
        this.colors = colors;
        updateVelocity();
        updateColor();
        this.scale *= 0.75F;
        this.maxAge = positions.length;
        this.setSpriteForAge(spriteProvider);
    }

    public static BetterParticle create(ClientWorld world, Vec3[] positions, int[] colors, SpriteProvider spriteProvider) {
        if (positions.length == 0) {
            return null;
        }
        return new BetterParticle(world, positions[0], positions, colors, spriteProvider);
    }

    private void updateColor() {
        if (age >= 0 && age < maxAge) {
            int color = colors[age];
            this.alpha = (float) ((color >> 24) & 0xFF) / 255;
            this.red = (float) ((color >> 16) & 0xFF) / 255;
            this.green = (float) ((color >> 8) & 0xFF) / 255;
            this.blue = (float) (color & 0xFF) / 255;
        }
    }

    private void updateVelocity() {
        if (this.age >= 0 && this.age + 1 < this.maxAge) {
            Vec3 pos = positions[age];
            Vec3 posNext = positions[age + 1];
            velocityX = posNext.x - pos.x;
            velocityY = posNext.y - pos.y;
            velocityZ = posNext.z - pos.z;
        } else {
            velocityX = 0;
            velocityY = 0;
            velocityZ = 0;
        }
    }

    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        if (++this.age >= this.maxAge) {
            this.markDead();
        } else {
            Vec3 pos = positions[age];
            setPos(pos.x, pos.y, pos.z);
            updateVelocity();
            updateColor();
        }
        this.setSpriteForAge(this.spriteProvider);
    }

    public int getBrightness(float tint) {
        return 15728880;
    }
}
