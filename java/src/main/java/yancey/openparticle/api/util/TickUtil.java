package yancey.openparticle.api.util;

public class TickUtil {

    /**
     * 通过视频时间获取游戏tick
     *
     * @param second 第几秒
     * @param frame  如果视频是60帧，这是第几帧
     */
    public static double getTickByTime(int second, int frame) {
        return second * 20 + (double) frame / 3;
    }

}
