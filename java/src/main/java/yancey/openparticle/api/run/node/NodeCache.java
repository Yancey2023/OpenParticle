package yancey.openparticle.api.run.node;

import yancey.openparticle.api.run.math.Matrix;

public class NodeCache {

    public final Matrix cachePosition;
    public final int cacheColor;

    public NodeCache(Matrix cachePosition, int cacheColor) {
        this.cachePosition = cachePosition;
        this.cacheColor = cacheColor;
    }
}
