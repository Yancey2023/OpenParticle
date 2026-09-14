package yancey.openparticle.api.run.data.color;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class DataColor {

    public static final int NONE = 0;
    public static final int STATIC = 1;
    public static final int FREE = 2;

    public static DataColor readFromFile(DataInputStream dataInputStream) throws IOException {
        byte type = dataInputStream.readByte();
        return switch (type) {
            case NONE -> null;
            case STATIC -> new DataColorStatic(dataInputStream);
            case FREE -> new DataColorFree(dataInputStream);
            default -> throw new IllegalStateException("未知的颜色类型: " + type);
        };
    }

    protected abstract byte getType();

    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(getType());
    }

    public static void writeToFile(DataOutputStream dataOutputStream, DataColor dataColor) throws IOException {
        if (dataColor == null) {
            dataOutputStream.writeByte(NONE);
        } else {
            dataColor.writeToFile(dataOutputStream);
        }
    }

    public abstract int getColor(int tick);

    public Integer getCurrentStaticColor() {
        return null;
    }
}
