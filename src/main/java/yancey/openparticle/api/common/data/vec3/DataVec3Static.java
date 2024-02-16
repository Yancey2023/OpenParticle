package yancey.openparticle.api.common.data.vec3;

import yancey.openparticle.api.common.math.Vec3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataVec3Static extends DataVec3 {

    private final Vec3 vec3;

    public DataVec3Static(Vec3 vec3) {
        this.vec3 = vec3;
    }

    public DataVec3Static(DataInputStream dataInputStream) throws IOException {
        this.vec3 = new Vec3(dataInputStream);
    }

    protected byte getType() {
        return STATIC;
    }

    @Override
    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataOutputStream);
        vec3.writeToFile(dataOutputStream);
    }

    @Override
    public Vec3 getVec3(int tick, int age) {
        return vec3;
    }

    @Override
    public Vec3 getCurrentStaticPosition() {
        return vec3;
    }
}
