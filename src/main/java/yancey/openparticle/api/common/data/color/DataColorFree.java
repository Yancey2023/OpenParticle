package yancey.openparticle.api.common.data.color;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataColorFree extends DataColor {

    private final int[] colors;

    public DataColorFree(int[] colors) {
        this.colors = colors;
    }

    public DataColorFree(DataInputStream dataInputStream) throws IOException {
        this.colors = new int[dataInputStream.readInt()];
        for (int i = 0; i < this.colors.length; i++) {
            this.colors[i] = dataInputStream.readInt();
        }
    }

    protected byte getType() {
        return FREE;
    }

    @Override
    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataOutputStream);
        for (int color : colors) {
            dataOutputStream.writeInt(color);
        }
    }

    @Override
    public int getColor(int tick, int age) {
        return colors[Math.min(tick, colors.length - 1)];
    }
}
