package yancey.openparticle.api.common.nativecore;

import java.io.Closeable;
import java.nio.Buffer;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class OpenParticleProject implements Closeable {

    public final String path;
    public final int tickEnd;
    private final ReentrantLock lock = new ReentrantLock();
    /**
     * pointer of the data in native
     */
    private long particleDataPointer;
    private int currentParticleCount = -1;

    public OpenParticleProject(Bridge bridge, String path) {
        lock.lock();
        try {
            this.path = path;
            this.particleDataPointer = readFile(Objects.requireNonNull(path), Objects.requireNonNull(bridge));
            if (particleDataPointer == 0) {
                throw new RuntimeException("Failed to read particle file in native: " + path);
            }
            tickEnd = getTickEnd(particleDataPointer);
        } finally {
            lock.unlock();
        }
    }

    public void tick(int tick) {
        if (tick < 0) {
            throw new RuntimeException("Tick should be a positive number: " + tick);
        }
        lock.lock();
        try {
            if (particleDataPointer == 0) {
                throw new RuntimeException("open particle project is closed");
            }
            currentParticleCount = -1;
            prepareTickCache(particleDataPointer, tick);
        } finally {
            lock.unlock();
        }
    }

    public int getParticleCount() {
        lock.lock();
        try {
            if (particleDataPointer == 0) {
                return 0;
            }
            if (currentParticleCount < 0) {
                currentParticleCount = getParticleCount(particleDataPointer);
            }
            return currentParticleCount;
        } finally {
            lock.unlock();
        }
    }

    // or: getParticleSize() * 112
    public int getVBOSize() {
        lock.lock();
        try {
            if (particleDataPointer == 0) {
                throw new RuntimeException("open particle project is closed");
            }
            return getVBOSize(particleDataPointer);
        } finally {
            lock.unlock();
        }
    }

    /**
     * @param pointer        a pointer of particle data
     * @param bufferPointer  a pointer of buffer
     * @param isSingleThread is prepared VBO data in single thread
     * @param tickDelta      tick delta
     * @param cameraX        camera x-axis position
     * @param cameraY        camera y-axis position
     * @param cameraZ        camera z-axis position
     * @param rx             camera rotation x
     * @param ry             camera rotation y
     * @param rz             camera rotation z
     * @param rw             camera rotation w
     */
    private native static void render(long pointer, long bufferPointer, boolean isSingleThread, float tickDelta, float cameraX, float cameraY, float cameraZ, float rx, float ry, float rz, float rw);

    public void render(Buffer directBuffer, boolean isSingleThread, float tickDelta, float cameraX, float cameraY, float cameraZ, float rx, float ry, float rz, float rw) {
        if (!directBuffer.isDirect()) {
            throw new RuntimeException("buffer is not direct");
        }
        if (directBuffer.capacity() < getVBOSize()) {
            throw new RuntimeException("buffer is too small");
        }
        if (tickDelta < 0 || tickDelta > 1) {
            throw new RuntimeException("Tick delta out of range: " + tickDelta);
        }
        lock.lock();
        try {
            if (particleDataPointer == 0) {
                throw new RuntimeException("open particle project is closed");
            }
            render(particleDataPointer, directBuffer, isSingleThread, tickDelta, cameraX, cameraY, cameraZ, rx, ry, rz, rw);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        lock.lock();
        try {
            if (particleDataPointer == 0) {
                return;
            }
            release(particleDataPointer);
            particleDataPointer = 0;
        } finally {
            lock.unlock();
        }
    }

    /**
     * read particle file
     *
     * @param path file path
     * @return a pointer of particle data
     */
    private native static long readFile(String path, Bridge bridge);

    /**
     * release particle data
     *
     * @param pointer a pointer of particle data
     */
    private native static void release(long pointer);

    /**
     * @param pointer a pointer of particle data
     * @return tick end
     */
    private native static int getTickEnd(long pointer);

    /**
     * @param pointer a pointer of particle data
     * @return particle count in current tick
     */
    private native static int getParticleCount(long pointer);

    /**
     * @param pointer a pointer of particle data
     * @param tick    current tick
     */
    private native static void prepareTickCache(long pointer, int tick);

    /**
     * @param pointer a pointer of particle data
     */
    private native static int getVBOSize(long pointer);

    /**
     * @param pointer        a pointer of particle data
     * @param directBuffer   direct buffer
     * @param isSingleThread is prepared VBO data in single thread
     * @param tickDelta      tick delta
     * @param cameraX        camera x-axis position
     * @param cameraY        camera y-axis position
     * @param cameraZ        camera z-axis position
     * @param rx             camera rotation x
     * @param ry             camera rotation y
     * @param rz             camera rotation z
     * @param rw             camera rotation w
     */
    private native static void render(long pointer, Buffer directBuffer, boolean isSingleThread, float tickDelta, float cameraX, float cameraY, float cameraZ, float rx, float ry, float rz, float rw);

    public void render(long bufferPointer, boolean isSingleThread, float tickDelta, float cameraX, float cameraY, float cameraZ, float rx, float ry, float rz, float rw) {
        if (tickDelta < 0 || tickDelta > 1) {
            throw new RuntimeException("Tick delta out of range: " + tickDelta);
        }
        lock.lock();
        try {
            if (particleDataPointer == 0) {
                throw new RuntimeException("open particle project is closed");
            }
            render(particleDataPointer, bufferPointer, isSingleThread, tickDelta, cameraX, cameraY, cameraZ, rx, ry, rz, rw);
        } finally {
            lock.unlock();
        }
    }

    public interface Bridge {

        /**
         * a method to get particle sprites data, it will be call in native
         *
         * @param namespace identify namespace
         * @param value     identify value
         * @return sprites, one vertex position need four float
         */
        @SuppressWarnings("unused")
        float[] getParticleSpritesData(String namespace, String value);

    }

}
