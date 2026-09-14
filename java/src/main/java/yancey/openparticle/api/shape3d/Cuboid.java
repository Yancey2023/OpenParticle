package yancey.openparticle.api.shape3d;

import yancey.openparticle.api.particle.Particle;
import yancey.openparticle.api.shape2d.Shape2dUtil;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class Cuboid {

    public enum Type {
        VERTICES,//顶点
        BORDER,//边框
        HOLLOW,//空心
        SOLID,//实心
    }

    private float length = -1, width = -1, height = -1, interval = -1;
    private int count = -1;
    private Type type;

    private Cuboid() {
    }

    public static Cuboid create() {
        return new Cuboid();
    }

    public Cuboid shape(float length, float width, float height) {
        this.length = length;
        this.width = width;
        this.height = height;
        return this;
    }

    public Cuboid shape(float side) {
        return shape(side, side, side);
    }

    public Cuboid type(Type type) {
        this.type = type;
        return this;
    }

    public Cuboid interval(float interval) {
        this.interval = interval;
        return this;
    }

    public Cuboid count(int count) {
        this.count = count;
        return this;
    }

    public Function<Particle, List<Particle>> build() {
        if (length == -1 || width == -1 || height == -1) {
            throw new RuntimeException("长方体参数缺失: 没有长宽高");
        }
        if (type == null) {
            throw new RuntimeException("长方体参数缺失: 没有类型");
        }
        if (type == Type.VERTICES) {
            if (interval != -1 && count != -1) {
                throw new RuntimeException("长方体参数多余: 类型为顶点的时候不需要粒子间隔和粒子数量");
            }
        } else {
            if (interval == -1 && count == -1) {
                throw new RuntimeException("长方体参数缺失: 需要粒子间隔或粒子数量");
            }
            if (interval != -1 && count != -1) {
                throw new RuntimeException("长方体参数冲突: 不能同时传入粒子间隔和粒子数量");
            }
            if (interval == -1) {
                switch (type) {
                    case BORDER -> interval = (length + width + height) * 4 / count;
                    case HOLLOW ->
                            interval = (float) Math.cbrt((length * width + width * height + length * height) * 2 / count);
                    case SOLID -> interval = (float) Math.cbrt(length * width * height / count);
                }
            }
        }
        float halfX = length / 2;
        float halfY = width / 2;
        float halfZ = height / 2;
        switch (type) {
            case VERTICES -> {
                return particle -> List.of(
                        particle.offset(-halfX, -halfY, -halfZ),
                        particle.offset(-halfX, -halfY, halfZ),
                        particle.offset(halfX, -halfY, -halfZ),
                        particle.offset(-halfX, halfY, -halfZ),
                        particle.offset(halfX, halfY, -halfZ),
                        particle.offset(-halfX, halfY, halfZ),
                        particle.offset(halfX, -halfY, halfZ),
                        particle.offset(halfX, halfY, halfZ)
                );
            }
            case BORDER, HOLLOW, SOLID -> {
                List<Float> numsX = Shape2dUtil.line(-halfX, halfX, length / interval + 1);
                List<Float> numsY = Shape2dUtil.line(-halfY, halfY, width / interval + 1);
                List<Float> numsZ = Shape2dUtil.line(-halfZ, halfZ, height / interval + 1);
                if (type == Type.BORDER) {
                    return particle -> Stream.of(
                            numsX.stream().map(x -> particle.offset(x, -halfY, -halfZ)),
                            numsX.stream().map(x -> particle.offset(x, halfY, -halfZ)),
                            numsX.stream().map(x -> particle.offset(x, -halfY, halfZ)),
                            numsX.stream().map(x -> particle.offset(x, halfY, halfZ)),
                            numsY.stream().map(y -> particle.offset(-halfX, y, -halfZ)),
                            numsY.stream().map(y -> particle.offset(halfX, y, -halfZ)),
                            numsY.stream().map(y -> particle.offset(-halfX, y, halfZ)),
                            numsY.stream().map(y -> particle.offset(halfX, y, halfZ)),
                            numsZ.stream().map(z -> particle.offset(-halfX, -halfY, z)),
                            numsZ.stream().map(z -> particle.offset(halfX, -halfY, z)),
                            numsZ.stream().map(z -> particle.offset(-halfX, halfY, z)),
                            numsZ.stream().map(z -> particle.offset(halfX, halfY, z))
                    ).flatMap(stream -> stream).toList();
                } else if (type == Type.HOLLOW) {
                    return particle -> Stream.of(
                            numsX.stream().flatMap(x -> numsY.stream().map(y -> particle.offset(x, y, -halfZ))),
                            numsX.stream().flatMap(x -> numsY.stream().map(y -> particle.offset(x, y, halfZ))),
                            numsX.stream().flatMap(x -> numsZ.stream().map(z -> particle.offset(x, -halfY, z))),
                            numsX.stream().flatMap(x -> numsZ.stream().map(z -> particle.offset(x, halfY, z))),
                            numsY.stream().flatMap(y -> numsZ.stream().map(z -> particle.offset(-halfX, y, z))),
                            numsY.stream().flatMap(y -> numsZ.stream().map(z -> particle.offset(halfX, y, z)))
                    ).flatMap(stream -> stream).toList();
                } else {
                    return particle -> numsX.stream().flatMap(x -> numsY.stream().flatMap(y -> numsZ.stream().map(z -> particle.offset(x, y, z)))).toList();
                }
            }
            default -> throw new RuntimeException("未知的类型 -> " + type);
        }
    }

}
