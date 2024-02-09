package yancey.openparticle.api.common.data;

import yancey.openparticle.api.common.controller.SimpleParticleController;

import java.util.ArrayList;
import java.util.List;

public class DataRunningPerTick {

    public final int tick;
    public final List<SimpleParticleController> controllerList = new ArrayList<>();

    public DataRunningPerTick(int tick) {
        this.tick = tick;
    }

    public static DataRunningPerTick getFromList(int tick, List<DataRunningPerTick> dataRunningList) {
        for (DataRunningPerTick dataRunning : dataRunningList) {
            if (dataRunning.tick == tick) {
                return dataRunning;
            }
        }
        DataRunningPerTick dataRunning = new DataRunningPerTick(tick);
        dataRunningList.add(dataRunning);
        return dataRunning;
    }

}
