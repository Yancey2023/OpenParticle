package yancey.openparticle.api.particle;

import yancey.openparticle.api.run.data.particle.DataParticle;
import yancey.openparticle.api.run.data.particle.DataParticleCompound;
import yancey.openparticle.api.util.version.Version;

import java.util.List;

public class ParticleCompound extends Particle {

    private final List<Particle> children;

    private ParticleCompound(int tick, int age, List<Particle> children) {
        super(tick, age);
        this.children = children;
    }

    public static ParticleCompound of(List<Particle> children) {
        if (children == null || children.isEmpty()) {
            throw new IllegalArgumentException("children cannot be null or empty");
        }
        int tick = children.stream().mapToInt(particle -> particle.tick).min().orElse(0);
        int age = children.stream().mapToInt(particle -> particle.tick + particle.age - tick).max().orElse(0);
        return new ParticleCompound(tick, age, children);
    }

    @Override
    protected DataParticle run(Version version) {
        return new DataParticleCompound(children.stream()
                .map(particle -> particle.runWithCache(version))
                .toArray(DataParticle[]::new));
    }
}
