package yancey.openparticle.api.common.controller;

import yancey.openparticle.api.common.OpenParticleAPI;
import yancey.openparticle.api.common.data.ParticleState;
import yancey.openparticle.api.common.data.particle.DataParticleSingle;
import yancey.openparticle.api.common.math.Vec3;
import yancey.openparticle.api.common.node.Node;
import yancey.openparticle.api.common.util.ColorUtil;

public class SimpleParticleController implements ParticleController {

    private final int tickStart;
    private final Node node;
    private final int age;
    private Object particleSprites;

    public SimpleParticleController(Node node) {
        this.node = node;
        this.tickStart = node.getTickStart();
        this.age = node.getAge();
    }

    @Override
    public ParticleState getParticleState(int tick) {
        ParticleState particleState = new ParticleState();
        node.cache(tick);
        Vec3 position = node.cachePosition.apply(Vec3.ZERO);
        particleState.x = position.x;
        particleState.y = position.y;
        particleState.z = position.z;
        particleState.r = (byte) ColorUtil.getRed(node.cacheColor);
        particleState.g = (byte) ColorUtil.getGreen(node.cacheColor);
        particleState.b = (byte) ColorUtil.getBlue(node.cacheColor);
        particleState.a = (byte) ColorUtil.getAlpha(node.cacheColor);
        particleState.bright = 15728880;
        return particleState;
    }

    @Override
    public int getTickStart() {
        return tickStart;
    }

    @Override
    public int getAge() {
        return age;
    }

    @Override
    public Object getParticleSprites(OpenParticleAPI openParticleAPI) {
        if (particleSprites == null) {
            particleSprites = ((DataParticleSingle) node.dataParticle).identifier.getParticleSprites(openParticleAPI);
        }
        return particleSprites;
    }

}
