package yancey.openparticle.api.common.node;

import yancey.openparticle.api.common.data.particle.DataParticle;
import yancey.openparticle.api.common.math.Matrix;

import java.util.concurrent.locks.ReentrantLock;

public class Node {

    public final DataParticle dataParticle;
    private final ReentrantLock lock = new ReentrantLock();
    private Node parent;
    private int tickAdd, age;
    private Matrix cachePosition;
    private Integer cacheColor;
    private boolean isCache = false;
    private boolean isCacheColor = false;

    public Node(DataParticle dataParticle) {
        this.dataParticle = dataParticle;
    }

    public void setParent(Node parent) {
        this.parent = parent;
        this.parent.age = Math.max(this.parent.age, age);
    }

    public void addTickAdd(int tickAdd) {
        this.tickAdd += tickAdd;
    }

    private Matrix getPositionMatrix(int tick) {
        return dataParticle.getPositionMatrix(tick + tickAdd, age);
    }

    private Integer getColor(int tick) {
        return dataParticle.getColor(tick + tickAdd, age);
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getTickStart() {
        return parent == null ? tickAdd : tickAdd + parent.getTickStart();
    }

    public Matrix getCachePositionMatrix(int tick) {
        lock.lock();
        try {
            if (cachePosition == null) {
                cachePosition = getPositionMatrix(tick);
                if (parent != null) {
                    cachePosition = Matrix.multiply(parent.getCachePositionMatrix(tick), cachePosition);
                }
                isCache = true;
            }
            return cachePosition;
        } finally {
            lock.unlock();
        }
    }

    public int getCacheColor(int tick) {
        lock.lock();
        try {
            if (!isCacheColor) {
                Integer color = parent.getColor(tick);
                if (color == null) {
                    cacheColor = getColor(tick);
                } else {
                    cacheColor = color;
                }
                isCache = true;
                isCacheColor = true;
            }
            return cacheColor;
        } finally {
            lock.unlock();
        }
    }

    public void clearCache() {
        lock.lock();
        try {
            if (isCache) {
                isCache = false;
                isCacheColor = false;
                cachePosition = null;
                if (parent != null) {
                    parent.clearCache();
                }
            }
        } finally {
            lock.unlock();
        }
    }
}
