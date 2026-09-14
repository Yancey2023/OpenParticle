package yancey.openparticle.api.shape2d;

import yancey.openparticle.api.particle.Particle;
import yancey.openparticle.api.run.math.Matrix;
import yancey.openparticle.api.run.math.Vec3;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class Line {
    private Vec3 start, end, rotate;
    private float length = -1, interval = -1;
    private int count = -1;

    private Line() {
    }

    public static Line create() {
        return new Line();
    }

    public Line start(Vec3 start) {
        this.start = start;
        return this;
    }

    public Line start(float x, float y, float z) {
        return start(new Vec3(x, y, z));
    }

    public Line end(Vec3 end) {
        this.end = end;
        return this;
    }

    public Line end(float x, float y, float z) {
        return end(new Vec3(x, y, z));
    }

    public Line rotate(Vec3 rotate) {
        this.rotate = rotate;
        return this;
    }

    public Line rotate(float x, float y, float z) {
        return rotate(new Vec3(x, y, z));
    }

    public Line length(float length) {
        this.length = length;
        return this;
    }

    public Line interval(float interval) {
        this.interval = interval;
        return this;
    }

    public Line count(int count) {
        this.count = count;
        return this;
    }

    public Function<Particle, List<Particle>> build() {
        if (length == -1 && rotate == null) {
            if (end == null) {
                throw new RuntimeException("直线参数缺失");
            }
            start = Objects.requireNonNullElse(start, Vec3.ZERO);
            length = start.distance(end);
            rotate = end.remove(start).getRadian();
            if (interval != -1 && count != -1) {
                throw new RuntimeException("直线参数冲突: 不能同时传入粒子间隔和粒子数量");
            }
        } else if (length != -1 && rotate != null) {
            if (start != null && end != null) {
                throw new RuntimeException("直线参数冲突");
            }
            start = Objects.requireNonNullElse(start, end == null ? Vec3.ZERO : end.remove(Matrix.rotateXYZ(rotate).apply(Vec3.ZERO)));
            if (interval != -1 && count != -1) {
                throw new RuntimeException("直线参数冲突: 不能同时传入粒子间隔和粒子数量");
            }
        } else if (count != -1 && interval != -1 && rotate != null) {
            length = interval * count;
            start = Objects.requireNonNullElse(start, Vec3.ZERO);
        } else {
            throw new RuntimeException("无法确认唯一的直线");
        }
        if (interval == -1 && count == -1) {
            throw new RuntimeException("直线参数缺失: 需要粒子间隔或粒子数量");
        }
        if (count == -1) {
            count = (int) (length / interval) + 1;
        }
        return particle -> {
            if (count <= 0) {
                throw new RuntimeException("直线的粒子数量为" + count);
            }
            Matrix matrix = Matrix.multiply(Matrix.offset(start), Matrix.rotateXYZ(rotate));
            return Shape2dUtil.line(length, count).stream()
                    .map(distance -> particle.offset(matrix.apply(new Vec3(distance, 0, 0))))
                    .toList();
        };
    }
}
