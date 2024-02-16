package yancey.openparticle.core.core;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import org.slf4j.Logger;
import yancey.openparticle.api.common.OpenParticleAPI;
import yancey.openparticle.api.common.data.DataRunningPerTick;
import yancey.openparticle.core.mixin.ParticleManagerAccessor;
import yancey.openparticle.core.util.MyLogger;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

@Environment(EnvType.CLIENT)
public class OpenParticleCore {

    private static final Map<Identifier, SpriteProvider> spriteAwareFactories = ((ParticleManagerAccessor) MinecraftClient.getInstance().particleManager).getSpriteAwareFactories();
    private static final Logger LOGGER = LogUtils.getLogger();
    public static final OpenParticleAPI CORE = new OpenParticleAPI(
            identifier -> {
                Optional<RegistryEntry.Reference<ParticleType<?>>> entry = Registries.PARTICLE_TYPE.getEntry(Registries.PARTICLE_TYPE.getRawId(Registries.PARTICLE_TYPE.get(
                        new net.minecraft.util.Identifier(identifier.getNamespace(), identifier.getValue()))));
                return entry.map(particleTypeReference -> spriteAwareFactories.get(Registries.PARTICLE_TYPE.getId(particleTypeReference.value()))).orElse(null);
            },
            new MyLogger(LOGGER)
    );
    private static final ReentrantLock LOCK = new ReentrantLock();
    public static String lastPath = null;
    private static RunningHandler runningHandler = null;
    public static DataRunningPerTick[] dataRunningList = null;

    private OpenParticleCore() {

    }

    public static void clearParticle() {
        if (runningHandler != null) {
            runningHandler.stop();
            runningHandler = null;
        }
        ((ParticleManagerAccessor) MinecraftClient.getInstance().particleManager).invokeClearParticles();
    }

    public static boolean loadFile(String path) {
        LOCK.lock();
        dataRunningList = null;
        runningHandler = null;
        MinecraftClient client = MinecraftClient.getInstance();
        try {
            long timeStart = System.currentTimeMillis();
            dataRunningList = CORE.input(new File(path)).getDataRunningList();
            long timeEnd = System.currentTimeMillis();
            if (client.world != null) {
                if (client.world.getGameRules().getBoolean(GameRules.COMMAND_BLOCK_OUTPUT)) {
                    client.inGameHud.getChatHud().addMessage(Text.empty()
                            .append(Text.literal("粒子文件加载成功(耗时"))
                            .append(Text.literal((timeEnd - timeStart) + "ms").formatted(Formatting.AQUA))
                            .append(Text.literal(")"))
                    );
                }
            }
            lastPath = path;
            return true;
        } catch (IOException e) {
            if (!Files.exists(Path.of(path))) {
                client.inGameHud.getChatHud().addMessage(Text.empty()
                        .append(Text.literal("粒子文件加载失败(找不到文件: ").formatted(Formatting.RED))
                        .append(Text.literal(path).formatted(Formatting.LIGHT_PURPLE))
                        .append(Text.literal(")").formatted(Formatting.RED))
                );
            } else {
                client.inGameHud.getChatHud().addMessage(Text.literal("未知原因").formatted(Formatting.RED).styled(style -> {
                    StringWriter stringWriter = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(stringWriter);
                    e.printStackTrace(printWriter);
                    return style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal(stringWriter.toString()).formatted(Formatting.RED)));
                }));
            }
            return false;
        } finally {
            LOCK.unlock();
        }
    }

    public static void run(String path, World world) {
        LOCK.lock();
        clearParticle();
        if (!Objects.equals(lastPath, path) && !loadFile(path)) {
            return;
        }
        if (dataRunningList != null) {
            runningHandler = new RunningHandler(world, dataRunningList);
            runningHandler.run();
        }
        LOCK.unlock();
    }

    public static void runTick(String path, World world, int tick) {
        LOCK.lock();
        if (!Objects.equals(lastPath, path) && !loadFile(path)) {
            return;
        }
        if (dataRunningList != null) {
            for (DataRunningPerTick dataRunning : runningHandler.dataRunningList) {
                if (dataRunning.tick == tick) {
                    runningHandler.runTick(world, dataRunning.controllerList);
                    break;
                }
            }
        }
        LOCK.unlock();
    }

    public static boolean loadAndRun(String path, World world) {
        if (loadFile(path)) {
            run(path, world);
            return true;
        } else {
            return false;
        }
    }

}
