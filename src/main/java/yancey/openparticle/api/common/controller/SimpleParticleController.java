package yancey.openparticle.api.common.controller;

import yancey.openparticle.api.common.OpenParticleAPI;
import yancey.openparticle.api.common.data.ParticleState;
import yancey.openparticle.api.common.math.Vec3;
import yancey.openparticle.api.common.node.Node;
import yancey.openparticle.api.common.util.ColorUtil;

public class SimpleParticleController implements ParticleController {

    private final int tickStart;
    private final Node node;
    private final int age;
    private Object particleSprites;
    private final boolean isStatic;
    private int lastTick = -1;
    private ParticleState nextParticleState;

    public SimpleParticleController(Node node) {
        this.node = node;
        this.tickStart = node.getTickStart();
        this.age = node.getAge();
        isStatic = node.isStatic();
        if (isStatic) {
            updateParticleState();
        }
    }

    @Override
    public boolean isStatic() {
        return isStatic;
    }

    @Override
    public void prepare(int tick) {
        if (isStatic) {
            return;
        }
        if (lastTick != tick) {
            if (tick >= age) {
                return;
            }
            node.cache(tick);
            updateParticleState();
            lastTick = tick;
        }
    }

    private void updateParticleState() {
        nextParticleState = new ParticleState();
        Vec3 position = node.cachePosition.apply(Vec3.ZERO);
        nextParticleState.x = position.x;
        nextParticleState.y = position.y;
        nextParticleState.z = position.z;
        nextParticleState.r = (byte) ColorUtil.getRed(node.cacheColor);
        nextParticleState.g = (byte) ColorUtil.getGreen(node.cacheColor);
        nextParticleState.b = (byte) ColorUtil.getBlue(node.cacheColor);
        nextParticleState.a = (byte) ColorUtil.getAlpha(node.cacheColor);
        nextParticleState.bright = 15728880;
    }

    @Override
    public ParticleState getParticleState() {
        return nextParticleState;
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
            particleSprites = node.getParticleSprites(openParticleAPI);
        }
        return particleSprites;
    }

}
