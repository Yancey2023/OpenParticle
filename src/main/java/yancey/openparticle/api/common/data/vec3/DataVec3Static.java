package yancey.openparticle.api.common.data.vec3;

import yancey.openparticle.api.common.math.Vec3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataVec3Static extends DataVec3 {

    private final Vec3 position;

    public DataVec3Static(Vec3 position) {
        this.position = position;
    }

    public DataVec3Static(DataInputStream dataInputStream) throws IOException {
        this.position = new Vec3(dataInputStream);
    }

    protected byte getType() {
        return STATIC;
    }

    @Override
    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataOutputStream);
        position.writeToFile(dataOutputStream);
    }

    @Override
    public Vec3 getVec3(int tick, int age) {
        return position;
    }
}
