package yancey.openparticle.core.core.data;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import yancey.openparticle.core.client.GuiProgressBar;
import yancey.openparticle.core.network.NetworkHandler;
import yancey.openparticle.core.util.IdentifierCache;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataRunningPerTick {

    public final int tick;
    public final List<PerParticleState> dataList = new ArrayList<>();

    public DataRunningPerTick(DataInputStream dataInputStream, GuiProgressBar guiProgressBar) throws IOException {
        tick = dataInputStream.readInt();
        int size = dataInputStream.readInt();
        for (int i = 0; i < size; i++) {
            dataList.add(new PerParticleState(dataInputStream));
            guiProgressBar.progress++;
        }
    }

    public DataRunningPerTick(PacketByteBuf packetByteBuf) {
        tick = packetByteBuf.readInt();
        int size = packetByteBuf.readInt();
        for (int i = 0; i < size; i++) {
            dataList.add(new PerParticleState(packetByteBuf));
        }
    }

    public static DataRunningPerTick[] readDataRunningPerTicksToFile(DataInputStream dataInputStream, GuiProgressBar guiProgressBar) throws IOException {
        IdentifierCache.readFromFile(dataInputStream);
        DataRunningPerTick[] dataRunningPerTicks = new DataRunningPerTick[dataInputStream.readInt()];
        for (int i = 0; i < dataRunningPerTicks.length; i++) {
            dataRunningPerTicks[i] = new DataRunningPerTick(dataInputStream, guiProgressBar);
        }
        return dataRunningPerTicks;
    }

    public void toBuf(PacketByteBuf packetByteBuf) {
        packetByteBuf.writeInt(tick);
        packetByteBuf.writeInt(dataList.size());
        for (PerParticleState perParticleState : dataList) {
            perParticleState.toBuf(packetByteBuf);
        }
    }

    public void run(String path, World world) {
        if (world.isClient) {
            for (PerParticleState perParticleState : dataList) {
                perParticleState.run(world);
            }
        } else {
            NetworkHandler.summonParticle((ServerWorld) world, path, tick);
        }
    }
}
