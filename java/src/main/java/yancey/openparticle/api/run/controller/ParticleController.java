package yancey.openparticle.api.run.controller;

import yancey.openparticle.api.run.data.ParticleState;
import yancey.openparticle.api.run.math.Vec3;
import yancey.openparticle.api.run.node.Node;
import yancey.openparticle.api.run.node.NodeCache;
import yancey.openparticle.api.run.util.ColorUtil;

import java.util.Objects;

public class ParticleController {

    public final Node node;

    public ParticleController(Node node) {
        this.node = node;
    }

    public ParticleState getParticleState(NodeCache[] caches) {
        NodeCache nodeCache = Objects.requireNonNull(caches[node.id]);
        ParticleState particleState = new ParticleState();
        Vec3 position = nodeCache.cachePosition.apply(Vec3.ZERO);
        particleState.x = position.x;
        particleState.y = position.y;
        particleState.z = position.z;
        particleState.r = (byte) ColorUtil.getRed(nodeCache.cacheColor);
        particleState.g = (byte) ColorUtil.getGreen(nodeCache.cacheColor);
        particleState.b = (byte) ColorUtil.getBlue(nodeCache.cacheColor);
        particleState.a = (byte) ColorUtil.getAlpha(nodeCache.cacheColor);
        particleState.bright = 15728880;
        return particleState;
    }

}
