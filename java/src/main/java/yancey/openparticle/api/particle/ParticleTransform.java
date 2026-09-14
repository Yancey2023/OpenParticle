package yancey.openparticle.api.particle;

import yancey.openparticle.api.run.data.color.DataColor;
import yancey.openparticle.api.run.data.matrix.DataMatrix;
import yancey.openparticle.api.run.data.matrix.DataMatrixFree;
import yancey.openparticle.api.run.data.matrix.DataMatrixStatic;
import yancey.openparticle.api.run.data.particle.DataParticle;
import yancey.openparticle.api.run.data.particle.DataParticleTransform;
import yancey.openparticle.api.run.math.Matrix;
import yancey.openparticle.api.util.version.Version;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class ParticleTransform extends Particle {

    private final Particle particle;
    private final DataMatrix dataMatrix;
    private final DataColor dataColor;
    private final int tickAdd;

    public ParticleTransform(Particle particle, DataMatrix dataMatrix, DataColor dataColor, int tickAdd) {
        super(particle.tick, particle.age);
        this.particle = particle;
        this.dataMatrix = dataMatrix;
        this.dataColor = dataColor;
        this.tickAdd = tickAdd;
    }

    @Override
    protected DataParticle run(Version version) {
        // 为了优化性能，在这里对矩阵转换进行预处理
        List<DataMatrix> dataMatrixList = new ArrayList<>();
        DataColor dataColorResult = null;
        int tickAddResult = 0;
        Particle current = this;
        while (current instanceof ParticleTransform particleTransform) {
            if (particle.isUseCache) {
                isUseCache = true;
            }
            if (particleTransform.dataMatrix != null) {
                dataMatrixList.add(particleTransform.dataMatrix);
            }
            if (dataColorResult == null) {
                dataColorResult = particleTransform.dataColor;
            }
            tickAddResult += particleTransform.tickAdd;
            current = particleTransform.particle;
        }
        byte dataMatrixType = DataMatrix.NONE;
        for (DataMatrix dataMatrix : dataMatrixList) {
            if (dataMatrixType == DataMatrix.NONE && dataMatrix.getType() == DataMatrix.STATIC) {
                dataMatrixType = DataMatrix.STATIC;
            }
            if (dataMatrix.getType() == DataMatrix.FREE) {
                dataMatrixType = DataMatrix.FREE;
                break;
            }
        }
        DataMatrix dataMatrixResult = switch (dataMatrixType) {
            case DataMatrix.NONE -> null;
            case DataMatrix.STATIC -> new DataMatrixStatic(Matrix.multiplyAll(dataMatrixList.stream()
                    .map(DataMatrix::getCurrentStaticTransform)
                    .toArray(Matrix[]::new)));
            case DataMatrix.FREE -> new DataMatrixFree(IntStream.iterate(0, tick -> tick + 1).limit(age)
                    .mapToObj(tick -> Matrix.multiplyAll(dataMatrixList.stream()
                            .map(dataMatrix -> dataMatrix.getMatrix(tick))
                            .toArray(Matrix[]::new))).toArray(Matrix[]::new));
            default -> throw new IllegalStateException("Unexpected value: " + dataMatrixType);
        };
        return new DataParticleTransform(current.runWithCache(version), dataMatrixResult, dataColorResult, tickAddResult);
    }

}
