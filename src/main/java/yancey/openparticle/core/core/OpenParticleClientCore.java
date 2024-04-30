package yancey.openparticle.core.core;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.mixin.client.particle.ParticleManagerAccessor.SimpleSpriteProviderAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.texture.Sprite;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import org.joml.Quaternionf;
import yancey.openparticle.api.common.nativecore.OpenParticleProject;
import yancey.openparticle.core.events.RunningEventManager;
import yancey.openparticle.core.mixin.BufferBuilderAccessor;
import yancey.openparticle.core.mixin.ParticleManagerAccessor;
import yancey.openparticle.core.network.RunPayloadC2S;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

@Environment(EnvType.CLIENT)
public class OpenParticleClientCore {

    private static final Map<Identifier, SpriteProvider> spriteAwareFactories = ((ParticleManagerAccessor) MinecraftClient.getInstance().particleManager).getSpriteAwareFactories();
    @SuppressWarnings("UnstableApiUsage")
    private static final OpenParticleProject.Bridge bridge = (namespace, value) -> {
        SpriteProvider spriteProvider = spriteAwareFactories.get(new Identifier(namespace, value));
        List<Sprite> sprites = ((SimpleSpriteProviderAccessor) spriteProvider).getSprites();
        float[] result = new float[sprites.size() * 4];
        for (int i = 0; i < sprites.size(); i++) {
            Sprite sprite = sprites.get(i);
            int j = i * 4;
            result[j] = sprite.getMinU();
            result[j + 1] = sprite.getMinV();
            result[j + 2] = sprite.getMaxU();
            result[j + 3] = sprite.getMaxV();
        }
        return result;
    };
    private static final ReentrantLock LOCK = new ReentrantLock();
    public static boolean inRendered = true;
    private static OpenParticleProject openParticleProject;
    private static int nextTick = Integer.MAX_VALUE;
    private static boolean isRunning = false;
    private static boolean isRendering = false;

    private OpenParticleClientCore() {

    }

    public static void stop() {
        isRunning = false;
        nextTick = Integer.MAX_VALUE;
        RunningEventManager.INSTANCE.stop();
    }

    public static boolean loadFile0(String path) {
        stop();
        if (openParticleProject != null) {
            openParticleProject.close();
        }
        try {
            openParticleProject = new OpenParticleProject(bridge, path);
            return true;
        } catch (Exception e) {
            MinecraftClient client = MinecraftClient.getInstance();
            if (!Files.exists(Path.of(path))) {
                client.inGameHud.getChatHud().addMessage(Text.empty()
                        .append(Text.literal("粒子文件加载失败(找不到文件: ").formatted(Formatting.RED))
                        .append(Text.literal(path).formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(")").formatted(Formatting.RED))
                );
            } else {
                client.inGameHud.getChatHud().addMessage(Text.literal("粒子文件加载失败").formatted(Formatting.RED).styled(style -> {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(stringWriter.toString()).formatted(Formatting.RED)));
                }));
            }
            return false;
        }
    }

    public static boolean loadFile(String path) {
        LOCK.lock();
        try {
            MinecraftClient client = MinecraftClient.getInstance();
            long timeStart = System.currentTimeMillis();
            boolean isSuccess = loadFile0(path);
            long timeEnd = System.currentTimeMillis();
            if (isSuccess && client.world != null) {
                if (client.world.getGameRules().getBoolean(GameRules.COMMAND_BLOCK_OUTPUT)) {
                    client.inGameHud.getChatHud().addMessage(Text.empty()
                            .append(Text.literal("粒子文件加载成功(耗时"))
                            .append(Text.literal((timeEnd - timeStart) + "ms").formatted(Formatting.AQUA))
                            .append(Text.literal(")"))
                    );
                }
            }
            return isSuccess;
        } finally {
            LOCK.unlock();
        }
    }

    public static void loadAndRun(String path) {
        if (loadFile(path)) {
            run(path);
        }
    }

    public static void run(String path) {
        LOCK.lock();
        try {
            if (openParticleProject == null || openParticleProject.path == null || !Objects.equals(openParticleProject.path, path)) {
                if (!loadFile0(path)) {
                    return;
                }
            }
            ClientPlayNetworking.send(new RunPayloadC2S(openParticleProject.path, openParticleProject.tickEnd));
        } finally {
            LOCK.unlock();
        }
    }

    public static void runTick(String path, int tick) {
        LOCK.lock();
        try {
            if (openParticleProject == null || !Objects.equals(openParticleProject.path, path)) {
                if (!loadFile0(path)) {
                    isRunning = false;
                    isRendering = false;
                    return;
                }
            }
            if (tick < 0 || tick >= openParticleProject.tickEnd) {
                isRunning = false;
                isRendering = false;
                return;
            }
            nextTick = tick;
            isRunning = true;
        } finally {
            LOCK.unlock();
        }
    }

    public static int getParticleSize() {
        LOCK.lock();
        try {
            return openParticleProject == null ? 0 : openParticleProject.getParticleCount();
        } finally {
            LOCK.unlock();
        }
    }

    public static void tick() {
        LOCK.lock();
        try {
            if (openParticleProject != null && isRunning) {
                openParticleProject.tick(nextTick);
                isRendering = true;
            } else {
                isRendering = false;
            }
        } finally {
            LOCK.unlock();
        }
    }

    public static void render(Camera camera, float tickDelta, BufferBuilder bufferBuilder) {
        if (inRendered) {
            return;
        }
        LOCK.lock();
        try {
            inRendered = true;
            if (openParticleProject == null || !isRunning || !isRendering) {
                return;
            }
            int particleCount = openParticleProject.getParticleCount();
            if (particleCount == 0) {
                return;
            }
            int elementOffset = 112 * particleCount;
            ((BufferBuilderAccessor) bufferBuilder).invokeGrow(elementOffset);
            Vec3d pos = camera.getPos();
            Quaternionf rotation = camera.getRotation();
            openParticleProject.render(
                    ((BufferBuilderAccessor) bufferBuilder).getBuffer(),
                    true, tickDelta,
                    (float) pos.x, (float) pos.y, (float) pos.z,
                    rotation.x, rotation.y, rotation.z, rotation.w
            );
            ((BufferBuilderAccessor) bufferBuilder).setElementOffset(elementOffset);
            ((BufferBuilderAccessor) bufferBuilder).setVertexCount(4 * particleCount);
        } finally {
            LOCK.unlock();
        }
    }

}
