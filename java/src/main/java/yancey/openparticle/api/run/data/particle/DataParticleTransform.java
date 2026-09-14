package yancey.openparticle.api.run.data.particle;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import yancey.openparticle.api.run.data.DataParticleManager;
import yancey.openparticle.api.run.data.color.DataColor;
import yancey.openparticle.api.run.data.matrix.DataMatrix;
import yancey.openparticle.api.run.data.matrix.DataMatrixFree;
import yancey.openparticle.api.run.data.matrix.DataMatrixStatic;
import yancey.openparticle.api.run.math.Matrix;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class DataParticleTransform extends DataParticle {

    @NotNull
    public DataParticle child;
    @Nullable
    public DataMatrix dataMatrix;
    @Nullable
    public DataColor dataColor;
    public int tickAdd;

    public DataParticleTransform(@NotNull DataParticle child, @Nullable DataMatrix dataMatrix, @Nullable DataColor dataColor, int tickAdd) {
        this.child = child;
        this.dataMatrix = dataMatrix;
        this.dataColor = dataColor;
        this.tickAdd = tickAdd;
        if (tickAdd < 0) {
            throw new RuntimeException("tickAdd should not be negative");
        }
    }

    public DataParticleTransform(DataParticleManager dataParticleManager, DataInputStream dataInputStream) throws IOException {
        this.child = dataParticleManager.getDataParticle(dataInputStream.readInt());
        this.dataMatrix = DataMatrix.readFromFile(dataInputStream);
        this.dataColor = DataColor.readFromFile(dataInputStream);
        this.tickAdd = dataInputStream.readInt();
    }

    @Override
    public int getType() {
        return TRANSFORM;
    }

    @Override
    public void writeToFile(DataParticleManager dataParticleManager, DataOutputStream dataOutputStream) throws IOException {
        super.writeToFile(dataParticleManager, dataOutputStream);
        dataOutputStream.writeInt(child.id);
        DataMatrix.writeToFile(dataOutputStream, dataMatrix);
        DataColor.writeToFile(dataOutputStream, dataColor);
        dataOutputStream.writeInt(tickAdd);
    }

    @Override
    public int getParticleCount() {
        return child.getParticleCount();
    }

    @Override
    public void collect(List<DataParticle> dataParticleList) {
        if (!dataParticleList.contains(this)) {
            child.collect(dataParticleList);
            dataParticleList.add(this);
        }
    }

    @Override
    @SuppressWarnings("DataFlowIssue")
    public void optimize() {
        if (child instanceof DataParticleCompound compoundChild) {
            if (compoundChild.children.length > 1) {
                return;
            }
            child = compoundChild.children[0];
        }
        if (child instanceof DataParticleTransform transformChild) {
            if (transformChild.dataMatrix != null) {
                if (dataMatrix == null) {
                    dataMatrix = transformChild.dataMatrix;
                } else {
                    int age = 0;
                    if (dataMatrix.getType() == DataMatrix.FREE) {
                        age = ((DataMatrixFree) dataMatrix).matrices.length;
                    } else if (transformChild.dataMatrix.getType() == DataMatrix.FREE) {
                        age = ((DataMatrixFree) transformChild.dataMatrix).matrices.length;
                    }
                    if (age == 0) {
                        dataMatrix = new DataMatrixStatic(Matrix.multiply(
                                ((DataMatrixStatic) dataMatrix).matrix,
                                ((DataMatrixStatic) transformChild.dataMatrix).matrix
                        ));
                    } else {
                        Matrix[] matrices = new Matrix[age];
                        for (int i = 0; i < matrices.length; i++) {
                            matrices[i] = Matrix.multiply(
                                    dataMatrix.getMatrix(i),
                                    transformChild.dataMatrix.getMatrix(i)
                            );
                        }
                        dataMatrix = new DataMatrixFree(matrices);
                    }
                }
            }
            if (dataColor == null) {
                dataColor = transformChild.dataColor;
            }
            tickAdd += transformChild.tickAdd;
            child = transformChild.child;
        }
    }

    @Override
    public void setId(int id) {
        this.id = id;
        if (child.id > id) {
            throw new RuntimeException("child id is greater than id");
        }
        if (child.id == -1) {
            throw new RuntimeException("child id is less than id");
        }
    }

    @Override
    public @NotNull Matrix getTransform(int tick) {
        return dataMatrix == null ? Matrix.ZERO : dataMatrix.getMatrix(tick);
    }

    @Override
    public int getColor(int tick) {
        return dataColor == null ? 0 : dataColor.getColor(tick);
    }

}
