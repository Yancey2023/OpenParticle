//
// Created by Yancey on 24-4-12.
//

#ifndef OPENPARTICLE_PARTICLEMANAGER_H
#define OPENPARTICLE_PARTICLEMANAGER_H

#include "ParticleRender.h"

namespace OpenParticle {

    template<uint16_t cacheSize>
    class ParticleManager {
    private:
        ParticleData *particleData;
        ParticleTicker *particleTicker;
        // particle count in every tick
        std::vector<int32_t> particleCounts;
        // storage from 0 to (cacheSize - 1)
        std::array<std::optional<std::shared_future<std::vector<std::optional<SingleParticleNodeCache>>>>, cacheSize> preCache;
        int32_t currentTick = -1;
        // which index is (currentCache - 1) in current cache
        int64_t index = cacheSize - 2;
        // storage from (currentTick - 1) to (currentTick - 1 + cacheSize)
        std::array<std::optional<std::shared_future<std::vector<std::optional<SingleParticleNodeCache>>>>, cacheSize> currentCache;

    public:
        explicit ParticleManager(const char *path, const std::function<void(Identifier &identifier)> &setSprite) {
            std::ifstream istream(path, std::ios::in | std::ios::binary);
            if (!istream.is_open()) {
                throw std::runtime_error("fail to open file");
            }
            union {
                int32_t int32 = 0x01020304;
                int8_t int8;
            } data;
            if (HEDLEY_LIKELY(data.int8 == 0x04)) {
                DataReader<true> dataReader(istream);
                particleData = new ParticleData(dataReader, setSprite);
            } else if (data.int8 == 0x01) {
                DataReader<false> dataReader(istream);
                particleData = new ParticleData(dataReader, setSprite);
            } else {
                throw std::runtime_error("unknown byte order");
            }
            istream.close();
            particleTicker = new ParticleTicker(particleData);
            int32_t tickEnd = getTickEnd();
            particleCounts = std::vector<int>(tickEnd);
            for (int i = 0; i < particleTicker->particleSingleNodeListSize; ++i) {
                Node *node = particleTicker->particleSingleNodeList[i];
#if OpenParticleDebug == true
                if (node->tickStart < 0 || node->tickStart >= particleCounts.size()) {
                    throw std::runtime_error("tickStart out of range [0, tickEnd)");
                }
                if (node->tickEnd < 0 || node->tickEnd > particleCounts.size()) {
                    throw std::runtime_error("tickEnd out of range [0, tickEnd]");
                }
#endif
                for (int j = node->tickStart; j < node->tickEnd; ++j) {
                    particleCounts[j]++;
                }
            }
            for (int32_t tick = 0; tick < cacheSize; ++tick) {
                if (particleTicker->root->tickStart <= tick &&
                    particleTicker->root->tickEnd > tick) {
                    preCache[tick] = std::async([this, tick]() {
                        return particleTicker->buildCache(tick);
                    });
                } else {
                    preCache[tick] = std::nullopt;
                }
            }
        }

        ~ParticleManager() {
            delete particleData;
            delete particleTicker;
        }

        [[nodiscard]] int32_t getTickEnd() const {
            return particleTicker->root->tickEnd;
        }

