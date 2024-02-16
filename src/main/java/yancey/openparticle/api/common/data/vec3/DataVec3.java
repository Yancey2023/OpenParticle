package yancey.openparticle.api.common.data.vec3;

import yancey.openparticle.api.common.math.Vec3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class DataVec3 {

    public static final int STATIC = 0;
    public static final int SIMPLE = 1;
    public static final int FREE = 2;

    public static DataVec3 readFromFile(DataInputStream dataInputStream) throws IOException {
        byte type = dataInputStream.readByte();
        return switch (type) {
            case STATIC -> new DataVec3Static(dataInputStream);
            case SIMPLE -> new DataVec3Simple(dataInputStream);
            case FREE -> new DataVec3Free(dataInputStream);
            default -> throw new IllegalStateException("未知的位置类型: " + type);
        };
    }

    protected abstract byte getType();

    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(getType());
    }

    public abstract Vec3 getVec3(int tick, int age);

    public Vec3 getCurrentStaticPosition() {
        return null;
    }
}
