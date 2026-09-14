package yancey.openparticle.api.particle;

import yancey.openparticle.api.run.data.color.DataColor;
import yancey.openparticle.api.run.data.color.DataColorFree;
import yancey.openparticle.api.run.data.color.DataColorStatic;
import yancey.openparticle.api.run.data.matrix.DataMatrix;
import yancey.openparticle.api.run.data.matrix.DataMatrixFree;
import yancey.openparticle.api.run.data.matrix.DataMatrixStatic;
import yancey.openparticle.api.run.data.particle.DataParticle;
import yancey.openparticle.api.run.math.MathUtil;
import yancey.openparticle.api.run.math.Matrix;
import yancey.openparticle.api.run.math.Vec3;
import yancey.openparticle.api.run.util.ColorUtil;
import yancey.openparticle.api.util.version.Version;

import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

public abstract class Particle {

    protected boolean isUseCache = false;
    private DataParticle cache;
    public final int tick;
    public final int age;

    public Particle(int tick, int age) {
        if (age == 0) {
            throw new IllegalArgumentException("Age must be greater than zero");
        }
        this.tick = tick;
        this.age = age;
    }

    protected abstract DataParticle run(Version version);

    public DataParticle runWithCache(Version version) {
        if (isUseCache && cache != null) {
            return cache;
        }
        DataParticle dataParticle = run(version);
        if (isUseCache) {
            cache = dataParticle;
        }
        return dataParticle;
    }

    public Particle useCache(boolean isUseCache) {
        this.isUseCache = isUseCache;
        return this;
    }

    public Particle repeat(int times) {
        List<Particle> children = new ArrayList<>();
        for (int i = 0; i < times; i++) {
            children.add(this);
        }
        return ParticleCompound.of(children).useCache(isUseCache);
    }

    public Particle compound(Function<Particle, List<Particle>> function) {
        return compound(function.apply(this));
    }

    public static Particle compound(List<Particle> children) {
        boolean isUseCache = true;
        for (Particle particle : children) {
            if (!particle.isUseCache) {
                isUseCache = false;
                break;
            }
        }
        return ParticleCompound.of(children).useCache(isUseCache);
    }

    public static Particle compound(Particle... children) {
        return compound(List.of(children));
    }

    /**
     * 使用这个方法会对每一个经过方法的粒子重新计算，所以支持随机
     */
    public Particle apply(Function<Particle, Particle> function) {
        return new ParticleFunction(this, function).useCache(false);
    }

    public Particle transform(DataMatrix dataMatrix) {
        return new ParticleTransform(this, dataMatrix, null, 0).useCache(isUseCache);
    }

    public Particle transform(Matrix matrix) {
        return transform(new DataMatrixStatic(matrix));
    }

    public Particle transform(Matrix[] matrices) {
        if (matrices.length != age) {
            throw new RuntimeException("matrices.length != age");
        }
        return transform(new DataMatrixFree(matrices));
    }

    public Particle offset(Vec3 offset) {
        return transform(Matrix.offset(offset));
    }

    public Particle offset(float x, float y, float z) {
        return offset(new Vec3(x, y, z));
    }

    public Particle offset(Vec3[] vec3List) {
        if (vec3List.length != age) {
            throw new RuntimeException("vec3List.length != age");
        }
        return transform(Arrays.stream(vec3List).map(Matrix::offset).toArray(Matrix[]::new));
    }

    public Particle offsetSpeed(float x, float y, float z, float vx, float vy, float vz) {
        Matrix[] matrices = new Matrix[age];
        for (int i = 0; i < age; i++) {
            matrices[i] = Matrix.offset(x + vx * i, y + vy * i, z + vz * i);
        }
        return transform(matrices);
    }

    public Particle offsetSpeed(Vec3 offset, Vec3 speed) {
        return offsetSpeed(offset.x, offset.y, offset.z, speed.x, speed.y, speed.z);
    }

    public Particle rotate(Vec3 rotate) {
        return transform(new DataMatrixStatic(Matrix.rotateXYZ(rotate)));
    }

    public Particle rotate(float x, float y, float z) {
        return rotate(new Vec3(x, y, z));
    }

    public Particle rotateSpeed(float x, float y, float z, float vx, float vy, float vz) {
        Matrix[] matrices = new Matrix[age];
        for (int i = 0; i < age; i++) {
            matrices[i] = Matrix.rotateXYZ(x + vx * i, y + vy * i, z + vz * i);
        }
        return transform(matrices);
    }

    public Particle rotateSpeed(Vec3 rotate, Vec3 speed) {
        return offsetSpeed(rotate.x, rotate.y, rotate.z, speed.x, speed.y, speed.z);
    }

    public Particle rotateFree(Vec3[] vec3List) {
        return transform(Arrays.stream(vec3List).map(Matrix::rotateXYZ).toArray(Matrix[]::new));
    }

    public Particle tick(int tick) {
        return new ParticleTransform(this, null, null, tick).useCache(isUseCache);
    }

    public Particle color(DataColor color) {
        return new ParticleTransform(this, null, color, 0).useCache(isUseCache);
    }

    public Particle color(Color color) {
        return color(new DataColorStatic(color.getRGB()));
    }

    public Particle color(int color) {
        return color(new DataColorStatic(color));
    }

    public Particle colorFree(int... color) {
        if (color.length != age) {
            throw new RuntimeException("color.length != age");
        }
        return color(new DataColorFree(color));
    }

    public Particle colorFree(Color... color) {
        return colorFree(Arrays.stream(color).mapToInt(Color::getRGB).toArray());
    }

    public Particle colors(int... colors) {
        return colorFree(IntStream.iterate(0, tick -> tick + 1).limit(age).map(tick -> {
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
        }).toArray());
    }

    public Particle colors(Color... colors) {
        return colors(Arrays.stream(colors).mapToInt(Color::getRGB).toArray());
    }

}
