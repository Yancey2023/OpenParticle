package yancey.openparticle.api.run.data.matrix;

import yancey.openparticle.api.run.math.Matrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class DataMatrix {

    public static final byte NONE = 0;
    public static final byte STATIC = 1;
    public static final byte FREE = 2;

    public static DataMatrix readFromFile(DataInputStream dataInputStream) throws IOException {
        byte type = dataInputStream.readByte();
        return switch (type) {
            case NONE -> null;
            case STATIC -> new DataMatrixStatic(dataInputStream);
            case FREE -> new DataMatrixFree(dataInputStream);
            default -> throw new IllegalStateException("未知的三维向量类型: " + type);
        };
    }

    public abstract byte getType();

    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(getType());
    }

    public static void writeToFile(DataOutputStream dataOutputStream, DataMatrix dataMatrix) throws IOException {
        if (dataMatrix == null) {
            dataOutputStream.writeByte(NONE);
        } else {
            dataMatrix.writeToFile(dataOutputStream);
        }
    }

    public abstract Matrix getMatrix(int tick);

    public abstract Matrix getCurrentStaticTransform();
}