        void tick(int32_t tick) {
            int32_t tickOffset = tick - currentTick;
            if (tickOffset == 0) {
                return;
            }
            uint16_t start;
            uint16_t end;
            if (tickOffset <= -cacheSize || tickOffset >= cacheSize) {
                index = 0;
                start = 0;
                end = cacheSize;
            } else {
                index += tickOffset;
                if (tickOffset < 0) {
                    if (index < 0) {
                        index += cacheSize;
                    }
                    start = 0;
                    end = -tickOffset;
                } else {
                    if (index >= cacheSize) {
                        index -= cacheSize;
                    }
                    end = cacheSize;
                    start = end - tickOffset;
                }
            }
#if OpenParticleDebug == true
            if (index < 0 || index >= cacheSize) {
                throw std::runtime_error("index is out of range [0, cacheSize)");
            }
            if (start >= end) {
                throw std::runtime_error("start should less than end");
            }
            if (end >= cacheSize && end - cacheSize >= cacheSize) {
                throw std::runtime_error("end is too large");
            }
#endif
            for (uint16_t i = start; i < end; ++i) {
                int32_t tickNeedToCache = tick - 1 + i;
                int32_t indexOfTheTick = i + index;
                if (indexOfTheTick >= cacheSize) {
                    indexOfTheTick -= cacheSize;
                }
#if OpenParticleDebug == true
                if (indexOfTheTick >= cacheSize) {
                    throw std::runtime_error("indexOfTheTick is out of range [0, cacheSize)");
                }
#endif
                // TODO cancel old tasks before running new tasks
                if (particleTicker->root->tickStart <= tickNeedToCache &&
                    particleTicker->root->tickEnd > tickNeedToCache &&
                    tickNeedToCache >= cacheSize) {
                    currentCache[indexOfTheTick] = std::async([this, tickNeedToCache]() {
                        return particleTicker->buildCache(tickNeedToCache);
                    });
                } else {
                    currentCache[indexOfTheTick] = std::nullopt;
                }
            }
            currentTick = tick;
        }

        const std::vector<std::optional<SingleParticleNodeCache>> *getTickCache(int32_t tick) {
            if (tick >= 0 && tick < cacheSize) {
                const auto &result = preCache[tick];
                return result.has_value() ? &result.value().get() : nullptr;
            }
#if OpenParticleDebug == true
            if (index < 0 || index >= cacheSize) {
                throw std::runtime_error("index out of range [0, cacheSize)");
            }
#endif
            int32_t dt = tick - currentTick;
            if (dt < -1 || dt >= cacheSize - 1) {
                return nullptr;
            }
            int32_t indexOfTheTick = index + dt + 1;
            if (indexOfTheTick >= cacheSize) {
                indexOfTheTick -= cacheSize;
            }
#if OpenParticleDebug == true
            if (indexOfTheTick < 0 || indexOfTheTick >= cacheSize) {
                throw std::runtime_error("index of the tick out of range [0, cacheSize)");
            }
#endif
            const auto &cache = currentCache[indexOfTheTick];
            return cache.has_value() ? &cache.value().get() : nullptr;
        }

        [[nodiscard]] int32_t getParticleCount() const {
            if (currentTick < 0 || currentTick >= getTickEnd()) {
                return 0;
            }
            return particleCounts[currentTick];
        }

        [[nodiscard]] int32_t getVBOSize() const {
            return 112 * getParticleCount();
        }

        void doRender(bool isSingleThread, uint8_t *buffer, float tickDelta,
                      float cameraX, float cameraY, float cameraZ,
                      float rx, float ry, float rz, float rw) {
#if OpenParticleDebug == true
            if (buffer == nullptr) {
                throw std::runtime_error("buffer is nullptr");
            }
            if (tickDelta < 0 || tickDelta > 1) {
                throw std::runtime_error("tickDelta is out of range [0, 1]");
            }
#endif
            if (isSingleThread) {
                ParticleRender::doRenderSingleThread(buffer, getParticleCount(), tickDelta,
                                                     cameraX, cameraY, cameraZ,
                                                     rx, ry, rz, rw,
                                                     getTickCache(currentTick - 1),
                                                     getTickCache(currentTick));
            } else {
                ParticleRender::doRenderMultiThread(buffer, getParticleCount(), tickDelta,
                                                    cameraX, cameraY, cameraZ,
                                                    rx, ry, rz, rw,
                                                    getTickCache(currentTick - 1),
                                                    getTickCache(currentTick));
            }
        }

        void waitPreRender() {
            for (const auto &item: preCache) {
                if (item.has_value()) {
                    item.value().get();
                }
            }
        }
    };

}// namespace OpenParticle

#endif//OPENPARTICLE_PARTICLEMANAGER_H
