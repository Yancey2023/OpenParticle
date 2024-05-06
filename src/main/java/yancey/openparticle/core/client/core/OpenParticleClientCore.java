package yancey.openparticle.core.client.core;

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
    //用来防止同一次渲染中渲染方法被调用多次
    public static boolean inRendered = true;
    private static OpenParticleProject openParticleProject;
    public static int lastTick = -1;
    public static boolean isRepeatTick = false;
    private static boolean isRendering = false;
    private static boolean isSingleThread = true;

    private OpenParticleClientCore() {

    }

    private static int nextTick;
    private static boolean nextIsSingleThread = true;
    //用来防止标记当前的运行情况
    private static boolean isRunning = false;

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

    public static void loadAndRun(String path, boolean isSingleThread) {
        if (loadFile(path)) {
            run(path, isSingleThread);
        }
    }

    public static void run(String path, boolean isSingleThread) {
        LOCK.lock();
        try {
            if (openParticleProject == null || openParticleProject.path == null || !Objects.equals(openParticleProject.path, path)) {
                if (!loadFile0(path)) {
                    return;
                }
            }
            ClientPlayNetworking.send(new RunPayloadC2S(openParticleProject.path, openParticleProject.tickEnd, isSingleThread));
        } finally {
            LOCK.unlock();
        }
    }

    public static void runTick(String path, int tick, boolean isSingleThread) {
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
            OpenParticleClientCore.isSingleThread = isSingleThread;
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

    private static void stop() {
        isRunning = false;
        isRendering = false;
        RunningEventManager.INSTANCE.stop();
    }

    private static boolean loadFile0(String path) {
        stop();
        if (openParticleProject != null) {
            openParticleProject.close();
            openParticleProject = null;
        }
        if (!Files.exists(Path.of(path))) {
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.empty()
                    .append(Text.literal("粒子文件加载失败(找不到文件: ").formatted(Formatting.RED))
                    .append(Text.literal(path).formatted(Formatting.LIGHT_PURPLE))
                    .append(Text.literal(")").formatted(Formatting.RED))
            );
            return false;
        }
        try {
            openParticleProject = new OpenParticleProject(bridge, path);
            return true;
        } catch (Exception e) {
            MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal("粒子文件加载失败").formatted(Formatting.RED).styled(style -> {
                StringWriter stringWriter = new StringWriter();
                PrintWriter printWriter = new PrintWriter(stringWriter);
                e.printStackTrace(printWriter);
                return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(stringWriter.toString()).formatted(Formatting.RED)));
            }));
            return false;
        }
    }

    public static void tick() {
        LOCK.lock();
        try {
            isRepeatTick = false;
            if (openParticleProject != null && isRunning) {
                if (lastTick != nextTick) {
                    openParticleProject.tick(nextTick);
                } else {
                    isRepeatTick = true;
                }
                isRendering = true;
            } else {
                isRendering = false;
            }
            nextIsSingleThread = isSingleThread;
            lastTick = nextTick;
        } finally {
            LOCK.unlock();
        }
    }

    public static void render(Camera camera, float tickDelta, BufferBuilder bufferBuilder) {
        LOCK.lock();
        try {
            if (inRendered) {
                return;
            }
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
                    nextIsSingleThread, isRepeatTick ? 1 : tickDelta,
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
