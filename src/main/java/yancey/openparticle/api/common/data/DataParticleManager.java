package yancey.openparticle.api.common.data;

import yancey.openparticle.api.common.controller.SimpleParticleController;
import yancey.openparticle.api.common.data.particle.DataParticle;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class DataParticleManager {

    public static List<DataParticle> dataParticleList = new ArrayList<>();
    public final List<DataParticle> parents;

    public DataParticleManager() {
        parents = new ArrayList<>();
    }

    public DataParticleManager(DataInputStream dataInputStream) throws IOException {
        int size1 = dataInputStream.readInt();
        dataParticleList = new ArrayList<>(size1);
        for (int i = 0; i < size1; i++) {
            dataParticleList.add(DataParticle.readFromFile(this, dataInputStream));
        }
        int size2 = dataInputStream.readInt();
        parents = new ArrayList<>(size2);
        for (int i = 0; i < size2; i++) {
            parents.add(getDataParticle(dataInputStream.readInt()));
        }
    }

    public void add(DataParticle dataParticle) {
        parents.add(dataParticle);
    }

    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeInt(dataParticleList.size());
        for (DataParticle dataParticle : dataParticleList) {
            dataParticle.writeToFile(dataOutputStream);
        }
        dataOutputStream.writeInt(parents.size());
        for (DataParticle parent : parents) {
            dataOutputStream.writeInt(parent.id);
        }
    }

    public DataParticle getDataParticle(int id) {
        return dataParticleList.get(id);
    }

    public long getParticleCount() {
        return parents.stream()
                .flatMap(DataParticle::streamParticleSingle)
                .count();
    }

    public DataRunningPerTick[] getDataRunningList() {
        List<DataRunningPerTick> dataRunningList = new ArrayList<>();
        parents.stream()
                .flatMap(parent -> parent.getNode().second)
                .toList()
                .stream()
                .map(SimpleParticleController::new)
                .toList()
                .stream()
                .forEach(controller -> {
                    DataRunningPerTick.getFromList(controller.getTickStart(), dataRunningList).controllerList.add(controller);
                });
        dataRunningList.sort(Comparator.comparingInt(dataRunningPerTick -> dataRunningPerTick.tick));
        return dataRunningList.toArray(new DataRunningPerTick[0]);
    }
}
