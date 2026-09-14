package yancey.openparticle.api.particle;

import yancey.openparticle.api.run.data.particle.DataParticle;
import yancey.openparticle.api.run.data.particle.DataParticleSingle;
import yancey.openparticle.api.type.ParticleType;
import yancey.openparticle.api.util.version.Version;

public class ParticleDefault extends Particle {

    private final ParticleType particleType;

    public ParticleDefault(ParticleType particleType, int age) {
        super(0, age);
        this.particleType = particleType;
    }

    @Override
    protected DataParticle run(Version version) {
        return new DataParticleSingle(particleType.getIdentifier(version), age);
    }

}
