package yancey.openparticle.api.common.data.particle;

import yancey.openparticle.api.common.data.DataParticleManager;
import yancey.openparticle.api.common.data.vec3.DataVec3;
import yancey.openparticle.api.common.math.Matrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.stream.Stream;

public class DataParticleRotate extends DataParticle {

    public final DataParticle dataParticle;
    public final DataVec3 rotate;

    public DataParticleRotate(DataParticle dataParticle, DataVec3 rotate) {
        super();
        this.dataParticle = dataParticle;
        this.rotate = rotate;
    }

    public DataParticleRotate(DataParticleManager dataParticleManager, DataInputStream dataInputStream) throws IOException {
        super(dataInputStream);
        this.dataParticle = dataParticleManager.getDataParticle(dataInputStream.readInt());
        this.rotate = DataVec3.readFromFile(dataInputStream);
    }

    @Override
    public int getType() {
        return ROTATE;
    }

    @Override
    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataOutputStream);
        dataOutputStream.writeInt(dataParticle.id);
        rotate.writeToFile(dataOutputStream);
    }

    @Override
    public Stream<DataParticle> getChildren() {
        return Stream.of(dataParticle);
    }

    @Override
    public Matrix getPositionMatrix(int tick, int age) {
        return Matrix.rotateXYZ(rotate.getVec3(tick, age));
    }
}
