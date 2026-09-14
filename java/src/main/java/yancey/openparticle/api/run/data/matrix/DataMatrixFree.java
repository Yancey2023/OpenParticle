package yancey.openparticle.api.run.data.matrix;

import yancey.openparticle.api.run.math.Matrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataMatrixFree extends DataMatrix {

    public final Matrix[] matrices;

    public DataMatrixFree(Matrix[] matrices) {
        this.matrices = matrices;
    }

    public DataMatrixFree(DataInputStream dataInputStream) throws IOException {
        this.matrices = new Matrix[dataInputStream.readInt()];
        for (int i = 0; i < this.matrices.length; i++) {
            this.matrices[i] = new Matrix(dataInputStream);
        }
    }

    public byte getType() {
        return FREE;
    }

    @Override
    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataOutputStream);
        dataOutputStream.writeInt(matrices.length);
        for (Matrix matrix : matrices) {
            matrix.writeToFile(dataOutputStream);
        }
    }

    @Override
    public Matrix getMatrix(int tick) {
        if (tick >= matrices.length) {
            System.out.println("[DataMatrixFree] 警告：存储了" + matrices.length + "tick的数据，但是访问了第" + tick + "的数据");
            return matrices[matrices.length - 1];
        }
        return matrices[tick];
    }

    @Override
    public Matrix getCurrentStaticTransform() {
        return null;
    }
}
