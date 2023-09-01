package yancey.openparticle.api.activity;

import org.jetbrains.annotations.NotNull;
import yancey.openparticle.api.controller.Controller;
import yancey.openparticle.api.math.Vec3;

public abstract class ActivityBase {

    @NotNull
    public abstract Controller[] run(Vec3 center);
}
