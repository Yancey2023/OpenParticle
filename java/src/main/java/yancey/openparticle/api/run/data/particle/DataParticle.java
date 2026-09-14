package yancey.openparticle.api.run.data.particle;

import org.jetbrains.annotations.NotNull;
import yancey.openparticle.api.run.data.DataParticleManager;
import yancey.openparticle.api.run.math.Matrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public abstract class DataParticle {

    public static final byte SINGLE = 0;
    public static final byte COMPOUND = 1;
    public static final byte TRANSFORM = 2;
    public int id = -1;

    public static DataParticle readFromFile(DataParticleManager dataParticleManager, DataInputStream dataInputStream) throws IOException {
        byte type = dataInputStream.readByte();
        return switch (type) {
            case SINGLE -> new DataParticleSingle(dataParticleManager, dataInputStream);
            case COMPOUND -> new DataParticleCompound(dataParticleManager, dataInputStream);
            case TRANSFORM -> new DataParticleTransform(dataParticleManager, dataInputStream);
            default -> throw new IllegalStateException("未知的粒子类型: " + type);
        };
    }

    public abstract int getType();

    public void writeToFile(DataParticleManager dataParticleManager, DataOutputStream dataOutputStream) throws IOException {
        dataOutputStream.writeByte(getType());
    }

    public abstract void collect(List<DataParticle> dataParticleList);

    public abstract int getParticleCount();

    public abstract void optimize();

    public void setId(int id) {
        this.id = id;
    }

    public @NotNull Matrix getTransform(int tick) {
        return Matrix.ZERO;
    }

    public int getColor(int tick) {
        return 0;
    }

}
