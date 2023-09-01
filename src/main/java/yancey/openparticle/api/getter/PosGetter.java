package yancey.openparticle.api.getter;

import yancey.openparticle.api.math.Vec3;

public abstract class PosGetter {

    //不变
    public static PosGetter pos(Vec3 pos) {
        return new PosGetter() {
            @Override
            public Vec3 get(int tick, int maxAge) {
                return pos.copy();
            }
        };
    }

    public static PosGetter moveLineFromZero(Vec3 posEnd) {
        return new PosGetter() {
            @Override
            public Vec3 get(int tick, int maxAge) {
                double a = (double) tick / maxAge;
                return new Vec3(a * posEnd.x, a * posEnd.y, a * posEnd.z);
            }
        };
    }

    //匀速直线运动
    public static PosGetter moveLine(Vec3 posStart, Vec3 posEnd) {
        return new PosGetter() {
            @Override
            public Vec3 get(int tick, int maxAge) {
                return moveLineFromZero(posEnd.remove(posStart)).get(tick, maxAge).addSelf(posStart);
            }
        };
    }

    public abstract Vec3 get(int tick, int maxAge);

}
