package yancey.openparticle.api.common.math;

public class MathUtil {

    public static float distance(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    public static float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

}
