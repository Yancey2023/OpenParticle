package yancey.openparticle.core.particle;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import org.slf4j.Logger;
import yancey.openparticle.api.controller.Controller;
import yancey.openparticle.api.math.Vec3;

import java.awt.*;

@Environment(EnvType.CLIENT)
public class BetterParticle extends SpriteBillboardParticle {

    private static final Logger LOGGER = LogUtils.getLogger();
    private final SpriteProvider spriteProvider;
    public Controller controller;
    private Vec3 posNext;

    private BetterParticle(ClientWorld world, Vec3 posStart, SpriteProvider spriteProvider, Controller controller) {
        super(world, posStart.x, posStart.y, posStart.z);
        this.velocityMultiplier = 0.91F;
        this.gravityStrength = 0.0125F;
        this.spriteProvider = spriteProvider;
        this.controller = controller;
        updateVelocity();
        updateColor();
        this.scale *= 0.75F;
        this.maxAge = controller.maxAge;
        this.setSpriteForAge(spriteProvider);
    }

    public static BetterParticle create(ClientWorld world, SpriteProvider spriteProvider, Controller controller) {
        Vec3 posStart = controller.posGetter.get(0, controller.maxAge);
        return new BetterParticle(world, posStart, spriteProvider, controller);
    }

    private void updateColor() {
        if (age >= 0 && age < maxAge) {
            Color color = controller.colorGetter.get(age, maxAge, new Vec3(x, y, z));
            this.alpha = color.getAlpha();
            this.red = color.getRed();
            this.green = color.getGreen();
            this.blue = color.getBlue();
        }
    }

    private void updateVelocity() {
        if (this.age >= 0 && this.age + 1 < this.maxAge) {
            posNext = controller.posGetter.get(age + 1,maxAge);
            velocityX = posNext.x - x;
            velocityY = posNext.y - y;
            velocityZ = posNext.z - z;
        } else {
            posNext = null;
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
            Vec3 pos = posNext;
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
