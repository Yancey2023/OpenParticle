package yancey.openparticle.test;

import org.jetbrains.annotations.NotNull;
import yancey.openparticle.api.activity.ActivityBase;
import yancey.openparticle.api.controller.Controller;
import yancey.openparticle.api.getter.ColorGetter;
import yancey.openparticle.api.getter.PosGetter;
import yancey.openparticle.api.getter.TimeGetter;
import yancey.openparticle.api.math.Vec3;
import yancey.openparticle.api.particle.Particles;

import java.awt.*;

public class MyActivity extends ActivityBase {

    @NotNull
    @Override
    public Controller[] run(Vec3 center) {
        Controller controller = Controller.fromParticle(
                Particles.END_ROD,
                TimeGetter.get(0),
                60,
                PosGetter.pos(center),
                ColorGetter.transform(Color.BLUE, Color.RED)
        );
        return new Controller[]{controller};
    }
}
