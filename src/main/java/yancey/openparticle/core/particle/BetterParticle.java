package yancey.openparticle.core.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.*;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.Vec3d;
import org.joml.Quaternionf;
import yancey.openparticle.api.common.controller.ParticleController;
import yancey.openparticle.api.common.data.ParticleState;
import yancey.openparticle.core.client.ParticleAsyncManager;
import yancey.openparticle.core.mixin.BufferBuilderAccessor;

import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Environment(EnvType.CLIENT)
public class BetterParticle extends Particle {

    public static final ExecutorService executorService = Executors.newFixedThreadPool(64);
    private static float cacheX1, cacheY1, cacheZ1, cacheX2, cacheY2, cacheZ2;    public static final ParticleTextureSheet BETTER_PARTICLE_SHEET = new ParticleTextureSheet() {
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.depthMask(true);
            RenderSystem.setShaderTexture(0, SpriteAtlasTexture.PARTICLE_ATLAS_TEXTURE);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            builder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR_LIGHT);
        }

        public void draw(Tessellator tessellator) {
            ParticleAsyncManager.renderParticles(tessellator.getBuffer());
            tessellator.draw();
        }

        public String toString() {
            return "BETTER_PARTICLE_SHEET";
        }
    };
    private static Vec3d lastCameraPos;
    private final ParticleController particleController;
    private final SpriteProvider spriteProvider;
    private ParticleState particleState;
    private float minU, maxU, minV, maxV;

    private BetterParticle(ClientWorld world, ParticleState start, ParticleController particleController, SpriteProvider spriteProvider) {
        super(world, start.x, start.y, start.z);
        this.spriteProvider = spriteProvider;
        this.particleController = particleController;
        this.particleState = start;
        this.maxAge = particleController.getAge();
        updateSprite();
    }

    public static BetterParticle create(ClientWorld world, ParticleController particleController, SpriteProvider spriteProvider) {
        if (particleController.getAge() == 0) {
            return null;
        }
        return new BetterParticle(world, particleController.getParticleState(0), particleController, spriteProvider);
    }

    public static void buildRenderCache(Camera camera) {
        Vec3d cameraPos = camera.getPos();
        if (cameraPos != lastCameraPos) {
            Quaternionf rotation = camera.getRotation();
            float xx = rotation.x * rotation.x, yy = rotation.y * rotation.y, zz = rotation.z * rotation.z, ww = rotation.w * rotation.w;
            float xy = rotation.x * rotation.y, xz = rotation.x * rotation.z, yz = rotation.y * rotation.z, xw = rotation.x * rotation.w;
            float zw = rotation.z * rotation.w, yw = rotation.y * rotation.w, k = 1 / (xx + yy + zz + ww);
            float a1 = (xx - yy - zz + ww) * k * -0.1F;
            float b1 = 2 * (xy + zw) * k * -0.1F;
            float c1 = 2 * (xz - yw) * k * -0.1F;
            float a2 = 2 * (xy - zw) * k * 0.1F;
            float b2 = (yy - xx - zz + ww) * k * 0.1F;
            float c2 = 2 * (yz + xw) * k * 0.1F;
            cacheX1 = a1 - a2;
            cacheY1 = b1 - b2;
            cacheZ1 = c1 - c2;
            cacheX2 = a1 + a2;
            cacheY2 = b1 + b2;
            cacheZ2 = c1 + c2;
            lastCameraPos = cameraPos;
        }
    }

    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    public ParticleTextureSheet getType() {
        return BETTER_PARTICLE_SHEET;
    }

    private void updateSprite() {
        Sprite sprite = spriteProvider.getSprite(this.age, this.maxAge);
        this.minU = sprite.getMinU();
        this.maxU = sprite.getMaxU();
        this.minV = sprite.getMinV();
        this.maxV = sprite.getMaxV();
    }

    public void tick() {
        updateSprite();
        this.prevPosX = this.particleState.x;
        this.prevPosY = this.particleState.y;
        this.prevPosZ = this.particleState.z;
        if (++this.age >= this.maxAge) {
            this.markDead();
            return;
        }
        this.particleState = particleController.getParticleState(age);
    }

    public void clearCache() {
        particleController.clearCache();
    }

    @Override
    public void buildGeometry(VertexConsumer vertexConsumer, Camera camera, float tickDelta) {
        // do nothing
    }

    public void buildGeometryAsync(VertexConsumer vertexConsumer, Camera camera, float tickDelta, int elementOffset) {
        Vec3d cameraPos = camera.getPos();
        float dx = lerp(tickDelta, (float) this.prevPosX, (float) this.particleState.x) - (float) cameraPos.getX();
        float dy = lerp(tickDelta, (float) this.prevPosY, (float) this.particleState.y) - (float) cameraPos.getY();
        float dz = lerp(tickDelta, (float) this.prevPosZ, (float) this.particleState.z) - (float) cameraPos.getZ();
        vertex(elementOffset, vertexConsumer, cacheX1 + dx, cacheY1 + dy, cacheZ1 + dz, maxU, maxV);
        vertex(elementOffset + 28, vertexConsumer, cacheX2 + dx, cacheY2 + dy, cacheZ2 + dz, maxU, minV);
        vertex(elementOffset + 56, vertexConsumer, -cacheX1 + dx, -cacheY1 + dy, -cacheZ1 + dz, minU, minV);
        vertex(elementOffset + 84, vertexConsumer, -cacheX2 + dx, -cacheY2 + dy, -cacheZ2 + dz, minU, maxV);
    }

    private void vertex(int elementOffset, VertexConsumer vertexConsumer, float x, float y, float z, float u, float v) {
        ByteBuffer buffer = ((BufferBuilderAccessor) vertexConsumer).getBuffer();
        //vertex
        buffer.putFloat(elementOffset, x);
        buffer.putFloat(elementOffset + 4, y);
        buffer.putFloat(elementOffset + 8, z);
        //texture
        buffer.putFloat(elementOffset + 12, u);
        buffer.putFloat(elementOffset + 16, v);
        //color
        buffer.put(elementOffset + 20, particleState.r);
        buffer.put(elementOffset + 21, particleState.g);
        buffer.put(elementOffset + 22, particleState.b);
        buffer.put(elementOffset + 23, particleState.a);
        //light
        buffer.putShort(elementOffset + 24, (short) (particleState.bright & '\uffff'));
        buffer.putShort(elementOffset + 26, (short) (particleState.bright >> 16 & '\uffff'));
    }


}
