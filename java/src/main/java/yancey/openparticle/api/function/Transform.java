package yancey.openparticle.api.function;

import yancey.openparticle.api.run.math.Vec3;

import java.awt.*;

public interface Transform<T> {

    T transform(int time, T start, T end, int duration);

    static Transform<Vec3> easePosition(EasingFunction easingFunction) {
        return (time, start, end, duration) -> new Vec3(
                (float) easingFunction.apply(time, start.x, end.x - start.x, duration),
                (float) easingFunction.apply(time, start.y, end.y - start.y, duration),
                (float) easingFunction.apply(time, start.z, end.z - start.z, duration)
        );
    }

    static Transform<Color> easeColor(EasingFunction easingFunction) {
        return (time, start, end, duration) -> new Color(
                (int) easingFunction.apply(time, start.getRed(), end.getRed() - start.getRed(), duration),
                (int) easingFunction.apply(time, start.getGreen(), end.getGreen() - start.getGreen(), duration),
                (int) easingFunction.apply(time, start.getBlue(), end.getBlue() - start.getBlue(), duration)
        );
    }

}
