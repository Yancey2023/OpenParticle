//
// Created by Yancey on 24-4-11.
//

#include "ParticleRender.h"

namespace OpenParticle::ParticleRender {

    inline float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    inline void vertex(uint8_t *buffer,
                       float x, float y, float z,
                       float u, float v,
                       int32_t color) {
#if OpenParticleDebug == true
        if (buffer == nullptr) {
            throw std::runtime_error("buffer is nullptr");
        }
#endif
        // position
        *(float *) (buffer) = x;
        *(float *) (buffer + 4) = y;
        *(float *) (buffer + 8) = z;
        // texture
        *(float *) (buffer + 12) = u;
        *(float *) (buffer + 16) = v;
        // color
        *(int32_t *) (buffer + 20) = (int32_t) color;
        // light
        *(int32_t *) (buffer + 24) = (int32_t) 0x00F000F0;
    }

    struct Task {
        size_t start, end;
        uint8_t *buffer;
    };

    void doRenderSingleThread(uint8_t *buffer, size_t particleCount, float tickDelta,
                              float cameraX, float cameraY, float cameraZ,
                              float rx, float ry, float rz, float rw,
                              const std::vector<std::optional<SingleParticleNodeCache>> *lastTickCache,
                              const std::vector<std::optional<SingleParticleNodeCache>> *currentTickCache) {
#if OpenParticleDebug == true
        if (buffer == nullptr) {
            throw std::runtime_error("buffer is nullptr");
        }
        if (tickDelta < 0 || tickDelta > 1) {
            throw std::runtime_error("tickDelta is out of range [0, 1]");
        }
#endif
        if (particleCount == 0) {
            return;
        }
        if (HEDLEY_UNLIKELY(currentTickCache == nullptr)) {
            // it appears when current tick less than 0 or bigger than tickEnd
            return;
        }
        if (HEDLEY_UNLIKELY(lastTickCache == nullptr)) {
            // it appears when current tick equal 0
            return doRenderSingleThread(buffer, particleCount, tickDelta,
                                        cameraX, cameraY, cameraZ,
                                        rx, ry, rz, rw,
                                        currentTickCache,
                                        currentTickCache);
        }
        // prepare render cache
        float xx = rx * rx, yy = ry * ry, zz = rz * rz, ww = rw * rw;
        float xy = rx * ry, xz = rx * rz, yz = ry * rz, xw = rx * rw;
        float zw = rz * rw, yw = ry * rw, k = 1 / (xx + yy + zz + ww);
        float a1 = (xx - yy - zz + ww) * k * -0.1F;
        float b1 = 2 * (xy + zw) * k * -0.1F;
        float c1 = 2 * (xz - yw) * k * -0.1F;
        float a2 = 2 * (xy - zw) * k * 0.1F;
        float b2 = (yy - xx - zz + ww) * k * 0.1F;
        float c2 = 2 * (yz + xw) * k * 0.1F;
        float cacheX1 = a1 - a2;
        float cacheY1 = b1 - b2;
        float cacheZ1 = c1 - c2;
        float cacheX2 = a1 + a2;
        float cacheY2 = b1 + b2;
        float cacheZ2 = c1 + c2;
        size_t size = currentTickCache->size();

        // render
        size_t index = 0;
        float dx, dy, dz;
        for (size_t i = 0; i < size; ++i) {
            const std::optional<SingleParticleNodeCache> &item = (*currentTickCache)[i];
            if (HEDLEY_UNLIKELY(item.has_value())) {
                const SingleParticleNodeCache &currentTickData = item.value();
                const std::optional<SingleParticleNodeCache> &lastTickItem = (*lastTickCache)[i];
                if (lastTickItem.has_value()) {
                    const SingleParticleNodeCache &lastTickData = lastTickItem.value();
                    dx = lerp(tickDelta, lastTickData.x, currentTickData.x) - cameraX;
                    dy = lerp(tickDelta, lastTickData.y, currentTickData.y) - cameraY;
                    dz = lerp(tickDelta, lastTickData.z, currentTickData.z) - cameraZ;
                } else {
                    dx = currentTickData.x - cameraX;
                    dy = currentTickData.y - cameraY;
                    dz = currentTickData.z - cameraZ;
                }
                auto *buffer0 = buffer + 112 * index++;
                vertex(buffer0,
                       cacheX1 + dx, cacheY1 + dy, cacheZ1 + dz,
                       currentTickData.sprite->maxU, currentTickData.sprite->maxV,
                       currentTickData.color);
                vertex(buffer0 + 28,
                       cacheX2 + dx, cacheY2 + dy, cacheZ2 + dz,
                       currentTickData.sprite->maxU, currentTickData.sprite->minV,
                       currentTickData.color);
                vertex(buffer0 + 56,
                       -cacheX1 + dx, -cacheY1 + dy, -cacheZ1 + dz,
                       currentTickData.sprite->minU, currentTickData.sprite->minV,
                       currentTickData.color);
                vertex(buffer0 + 84,
                       -cacheX2 + dx, -cacheY2 + dy, -cacheZ2 + dz,
                       currentTickData.sprite->minU, currentTickData.sprite->maxV,
                       currentTickData.color);
            }
        }
    }

