package yancey.openparticle.api.particle;

import yancey.openparticle.api.run.data.particle.DataParticle;
import yancey.openparticle.api.util.version.Version;

import java.util.function.Function;

public class ParticleFunction extends Particle {

    private final Particle particle;
    private final Function<Particle, Particle> function;

    public ParticleFunction(Particle particle, Function<Particle, Particle> function) {
        super(particle.tick, particle.age);
        this.particle = particle;
        this.function = function;
    }

    @Override
    protected DataParticle run(Version version) {
        Particle particle1 = function.apply(particle);
        if (particle1.age != age) {
            throw new RuntimeException("age与预期不符合，请检查你的代码");
        }
        return particle1.run(version);
    }
}
