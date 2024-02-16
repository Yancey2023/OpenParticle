package yancey.openparticle.api.common.data.color;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataColorStatic extends DataColor {

    private final int color;

    public DataColorStatic(int color) {
        this.color = color;
    }

    public DataColorStatic(DataInputStream dataInputStream) throws IOException {
        this.color = dataInputStream.readInt();
    }

    protected byte getType() {
        return STATIC;
    }

    @Override
    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataOutputStream);
        dataOutputStream.writeInt(color);
    }

    @Override
    public int getColor(int tick, int age) {
        return color;
    }

    @Override
    public Integer getCurrentStaticColor() {
        return color;
    }
}
