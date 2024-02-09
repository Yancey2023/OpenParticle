package yancey.openparticle.api.common.data.color;

import yancey.openparticle.api.common.math.MathUtil;
import yancey.openparticle.api.common.util.ColorUtil;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataColorSimple extends DataColor {

    private final int[] colors;

    public DataColorSimple(int[] colors) {
        this.colors = colors;
    }

    public DataColorSimple(DataInputStream dataInputStream) throws IOException {
        this.colors = new int[dataInputStream.readInt()];
        for (int i = 0; i < this.colors.length; i++) {
            this.colors[i] = dataInputStream.readInt();
        }
    }

    protected byte getType() {
        return SIMPLE;
    }

    @Override
    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataOutputStream);
        dataOutputStream.writeInt(this.colors.length);
        for (int color : colors) {
            dataOutputStream.writeInt(color);
        }
    }

    @Override
    public int getColor(int tick, int age) {
        float a = (float) tick / age * (colors.length - 1);
        int which = (int) a;
        if (which + 1 >= colors.length) {
            return colors[colors.length - 1];
        }
        int start = colors[which];
        int end = colors[which + 1];
        float delta = a - which;
        return (((int) MathUtil.lerp(delta, ColorUtil.getAlpha(start), ColorUtil.getAlpha(end)) & 0xFF) << 24) |
                (((int) MathUtil.lerp(delta, ColorUtil.getRed(start), ColorUtil.getRed(end)) & 0xFF) << 16) |
                (((int) MathUtil.lerp(delta, ColorUtil.getGreen(start), ColorUtil.getGreen(end)) & 0xFF) << 8) |
                (((int) MathUtil.lerp(delta, ColorUtil.getBlue(start), ColorUtil.getBlue(end)) & 0xFF));
    }

}
