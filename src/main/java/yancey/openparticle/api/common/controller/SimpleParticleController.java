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
    private int particleTypeRawId = -1;

    public SimpleParticleController(Node node) {
        this.node = node;
        this.tickStart = node.getTickStart();
        this.age = node.getAge();
    }

    @Override
    public ParticleState getParticleState(int tick) {
        ParticleState particleState = new ParticleState();
        Vec3 position = node.getCachePositionMatrix(tick).apply(Vec3.ZERO);
        particleState.x = position.x;
        particleState.y = position.y;
        particleState.z = position.z;
        int color = node.getCacheColor(tick);
        particleState.r = (byte) ColorUtil.getRed(color);
        particleState.g = (byte) ColorUtil.getGreen(color);
        particleState.b = (byte) ColorUtil.getBlue(color);
        particleState.a = (byte) ColorUtil.getAlpha(color);
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
    public int getParticleTypeRawId(OpenParticleAPI openParticleAPI) {
        if (particleTypeRawId == -1) {
            particleTypeRawId = ((DataParticleSingle) node.dataParticle).identifier.getRawId(openParticleAPI);
        }
        return particleTypeRawId;
    }

    @Override
    public void clearCache() {
        node.clearCache();
    }
}
