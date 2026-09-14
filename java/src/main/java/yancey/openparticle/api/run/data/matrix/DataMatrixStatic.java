package yancey.openparticle.api.run.data.matrix;

import yancey.openparticle.api.run.math.Matrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class DataMatrixStatic extends DataMatrix {

    public final Matrix matrix;

    public DataMatrixStatic(Matrix matrix) {
        this.matrix = matrix;
    }

    public DataMatrixStatic(DataInputStream dataInputStream) throws IOException {
        this.matrix = new Matrix(dataInputStream);
    }

    public byte getType() {
        return STATIC;
    }

    @Override
    public void writeToFile(DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataOutputStream);
        matrix.writeToFile(dataOutputStream);
    }

    @Override
    public Matrix getMatrix(int tick) {
        return matrix;
    }


    @Override
    public Matrix getCurrentStaticTransform() {
        return matrix;
    }
}
