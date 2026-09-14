package yancey.openparticle.api.function;

/**
 * easeIn     &#09;  缓入                                              <br>
 * easeOut    &#09;  缓出                                              <br>
 * easeInOut  &#09;  缓入缓出                                            <br>
 * <p>
 * Linear     &#09;  无缓动效果                                           <br>
 * Quad       &#09;  二次方的缓动            &#09;     t^2                 <br>
 * Sine       &#09;  正弦曲线的缓动           &#09;     sin(t)              <br>
 * Expo       &#09;  指数曲线的缓动           &#09;     2^t                 <br>
 * Circ       &#09;  圆形曲线的缓动           &#09;     sqrt(1-t^2)         <br>
 * Cubic      &#09;  三次方的缓动            &#09;     t^3                 <br>
 * Quart      &#09;  四次方的缓动            &#09;     t^4                 <br>
 * Quint      &#09;  五次方的缓动            &#09;     t^5                 <br>
 * Elastic    &#09;  指数衰减的正弦曲线缓动                                     <br>
 * Back       &#09;  超过范围的三次方缓动        &#09;     (s+1)*t^3 - s*t^2   <br>
 * Bounce     &#09;  指数衰减的反弹缓动                                       <br>
 */
public interface EasingFunction {

    /**
     * @param time     动画开始以来经过的时间
     * @param begin    动画的起点
     * @param change   从起点到终点的差值
     * @param duration 完成动画所需的时间
     * @return 动画的当前位置
     */
    double apply(double time, double begin, double change, double duration);

    /**
     * Linear 线性
     */
    static double easeLinear(double time, double begin, double change, double duration) {
        return change * time / duration + begin;
    }

    /**
     * Quadratic 二次渐变
     */
    static double easeInQuad(double time, double begin, double change, double duration) {
        return change * (time /= duration) * time + time;
    }

    static double easeOutQuad(double time, double begin, double change, double duration) {
        return -change * (time /= duration) * (time - 2) + begin;
    }

    static double easeInOutQuad(double time, double begin, double change, double duration) {
        if ((time /= duration / 2) < 1) {
            return change / 2 * time * time + begin;
        }
        return -change / 2 * ((--time) * (time - 2) - 1) + begin;
    }

    /**
     * Sinusoidal 正弦渐变
     */
    static double easeInSine(double time, double begin, double change, double duration) {
        return -change * Math.cos(time / duration * (Math.PI / 2)) + change + begin;
    }

    static double easeOutSine(double time, double begin, double change, double duration) {
        return change * Math.sin(time / duration * (Math.PI / 2)) + begin;
    }

    static double easeInOutSine(double time, double begin, double change, double duration) {
        return -change / 2 * (Math.cos(Math.PI * time / duration) - 1) + begin;
    }

    /**
     * Exponential 指数渐变
     */
    static double easeInExpo(double time, double begin, double change, double duration) {
        return (time == 0) ? begin : change * Math.pow(2, 10 * (time / duration - 1)) + begin;
    }

    static double easeOutExpo(double time, double begin, double change, double duration) {
        return (time == duration) ? begin + change : change * (-Math.pow(2, -10 * time / duration) + 1) + begin;
    }

    static double easeInOutExpo(double time, double begin, double change, double duration) {
        if (time == 0) {
            return begin;
        } else if (time == duration) {
            return begin + change;
        } else if ((time /= duration / 2) < 1) {
            return change / 2 * Math.pow(2, 10 * (time - 1)) + begin;
        } else {
            return change / 2 * (-Math.pow(2, -10 * --time) + 2) + begin;
        }
    }

    /**
     * Circular 圆形曲线
     */
    static double easeInCirc(double time, double begin, double change, double duration) {
        return -change * (Math.sqrt(1 - (time /= duration) * time) - 1) + begin;
    }

    static double easeOutCirc(double time, double begin, double change, double duration) {
        return change * Math.sqrt(1 - (time = time / duration - 1) * time) + begin;
    }

    static double easeInOutCirc(double time, double begin, double change, double duration) {
        if ((time /= duration / 2) < 1) {
            return -change / 2 * (Math.sqrt(1 - time * time) - 1) + begin;
        }
        return change / 2 * (Math.sqrt(1 - (time -= 2) * time) + 1) + begin;
    }

    /**
     * Cubic 三次方
     */
    static double easeInCubic(double time, double begin, double change, double duration) {
        return change * (time /= duration) * time * time + begin;
    }

    static double easeOutCubic(double time, double begin, double change, double duration) {
        return change * ((time = time / duration - 1) * time * time + 1) + begin;
    }

    static double easeInOutCubic(double time, double begin, double change, double duration) {
        if ((time /= duration / 2) < 1) return change / 2 * time * time * time + begin;
        return change / 2 * ((time -= 2) * time * time + 2) + begin;
    }