    void doRender(uint8_t *buffer, size_t particleCount, float tickDelta,
                  float cameraX, float cameraY, float cameraZ,
                  float rx, float ry, float rz, float rw,
                  const std::vector<std::optional<SingleParticleNodeCache>> *lastTickCache,
                  const std::vector<std::optional<SingleParticleNodeCache>> *currentTickCache) {
#if OpenParticleDebug == true
        if (buffer == nullptr) {
            throw std::runtime_error("buffer is nullptr");
        }
        if (tickDelta < 0 || tickDelta > 1) {
            throw std::runtime_error("tickDelta is out of range [0, 1]");
        }
#endif
        if (particleCount == 0) {
            return;
        }
        if (HEDLEY_UNLIKELY(currentTickCache == nullptr)) {
            // it appears when current tick less than 0 or bigger than tickEnd
            return;
        }
        if (HEDLEY_UNLIKELY(lastTickCache == nullptr)) {
            // it appears when current tick equal 0
            return doRender(buffer, particleCount, tickDelta,
                            cameraX, cameraY, cameraZ,
                            rx, ry, rz, rw,
                            currentTickCache,
                            currentTickCache);
        }
        // prepare render cache
        float xx = rx * rx, yy = ry * ry, zz = rz * rz, ww = rw * rw;
        float xy = rx * ry, xz = rx * rz, yz = ry * rz, xw = rx * rw;
        float zw = rz * rw, yw = ry * rw, k = 1 / (xx + yy + zz + ww);
        float a1 = (xx - yy - zz + ww) * k * -0.1F;
        float b1 = 2 * (xy + zw) * k * -0.1F;
        float c1 = 2 * (xz - yw) * k * -0.1F;
        float a2 = 2 * (xy - zw) * k * 0.1F;
        float b2 = (yy - xx - zz + ww) * k * 0.1F;
        float c2 = 2 * (yz + xw) * k * 0.1F;
        float cacheX1 = a1 - a2;
        float cacheY1 = b1 - b2;
        float cacheZ1 = c1 - c2;
        float cacheX2 = a1 + a2;
        float cacheY2 = b1 + b2;
        float cacheZ2 = c1 + c2;
        size_t size = currentTickCache->size();

        //assign tasks
        const unsigned int nThread = std::thread::hardware_concurrency();
        Task tasks[nThread];
        size_t numsInEveryTask = particleCount / nThread;
        size_t currentTaskNums = numsInEveryTask;
        int32_t tasksIndex = -1;
        for (size_t i = 0; i < size; ++i) {
            const std::optional<SingleParticleNodeCache> &item = (*currentTickCache)[i];
            if (HEDLEY_UNLIKELY(item.has_value())) {
                if (HEDLEY_UNLIKELY(currentTaskNums == numsInEveryTask)) {
                    if (tasksIndex >= 0) {
                        tasks[tasksIndex].end = i;
                    }
                    tasksIndex++;
                    if (tasksIndex == nThread) {
                        tasks[tasksIndex - 1].end = size;
                        break;
                    }
                    tasks[tasksIndex].start = i;
                    tasks[tasksIndex].buffer = buffer + 112 * tasksIndex * numsInEveryTask;
                    currentTaskNums = 0;
                }
                currentTaskNums++;
            }
        }
        if (tasksIndex < nThread) {
            tasks[tasksIndex].end = size;
            for (int i = tasksIndex + 1; i < nThread; ++i) {
                tasks[i].buffer = nullptr;
            }
        }

        //run render tasks
        std::optional<std::future<void>> futures[nThread];
        for (int i = 0; i < nThread; ++i) {
            const auto &task = tasks[i];
            if (task.buffer == nullptr) {
                futures[i] = std::nullopt;
                break;
            }
            futures[i] = std::async([&task, &currentTickCache, &lastTickCache, tickDelta,
                                     cameraX, cameraY, cameraZ,
                                     cacheX1, cacheY1, cacheZ1,
                                     cacheX2, cacheY2, cacheZ2]() {
                size_t index = 0;
                float dx, dy, dz;
                for (size_t i = task.start; i < task.end; ++i) {
                    const std::optional<SingleParticleNodeCache> &item = (*currentTickCache)[i];
                    if (HEDLEY_UNLIKELY(item.has_value())) {
                        const SingleParticleNodeCache &currentTickData = item.value();
                        const std::optional<SingleParticleNodeCache> &lastTickItem = (*lastTickCache)[i];
                        if (lastTickItem.has_value()) {
                            const SingleParticleNodeCache &lastTickData = lastTickItem.value();
                            dx = lerp(tickDelta, lastTickData.x, currentTickData.x) - cameraX;
                            dy = lerp(tickDelta, lastTickData.y, currentTickData.y) - cameraY;
                            dz = lerp(tickDelta, lastTickData.z, currentTickData.z) - cameraZ;
                        } else {
                            dx = currentTickData.x - cameraX;
                            dy = currentTickData.y - cameraY;
                            dz = currentTickData.z - cameraZ;
                        }
                        auto *buffer0 = task.buffer + 112 * index++;
                        vertex(buffer0,
                               cacheX1 + dx, cacheY1 + dy, cacheZ1 + dz,
                               currentTickData.sprite->maxU, currentTickData.sprite->maxV,
                               currentTickData.color);
                        vertex(buffer0 + 28,
                               cacheX2 + dx, cacheY2 + dy, cacheZ2 + dz,
                               currentTickData.sprite->maxU, currentTickData.sprite->minV,
                               currentTickData.color);
                        vertex(buffer0 + 56,
                               -cacheX1 + dx, -cacheY1 + dy, -cacheZ1 + dz,
                               currentTickData.sprite->minU, currentTickData.sprite->minV,
                               currentTickData.color);
                        vertex(buffer0 + 84,
                               -cacheX2 + dx, -cacheY2 + dy, -cacheZ2 + dz,
                               currentTickData.sprite->minU, currentTickData.sprite->maxV,
                               currentTickData.color);
                    }
                }
            });
        }
        for (auto &future: futures) {
            if (future.has_value()) {
                future->get();
            }
        }
    }

}// namespace OpenParticle::ParticleRender