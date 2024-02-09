package yancey.openparticle.api.common.data.particle;

import yancey.openparticle.api.common.data.DataParticleManager;
import yancey.openparticle.api.common.data.color.DataColor;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

public class DataParticleColor extends DataParticle {

    public final DataParticle dataParticle;
    public final DataColor color;

    public DataParticleColor(DataParticle dataParticle, DataColor color) {
        super();
        this.dataParticle = dataParticle;
        this.color = color;
    }

    public DataParticleColor(DataParticleManager dataParticleManager, DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
        this.dataParticle = dataParticleManager.getDataParticle(dataInputStream.readInt());
        this.color = DataColor.readFromFile(dataInputStream);
    }

    @Override
    public int getType() {
        return COLOR;
    }

    @Override
    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataOutputStream);
        dataOutputStream.writeInt(dataParticle.id);
        color.writeToFile(dataOutputStream);
    }

    @Override
    public Stream<DataParticle> getChildren() {
        return Stream.of(dataParticle);
    }

    @Override
    public Integer getColor(int tick, int age) {
        return color.getColor(tick, age);
    }
}
