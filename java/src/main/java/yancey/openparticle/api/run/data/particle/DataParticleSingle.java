package yancey.openparticle.api.run.data.particle;

import yancey.openparticle.api.run.data.DataParticleManager;
import yancey.openparticle.api.run.data.identifier.DataIdentifier;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class DataParticleSingle extends DataParticle {

    public DataIdentifier dataIdentifier;
    public int age;

    public DataParticleSingle(DataIdentifier dataIdentifier, int age) {
        this.dataIdentifier = dataIdentifier;
        this.age = age;
    }

    public DataParticleSingle(DataParticleManager dataParticleManager, DataInputStream dataInputStream) throws IOException {
        this.dataIdentifier = dataParticleManager.dataIdentifierList.get(dataInputStream.readInt());
        this.age = dataInputStream.readInt();
    }

    @Override
    public int getType() {
        return SINGLE;
    }

    @Override
    public void writeToFile(DataParticleManager dataParticleManager, DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataParticleManager, dataOutputStream);
        int index = -1;
        for (int i = 0; i < dataParticleManager.dataIdentifierList.size(); i++) {
            if (dataParticleManager.dataIdentifierList.get(i) == dataIdentifier) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            throw new IllegalArgumentException("fail to find particle identifier in particle manager");
        }
        dataOutputStream.writeInt(index);
        dataOutputStream.writeInt(age);
    }

    @Override
    public void collect(List<DataParticle> dataParticleList) {
        if (!dataParticleList.contains(this)) {
            dataParticleList.add(this);
        }
    }

    @Override
    public void optimize() {

    }

    @Override
    public int getParticleCount() {
        return 1;
    }

    @Override
    public int getColor(int tick) {
        return 0xFFFFFFFF;
    }

}
