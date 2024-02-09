package yancey.openparticle.core.core.data;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import yancey.openparticle.api.common.controller.SimpleParticleController;
import yancey.openparticle.api.common.data.DataRunningPerTick;
import yancey.openparticle.core.core.OpenParticleCore;
import yancey.openparticle.core.events.RunningEventManager;
import yancey.openparticle.core.mixin.ParticleManagerAccessor;
import yancey.openparticle.core.network.NetworkHandler;
import yancey.openparticle.core.particle.BetterParticle;

import java.util.List;
import java.util.Optional;

public class RunningHandler {
    public final World world;
    public boolean isRunning = false;
    public final DataRunningPerTick[] dataRunningList;
    private int tick = 0;
    private int which = 0;

    public RunningHandler(World world, DataRunningPerTick[] dataRunningList) {
        this.dataRunningList = dataRunningList;
        this.world = world;
    }

    public void run() {
        if (dataRunningList != null && dataRunningList.length != 0) {
            isRunning = true;
            tick = Math.min(dataRunningList[0].tick, 0);
            which = 0;
            RunningEventManager.INSTANCE.run(this);
        } else if (isRunning) {
            stop();
        }
    }

    public void stop() {
        isRunning = false;
        tick = 0;
        which = 0;
        RunningEventManager.INSTANCE.stop(this);
    }

    public void runPerTick() {
        if (dataRunningList == null) {
            stop();
            return;
        }
        while (which < dataRunningList.length) {
            if (tick != dataRunningList[which].tick) {
                tick++;
                return;
            }
            if (world.isClient) {
                runTick(OpenParticleCore.lastPath, world, dataRunningList[which++].controllerList);
            } else {
                which++;
                NetworkHandler.summonParticle((ServerWorld) world, OpenParticleCore.lastPath, tick);
            }
        }
        stop();
    }

    public void runTick(String path, World world, List<SimpleParticleController> controllerList) {
        for (SimpleParticleController controller : controllerList) {
            Optional<RegistryEntry.Reference<ParticleType<?>>> entry = Registries.PARTICLE_TYPE.getEntry(controller.getParticleTypeRawId(OpenParticleCore.CORE));
            entry.ifPresent(particleTypeReference -> MinecraftClient.getInstance().particleManager.addParticle(BetterParticle.create((ClientWorld) world, controller,
                    ((ParticleManagerAccessor) MinecraftClient.getInstance().particleManager).getSpriteAwareFactories().get(Registries.PARTICLE_TYPE.getId(particleTypeReference.value())))));
        }
    }
}
