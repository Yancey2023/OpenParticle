package yancey.openparticle.api.run.node;

import yancey.openparticle.api.run.data.particle.DataParticle;
import yancey.openparticle.api.run.data.particle.DataParticleCompound;
import yancey.openparticle.api.run.data.particle.DataParticleSingle;
import yancey.openparticle.api.run.data.particle.DataParticleTransform;
import yancey.openparticle.api.run.math.Matrix;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Node {

    public int id;
    public final boolean isTransformNode;
    public final DataParticle dataParticle;
    private List<Node> children;
    public int tickStart, tickEnd;

    public Node(DataParticle dataParticle, List<Node> nodeList) {
        id = nodeList.size();
        nodeList.add(this);
        this.dataParticle = dataParticle;
        this.isTransformNode = dataParticle.getType() == DataParticle.TRANSFORM;
        if (isTransformNode) {
            children = getNodeByDataParticle(((DataParticleTransform) dataParticle).child, nodeList);
        }
    }

    public static List<Node> getNodeByDataParticle(DataParticle dataParticle, List<Node> nodeList) {
        return switch (dataParticle.getType()) {
            case DataParticle.SINGLE, DataParticle.TRANSFORM ->
                    Collections.singletonList(new Node(dataParticle, nodeList));
            case DataParticle.COMPOUND ->
                    Arrays.stream(((DataParticleCompound) dataParticle).children).map(child -> getNodeByDataParticle(child, nodeList)).flatMap(List::stream).toList();
            default -> throw new IllegalArgumentException("Unsupported data particle type: " + dataParticle.getType());
        };
    }

    public void setTickStart(int tickStart) {
        if (isTransformNode) {
            this.tickStart = tickStart + ((DataParticleTransform) dataParticle).tickAdd;
            tickEnd = 0;
            for (Node node : children) {
                node.setTickStart(this.tickStart);
                tickEnd = Math.max(tickEnd, node.tickEnd);
            }
        } else {
            this.tickStart = tickStart;
            tickEnd = tickStart + ((DataParticleSingle) dataParticle).age;
        }
    }

    public void buildCache(int tick, NodeCache parentCache, NodeCache[] caches) {
        if (tick < tickStart || tick >= tickEnd) {
            return;
        }
        if (isTransformNode) {
            NodeCache nodeCache = new NodeCache(
                    Matrix.multiply(parentCache.cachePosition, dataParticle.getTransform(tick - tickStart)),
                    parentCache.cacheColor == 0 ? dataParticle.getColor(tick - tickStart) : parentCache.cacheColor
            );
            for (Node child : children) {
                child.buildCache(tick, nodeCache, caches);
            }
        } else {
            caches[id] = new NodeCache(
                    parentCache.cachePosition,
                    parentCache.cacheColor == 0 ? 0xFFFFFFFF : parentCache.cacheColor
            );
        }
    }

}
