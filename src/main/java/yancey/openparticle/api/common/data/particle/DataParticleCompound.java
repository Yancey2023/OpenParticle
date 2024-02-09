package yancey.openparticle.api.common.data.particle;

import yancey.openparticle.api.common.data.DataParticleManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.stream.Stream;

public class DataParticleCompound extends DataParticle {

    //组合方式 true - 同时出现 false - 顺序出现
    public final boolean flag;
    public final DataParticle[] dataParticles;

    public DataParticleCompound(boolean flag, DataParticle[] dataParticles) {
        super();
        this.flag = flag;
        this.dataParticles = dataParticles;
    }

    public DataParticleCompound(DataParticleManager dataParticleManager, DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
        this.flag = dataInputStream.readBoolean();
        this.dataParticles = new DataParticle[dataInputStream.readInt()];
        for (int i = 0; i < this.dataParticles.length; i++) {
            this.dataParticles[i] = dataParticleManager.getDataParticle(dataInputStream.readInt());
        }
    }

    @Override
    public int getType() {
        return COMPOUND;
    }

    @Override
    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataOutputStream);
        dataOutputStream.writeBoolean(flag);
        dataOutputStream.writeInt(dataParticles.length);
        for (DataParticle dataParticle : dataParticles) {
            dataOutputStream.writeInt(dataParticle.id);
        }
    }

    @Override
    public Stream<DataParticle> getChildren() {
        return Arrays.stream(dataParticles);
    }
}
