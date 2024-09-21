//
// Created by Yancey on 24-4-11.
//

#include "ParticleRender.h"

namespace OpenParticle::ParticleRender {

    float lerp(float delta, float start, float end) {
        return start + delta * (end - start);
    }

    void vertex(uint8_t *buffer,
                float x, float y, float z,
                float u, float v,
                int32_t color) {
#if OpenParticleDebug == true
        if (buffer == nullptr) {
            throw std::runtime_error("buffer is nullptr");
        }
#endif
        // position
        *reinterpret_cast<float *>(buffer) = x;
        *reinterpret_cast<float *>(buffer + 4) = y;
        *reinterpret_cast<float *>(buffer + 8) = z;
        // texture
        *reinterpret_cast<float *>(buffer + 12) = u;
        *reinterpret_cast<float *>(buffer + 16) = v;
        // color
        *reinterpret_cast<int32_t *>(buffer + 20) = color;
        // light
        *reinterpret_cast<int32_t *>(buffer + 24) = 0x00F000F0;
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
            lastTickCache = currentTickCache;
        }
        // prepare render cache
        Eigen::Matrix3f matrix = Eigen::Quaternionf(rw, rx, ry, rz).matrix();
        Eigen::Vector3f cache1 = matrix * Eigen::Vector3f(1, -1, 0);
        Eigen::Vector3f cache2 = matrix * Eigen::Vector3f(1, 1, 0);
        // TODO: every particle should have itself size, rather than 0.1125
        Eigen::Vector3f cache3 = cache1 * 0.1125;
        Eigen::Vector3f cache4 = cache2 * 0.1125;
        size_t size = currentTickCache->size();

        // render
        size_t index = 0;
        float dx, dy, dz;
        for (size_t i = 0; i < size; ++i) {
            const std::optional<SingleParticleNodeCache> &item = (*currentTickCache)[i];
            if (HEDLEY_UNLIKELY(item.has_value())) {
                const SingleParticleNodeCache &currentTickData = item.value();
                const std::optional<SingleParticleNodeCache> &lastTickItem = (*lastTickCache)[i];
                if (HEDLEY_LIKELY(lastTickItem.has_value())) {
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
                       cache3.x() + dx, cache3.y() + dy, cache3.z() + dz,
                       currentTickData.sprite->maxU, currentTickData.sprite->maxV,
                       currentTickData.color);
                vertex(buffer0 + 28,
                       cache4.x() + dx, cache4.y() + dy, cache4.z() + dz,
                       currentTickData.sprite->maxU, currentTickData.sprite->minV,
                       currentTickData.color);
                vertex(buffer0 + 56,
                       -cache3.x() + dx, -cache3.y() + dy, -cache3.z() + dz,
                       currentTickData.sprite->minU, currentTickData.sprite->minV,
                       currentTickData.color);
                vertex(buffer0 + 84,
                       -cache4.x() + dx, -cache4.y() + dy, -cache4.z() + dz,
                       currentTickData.sprite->minU, currentTickData.sprite->maxV,
                       currentTickData.color);
            }
        }
    }

    void doRenderMultiThread(uint8_t *buffer, size_t particleCount, float tickDelta,
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
            lastTickCache = currentTickCache;
        }
        // prepare render cache
        Eigen::Matrix3f matrix = Eigen::Quaternionf(rw, rx, ry, rz).matrix();
        Eigen::Vector3f cache1 = matrix * Eigen::Vector3f(1, -1, 0);
        Eigen::Vector3f cache2 = matrix * Eigen::Vector3f(1, 1, 0);
        // TODO: every particle should have itself size, rather than 0.1125
        Eigen::Vector3f cache3 = cache1 * 0.1125;
        Eigen::Vector3f cache4 = cache2 * 0.1125;
        size_t size = currentTickCache->size();

        //assign tasks
        const unsigned int nThread = std::thread::hardware_concurrency();
        Task *tasks = new Task[nThread];
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
        auto *futures = new std::optional<std::future<void>>[nThread];
        for (int i = 0; i < nThread; ++i) {
            const auto &task = tasks[i];
            if (task.buffer == nullptr) {
                futures[i] = std::nullopt;
                break;
            }
            futures[i] = std::async([&task, &currentTickCache, &lastTickCache, tickDelta,
                                     cameraX, cameraY, cameraZ,
                                     &cache3, &cache4]() {
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
                               cache3.x() + dx, cache3.y() + dy, cache3.z() + dz,
                               currentTickData.sprite->maxU, currentTickData.sprite->maxV,
                               currentTickData.color);
                        vertex(buffer0 + 28,
                               cache4.x() + dx, cache4.y() + dy, cache4.z() + dz,
                               currentTickData.sprite->maxU, currentTickData.sprite->minV,
                               currentTickData.color);
                        vertex(buffer0 + 56,
                               -cache3.x() + dx, -cache3.y() + dy, -cache3.z() + dz,
                               currentTickData.sprite->minU, currentTickData.sprite->minV,
                               currentTickData.color);
                        vertex(buffer0 + 84,
                               -cache4.x() + dx, -cache4.y() + dy, -cache4.z() + dz,
                               currentTickData.sprite->minU, currentTickData.sprite->maxV,
                               currentTickData.color);
                    }
                }
            });
        }
        for (int i = 0; i < nThread; ++i) {
            std::optional<std::future<void>> &future = futures[i];
            if (future.has_value()) {
                future->get();
            }
        }
        delete[] tasks;
        delete[] futures;
    }

}// namespace OpenParticle::ParticleRender