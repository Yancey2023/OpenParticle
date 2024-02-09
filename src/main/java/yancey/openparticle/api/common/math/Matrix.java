package yancey.openparticle.api.common.math;

public class Matrix {

    public final static Matrix ZERO = Matrix.offset(0, 0, 0);
    private final float[] data;

    private Matrix(float... data) {
        if (data.length != 16) {
            throw new RuntimeException("暂时只支持4x4的矩阵");
        }
        this.data = data;
    }

    public static Matrix offset(float x, float y, float z) {
        return new Matrix(
                1, 0, 0, x,
                0, 1, 0, y,
                0, 0, 1, z,
                0, 0, 0, 1
        );
    }

    public static Matrix offset(Vec3 offset) {
        return offset(offset.x, offset.y, offset.z);
    }

    public static Matrix scale(float scaleX, float scaleY, float scaleZ) {
        return new Matrix(
                scaleX, 0, 0, 0,
                0, scaleY, 0, 0,
                0, 0, scaleZ, 0,
                0, 0, 0, 1
        );
    }

    public static Matrix scale(float scale) {
        return scale(scale, scale, scale);
    }

    public static Matrix rotateX(double radian) {
        float sin = (float) Math.sin(radian);
        float cos = (float) Math.cos(radian);
        return new Matrix(
                1, 0, 0, 0,
                0, cos, -sin, 0,
                0, sin, cos, 0,
                0, 0, 0, 1
        );
    }

    public static Matrix rotateY(double radian) {
        float sin = (float) Math.sin(radian);
        float cos = (float) Math.cos(radian);
        return new Matrix(
                cos, 0, -sin, 0,
                0, 1, 0, 0,
                sin, 0, cos, 0,
                0, 0, 0, 1
        );
    }

    public static Matrix rotateZ(double radian) {
        float sin = (float) Math.sin(radian);
        float cos = (float) Math.cos(radian);
        return new Matrix(
                cos, -sin, 0, 0,
                sin, cos, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1
        );
    }

    public static Matrix rotateXYZ(double x, double y, double z) {
        return multiplyAll(rotateX(x), rotateY(y), rotateZ(z));
    }

    public static Matrix rotateXYZ(Vec3 radius) {
        return rotateXYZ(radius.x, radius.y, radius.z);
    }

    /**
     * matrix1 * matrix2
     * 相当于先做矩阵2的变换，再做矩阵1的变换
     */
    public static Matrix multiply(Matrix matrix1, Matrix matrix2) {
        if (matrix1 == Matrix.ZERO) {
            return matrix2;
        } else if (matrix2 == Matrix.ZERO) {
            return matrix1;
        }
        float[] data = new float[16];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                for (int k = 0; k < 4; k++) {
                    data[4 * i + j] += matrix1.data[4 * i + k] * matrix2.data[4 * k + j];
                }
            }
        }
        return new Matrix(data);
    }

    public static Matrix multiplyAll(Matrix... matrices) {
        if (matrices == null || matrices.length == 0) {
            throw new RuntimeException("矩阵相乘至少要有一个矩阵");
        }
        Matrix matrix = matrices[0];
        if (matrices.length == 1) {
            return matrix;
        }
        for (int i = 1; i < matrices.length; i++) {
            matrix = matrix.multiply(matrices[i]);
        }
        return matrix;
    }

    public Matrix multiply(Matrix matrix) {
        return multiply(matrix, this);
    }

    /**
     * 将坐标进行矩阵变换
     */
    public Vec3 apply(Vec3 vec3) {
        float[] input = new float[]{vec3.x, vec3.y, vec3.z, 1};
        float[] output = new float[4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                output[i] += data[4 * i + j] * input[j];
            }
        }
        return new Vec3(output[0] / output[3],
                output[1] / output[3],
                output[2] / output[3]);
    }

    public Vec3 apply(float x, float y, float z) {
        return apply(new Vec3(x, y, z));
    }

}