    /**
     * Quartic 四次方=
     */
    static double easeInQuart(double time, double begin, double change, double duration) {
        return change * (time /= duration) * time * time * time + begin;
    }

    static double easeOutQuart(double time, double begin, double change, double duration) {
        return -change * ((time = time / duration - 1) * time * time * time - 1) + begin;
    }

    static double easeInOutQuart(double time, double begin, double change, double duration) {
        if ((time /= duration / 2) < 1) return change / 2 * time * time * time * time + begin;
        return -change / 2 * ((time -= 2) * time * time * time - 2) + begin;
    }

    /**
     * Quintic 五次方
     */
    static double easeInQuint(double time, double begin, double change, double duration) {
        return change * (time /= duration) * time * time * time * time + begin;
    }

    static double easeOutQuint(double time, double begin, double change, double duration) {
        return change * ((time = time / duration - 1) * time * time * time * time + 1) + begin;
    }

    static double easeInOutQuint(double time, double begin, double change, double duration) {
        if ((time /= duration / 2) < 1) return change / 2 * time * time * time * time * time + begin;
        return change / 2 * ((time -= 2) * time * time * time * time + 2) + begin;
    }

    /**
     * Elastic 指数衰减正弦曲线
     */
    static double easeInElastic(double time, double begin, double change, double duration) {
        if (time == 0) {
            return begin;
        } else if ((time /= duration) == 1) {
            return begin + change;
        }
        double p = duration * 0.3;
        double s = change < Math.abs(change) ? p / 4 : p / (2 * Math.PI) * Math.asin(1);
        return -(change * Math.pow(2, 10 * (time -= 1)) * Math.sin((time * duration - s) * (2 * Math.PI) / p)) + begin;
    }

    static double easeOutElastic(double time, double begin, double change, double duration) {
        if (time == 0) {
            return begin;
        } else if ((time /= duration) == 1) {
            return begin + change;
        }
        double p = duration * 0.3;
        double s = change < Math.abs(change) ? p / 4 : p / (2 * Math.PI) * Math.asin(1);
        return change * Math.pow(2, -10 * time) * Math.sin((time * duration - s) * (2 * Math.PI) / p) + change + begin;
    }

    static double easeInOutElastic(double time, double begin, double change, double duration) {
        if (time == 0) {
            return begin;
        } else if ((time /= duration / 2) == 2) {
            return begin + change;
        }
        double p = duration * 0.45;
        double s = change < Math.abs(change) ? p / 4 : p / (2 * Math.PI) * Math.asin(1);
        if (time < 1) {
            return -.5 * (change * Math.pow(2, 10 * (time -= 1)) * Math.sin((time * duration - s) * (2 * Math.PI) / p)) + begin;
        }
        return change * Math.pow(2, -10 * (time -= 1)) * Math.sin((time * duration - s) * (2 * Math.PI) / p) * .5 + change + begin;
    }

    /**
     * Back
     */
    static double easeInBack(double time, double begin, double change, double duration) {
        double s = 1.70158;
        return change * (time /= duration) * time * ((s + 1) * time - s) + begin;
    }

    static double easeOutBack(double time, double begin, double change, double duration) {
        double s = 1.70158;
        return change * ((time = time / duration - 1) * time * ((s + 1) * time + s) + 1) + begin;
    }

    static double easeInOutBack(double time, double begin, double change, double duration) {
        double s = 1.70158;
        if ((time /= duration / 2) < 1) return change / 2 * (time * time * (((s *= (1.525)) + 1) * time - s)) + begin;
        return change / 2 * ((time -= 2) * time * (((s *= (1.525)) + 1) * time + s) + 2) + begin;
    }

    /**
     * Bounce
     */
    static double easeInBounce(double time, double begin, double change, double duration) {
        return change - easeOutBounce(duration - time, 0, change, duration) + begin;
    }

    static double easeOutBounce(double time, double begin, double change, double duration) {
        if ((time /= duration) < (1 / 2.75)) {
            return change * (7.5625 * time * time) + begin;
        } else if (time < (2 / 2.75)) {
            return change * (7.5625 * (time -= (1.5 / 2.75)) * time + .75) + begin;
        } else if (time < (2.5 / 2.75)) {
            return change * (7.5625 * (time -= (2.25 / 2.75)) * time + .9375) + begin;
        } else {
            return change * (7.5625 * (time -= (2.625 / 2.75)) * time + .984375) + begin;
        }
    }

    static double easeInOutBounce(double time, double begin, double change, double duration) {
        if (time < duration / 2) return easeInBounce(time * 2, 0, change, duration) * .5 + begin;
        return easeOutBounce(time * 2 - duration, 0, change, duration) * .5 + change * .5 + begin;
    }

}
