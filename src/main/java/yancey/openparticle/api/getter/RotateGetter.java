package yancey.openparticle.api.getter;

import yancey.openparticle.api.math.Vec3;

public abstract class RotateGetter {

    public static RotateGetter radians(Vec3 degree) {
        return new RotateGetter() {
            @Override
            public Vec3 getRotate(double tick) {
                return degree.copy();
            }
        };
    }

    public static RotateGetter degree(Vec3 degree) {
        return radians(degree.degreesToRadians());
    }

    public static RotateGetter addRadiansPerTick(Vec3 radiansAddPerTick) {
        return new RotateGetter() {
            @Override
            public Vec3 getRotate(double tick) {
                return new Vec3(tick * radiansAddPerTick.x, tick * radiansAddPerTick.y,tick * radiansAddPerTick.z);
            }
        };
    }

    public static RotateGetter addDegreePerTick(Vec3 degreesAddPerTick) {
        return addRadiansPerTick(degreesAddPerTick.degreesToRadians());
    }

    public abstract Vec3 getRotate(double tick);

}
