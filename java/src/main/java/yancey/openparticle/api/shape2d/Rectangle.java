package yancey.openparticle.api.shape2d;

import yancey.openparticle.api.particle.Particle;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

/**
 * 长方形
 */
public class Rectangle {

    public enum Type {
        VERTICES,//顶点
        BORDER,//边框
        HOLLOW,//空心
        SOLID,//实心
    }

    private float length = -1, width = -1, interval = -1;
    private int count = -1;
    private Type type = Type.BORDER;

    private Rectangle() {
    }

    public static Rectangle create() {
        return new Rectangle();
    }

    public Rectangle shape(float length, float width) {
        this.length = length;
        this.width = width;
        return this;
    }

    public Rectangle type(Type type) {
        this.type = type;
        return this;
    }

    public Rectangle interval(float interval) {
        this.interval = interval;
        return this;
    }

    public Rectangle count(int count) {
        this.count = count;
        return this;
    }

    public Function<Particle, List<Particle>> build() {
        if (length == -1 || width == -1) {
            throw new RuntimeException("长方体参数缺失: 没有长宽高");
        }
        if (type == Type.VERTICES) {
            if (interval != -1 && count != -1) {
                throw new RuntimeException("长方形参数多余: 类型为顶点的时候不需要粒子间隔和粒子数量");
            }
        } else {
            if (interval == -1 && count == -1) {
                throw new RuntimeException("长方形参数缺失: 需要粒子间隔或粒子数量");
            }
            if (interval != -1 && count != -1) {
                throw new RuntimeException("长方形参数冲突: 不能同时传入粒子间隔和粒子数量");
            }
            if (interval == -1) {
                switch (type) {
                    case BORDER, HOLLOW -> interval = (length + width) * 2 / count;
                    case SOLID -> interval = (float) Math.sqrt(length * width / count);
                }
            }
        }
        float halfX = length / 2;
        float halfY = width / 2;
        switch (type) {
            case VERTICES -> {
                return particle -> List.of(
                        particle.offset(-halfX, 0, -halfY),
                        particle.offset(-halfX, 0, -halfY),
                        particle.offset(halfX, 0, -halfY),
                        particle.offset(-halfX, 0, halfY)
                );
            }
            case BORDER, HOLLOW, SOLID -> {
                List<Float> numsX = Shape2dUtil.line(-halfX, halfX, length / interval + 1);
                List<Float> numsY = Shape2dUtil.line(-halfY, halfY, width / interval + 1);
                if (type == Type.SOLID) {
                    return particle -> numsX.stream().flatMap(x -> numsY.stream().map(y -> particle.offset(x, 0, y))).toList();
                } else {
                    return particle -> Stream.of(
                            numsX.stream().map(x -> particle.offset(x, 0, -halfY)),
                            numsX.stream().map(x -> particle.offset(x, 0, halfY)),
                            numsY.stream().map(y -> particle.offset(-halfX, 0, y)),
                            numsY.stream().map(y -> particle.offset(halfX, 0, y))
                    ).flatMap(stream -> stream).toList();
                }
            }
            default -> throw new RuntimeException("未知的类型 -> " + type);
        }
    }

}
