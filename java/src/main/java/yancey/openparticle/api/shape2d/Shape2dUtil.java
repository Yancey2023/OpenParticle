package yancey.openparticle.api.shape2d;

import java.util.ArrayList;
import java.util.List;

public class Shape2dUtil {

    public static List<Float> line(float end, float count) {
        float interval = end / (count - 1);
        List<Float> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(interval * i);
        }
        return result;
    }

    public static List<Float> line(float start, float end, float count) {
        return line(end - start, count).stream().map(num -> num + start).toList();
    }

}
