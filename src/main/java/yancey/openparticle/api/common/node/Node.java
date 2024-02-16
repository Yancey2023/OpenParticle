package yancey.openparticle.api.common.node;

import yancey.openparticle.api.common.OpenParticleAPI;
import yancey.openparticle.api.common.data.particle.DataParticle;
import yancey.openparticle.api.common.data.particle.DataParticleSingle;
import yancey.openparticle.api.common.math.Matrix;

import java.util.concurrent.locks.ReentrantLock;

public class Node {

    public final Integer currentStaticColor;
    //当前节点数据
    private final DataParticle dataParticle;
    //保证多线程安全
    private final ReentrantLock lock = new ReentrantLock();
    //当前节点永久不变的位置和颜色偏移（不一定存在）
    private final Matrix currentStaticPosition;
    //累计位置和颜色偏移（每tick更新一次）
    public Matrix cachePosition;
    public int cacheColor;
    //父节点
    private Node parent;
    //累计时间偏移，当前节点存储时间偏移，年龄
    private int tickStart = -1, tickAdd, age;
    //获取上一次更新是哪个tick
    private int lastCacheTick = -1;
    //累计位置和颜色偏移是否永久不变
    private boolean isPositionStatic = false;
    private boolean isColorStatic = false;

    //第一步：新建节点
    public Node(DataParticle dataParticle) {
        this.dataParticle = dataParticle;
        this.currentStaticPosition = dataParticle.getCurrentStaticPosition();
        this.currentStaticColor = dataParticle.getCurrentStaticColor();
    }

    //第二步：绑定父节点
    public void setParent(Node parent) {
        this.parent = parent;
        if (parent == null) {
            if (currentStaticPosition != null) {
                isPositionStatic = true;
                cachePosition = currentStaticPosition;
            }
            if (currentStaticColor != null) {
                isColorStatic = true;
                cachePosition = currentStaticPosition;
            }
        } else {
            if (parent.isPositionStatic && currentStaticPosition != null) {
                isPositionStatic = true;
                cachePosition = Matrix.multiply(parent.cachePosition, currentStaticPosition);
            }
            cacheColor = 0;
            if (parent.isColorStatic) {
                cacheColor = parent.cacheColor;
            }
            if (cacheColor != 0) {
                isColorStatic = true;
            } else if (currentStaticColor != null) {
                isColorStatic = true;
                cacheColor = currentStaticColor;
            }
        }
    }

    //第三步：设置年龄
    public void setAge(int age) {
        this.age = age;
        if (parent != null) {
            parent.age = Math.max(parent.age, age);
        }
    }

    //第四步：设置当前节点时间偏移
    public void addTickAdd(int tickAdd) {
        this.tickAdd += tickAdd;
    }


    public int getAge() {
        return age;
    }

    public Object getParticleSprites(OpenParticleAPI openParticleAPI) {
        return ((DataParticleSingle) dataParticle).identifier.getParticleSprites(openParticleAPI);
    }

    public int getTickStart() {
        lock.lock();
        try {
            if (tickStart == -1) {
                tickStart = parent == null ? tickAdd : tickAdd + parent.getTickStart();
            }
        } finally {
            lock.unlock();
        }
        return tickStart;
    }

    public void cache(int tick) {
        if (isStatic()) {
            return;
        }
        lock.lock();
        try {
            if (tick != lastCacheTick) {
                lastCacheTick = tick;
                //更新父节点
                if (parent != null) {
                    parent.cache(tick + tickAdd);
                }
                //更新位置
                if (!isPositionStatic) {
                    cachePosition = currentStaticPosition == null ? dataParticle.getPositionMatrix(tick + tickAdd, age) : currentStaticPosition;
                    if (parent != null) {
                        cachePosition = Matrix.multiply(parent.cachePosition, cachePosition);
                    }
                }
                //更新颜色
                if (!isColorStatic) {
                    cacheColor = 0;
                    if (parent != null) {
                        cacheColor = parent.cacheColor;
                    }
                    if (cacheColor == 0) {
                        cacheColor = currentStaticColor == null ? dataParticle.getColor(tick + tickAdd, age) : currentStaticColor;
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }

    public boolean isStatic() {
        return isPositionStatic && isColorStatic;
    }
}
