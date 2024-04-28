package yancey.openparticle.core.core;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.mixin.client.particle.ParticleManagerAccessor.SimpleSpriteProviderAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.texture.Sprite;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameRules;
import org.joml.Quaternionf;
import org.slf4j.Logger;
import yancey.openparticle.api.common.nativecore.OpenParticleProject;
import yancey.openparticle.core.events.RunningEventManager;
import yancey.openparticle.core.mixin.BufferBuilderAccessor;
import yancey.openparticle.core.mixin.ParticleManagerAccessor;
import yancey.openparticle.core.network.RunTickPayloadS2C;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

@Environment(EnvType.CLIENT)
public class OpenParticleCore {

    private static final Map<Identifier, SpriteProvider> spriteAwareFactories = ((ParticleManagerAccessor) MinecraftClient.getInstance().particleManager).getSpriteAwareFactories();
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final OpenParticleProject.Bridge bridge = (namespace, value) -> {
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
    public static OpenParticleProject openParticleProject;
    public static int nextTick = Integer.MAX_VALUE;
    public static int nextRunTick = Integer.MAX_VALUE;

    private OpenParticleCore() {

    }

    private static float lastRunTick = -1;
    private static float lastTickDelta = -1;

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

    public static void stop() {
        nextTick = Integer.MAX_VALUE;
        RunningEventManager.INSTANCE.stop();
    }

    public static boolean run(ServerWorld world) {
        if (openParticleProject != null) {
            run(openParticleProject.path, world);
            return true;
        } else {
            return false;
        }
    }

    public static void run(String path, ServerWorld world) {
        stop();
        nextTick = 0;
        RunningEventManager.INSTANCE.run(() -> {
            LOCK.lock();
            try {
                if (openParticleProject == null || nextTick > openParticleProject.tickEnd) {
                    stop();
                    return;
                }
                RunTickPayloadS2C payload = new RunTickPayloadS2C(path, nextTick++);
                for (ServerPlayerEntity serverPlayerEntity : world.getServer().getPlayerManager().getPlayerList()) {
                    ServerPlayNetworking.send(serverPlayerEntity, payload);
                }
            } finally {
                LOCK.unlock();
            }
        });
    }

    public static void runTick(String path, int tick) {
        LOCK.lock();
        try {
            if (((openParticleProject == null || !Objects.equals(openParticleProject.path, path) && !loadFile0(path)))
                    || tick < 0 || tick > openParticleProject.tickEnd) {
                nextRunTick = Integer.MAX_VALUE;
                return;
            }
            nextRunTick = tick;
        } finally {
            LOCK.unlock();
        }
    }

    public static boolean loadAndRun(String path, ServerWorld world) {
        if (loadFile(path)) {
            run(path, world);
            return true;
        } else {
            return false;
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
            if (nextRunTick != Integer.MAX_VALUE) {
                openParticleProject.tick(nextRunTick);
            }
        } finally {
            LOCK.unlock();
        }
    }

    public static void render(Camera camera, float tickDelta, BufferBuilder bufferBuilder) {
        LOCK.lock();
        try {
            if (nextRunTick == Integer.MAX_VALUE || openParticleProject == null) {
                return;
            }
            int particleCount = openParticleProject.getParticleCount();
            if (particleCount == 0) {
                return;
            }
            if (nextRunTick == lastRunTick && tickDelta == lastTickDelta) {
                return;
            }
            lastRunTick = nextRunTick;
            lastTickDelta = tickDelta;
            int elementOffset = 112 * particleCount;
            ((BufferBuilderAccessor) bufferBuilder).invokeGrow(elementOffset);
            Vec3d pos = camera.getPos();
            Quaternionf rotation = camera.getRotation();
            openParticleProject.render(
                    ((BufferBuilderAccessor) bufferBuilder).getBuffer(),
                    false, tickDelta,
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
