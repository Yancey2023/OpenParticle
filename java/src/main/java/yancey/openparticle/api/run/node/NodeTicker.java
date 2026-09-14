package yancey.openparticle.api.run.node;

import yancey.openparticle.api.run.data.DataRunningPerTick;
import yancey.openparticle.api.run.math.Matrix;

public class NodeTicker {
    public final Node root;
    public final DataRunningPerTick[] dataRunningPerTicks;
    public final int singleNodeSize;

    public NodeTicker(Node root, DataRunningPerTick[] dataRunningPerTicks, int singleNodeSize) {
        this.root = root;
        this.dataRunningPerTicks = dataRunningPerTicks;
        this.singleNodeSize = singleNodeSize;
    }

    public NodeCache[] buildCache(int tick) {
        NodeCache[] result = new NodeCache[singleNodeSize];
        root.buildCache(tick, new NodeCache(Matrix.ZERO, 0), result);
        return result;
    }
}
