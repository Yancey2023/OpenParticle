package yancey.openparticle.api.common.node;

import yancey.openparticle.api.common.data.particle.DataParticle;
import yancey.openparticle.api.common.math.Matrix;

import java.util.concurrent.locks.ReentrantLock;

public class Node {

    public final DataParticle dataParticle;
    private final ReentrantLock lock = new ReentrantLock();
    private Node parent;
    public Matrix cachePosition;
    public Integer cacheColor;
    private int tickStart = -1, tickAdd, age;
    private int lastCacheTick = -1;

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

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getTickStart() {
        if (tickStart == -1) {
            tickStart = parent == null ? tickAdd : tickAdd + parent.getTickStart();
        }
        return tickStart;
    }

    public void cache(int tick) {
        lock.lock();
        try {
            if (tick != lastCacheTick) {
                cachePosition = dataParticle.getPositionMatrix(tick - tickAdd, age);
                cacheColor = null;
                if (parent != null) {
                    parent.cache(tick - tickAdd);
                    cacheColor = parent.cacheColor;
                    cachePosition = Matrix.multiply(parent.cachePosition, cachePosition);
                }
                if (cacheColor == null) {
                    cacheColor = dataParticle.getColor(tick - tickAdd, age);
                }
                lastCacheTick = tick;
            }
        } finally {
            lock.unlock();
        }
    }
}
