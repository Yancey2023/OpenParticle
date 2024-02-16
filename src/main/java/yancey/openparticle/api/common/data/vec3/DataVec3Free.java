package yancey.openparticle.api.common.data.vec3;

import yancey.openparticle.api.common.math.Vec3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataVec3Free extends DataVec3 {

    private final Vec3[] vec3List;

    public DataVec3Free(Vec3[] vec3List) {
        this.vec3List = vec3List;
    }

    public DataVec3Free(DataInputStream dataInputStream) throws IOException {
        this.vec3List = new Vec3[dataInputStream.readInt()];
        for (int i = 0; i < this.vec3List.length; i++) {
            this.vec3List[i] = new Vec3(dataInputStream);
        }
    }

    protected byte getType() {
        return FREE;
    }

    @Override
    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataOutputStream);
        dataOutputStream.writeInt(vec3List.length);
        for (Vec3 position : vec3List) {
            position.writeToFile(dataOutputStream);
        }
    }

    @Override
    public Vec3 getVec3(int tick, int age) {
        return vec3List[Math.min(tick, vec3List.length - 1)];
    }
}
