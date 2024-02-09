package yancey.openparticle.api.common.data.vec3;

import yancey.openparticle.api.common.math.Vec3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataVec3Free extends DataVec3 {

    private final Vec3[] positions;

    public DataVec3Free(Vec3[] positions) {
        this.positions = positions;
    }

    public DataVec3Free(DataInputStream dataInputStream) throws IOException {
        this.positions = new Vec3[dataInputStream.readInt()];
        for (int i = 0; i < this.positions.length; i++) {
            this.positions[i] = new Vec3(dataInputStream);
        }
    }

    protected byte getType() {
        return FREE;
    }

    @Override
    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataOutputStream);
        dataOutputStream.writeInt(positions.length);
        for (Vec3 position : positions) {
            position.writeToFile(dataOutputStream);
        }
    }

    @Override
    public Vec3 getVec3(int tick, int age) {
        return positions[Math.min(tick, positions.length - 1)];
    }
}
