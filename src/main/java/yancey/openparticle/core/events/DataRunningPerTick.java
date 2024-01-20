package yancey.openparticle.core.events;

import net.minecraft.world.World;
import yancey.openparticle.core.client.GuiProgressBar;
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
            guiProgressBar.progress ++;
        }
    }

    public static DataRunningPerTick[] readDataRunningPerTicksToFile(DataInputStream dataInputStream, GuiProgressBar guiProgressBar) throws IOException {
        IdentifierCache.readFromFile(dataInputStream);
        DataRunningPerTick[] dataRunningPerTicks = new DataRunningPerTick[dataInputStream.readInt()];
        for (int i = 0; i < dataRunningPerTicks.length; i++) {
            dataRunningPerTicks[i] = new DataRunningPerTick(dataInputStream,guiProgressBar);
        }
        return dataRunningPerTicks;
    }

    public void run(World world) {
        for (PerParticleState perParticleState: dataList) {
            perParticleState.run(world);
        }
    }
}
