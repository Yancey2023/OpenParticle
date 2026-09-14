//
// Created by Yancey on 24-4-11.
//

#ifndef OPENPARTICLE_PARTICLERENDER_H
#define OPENPARTICLE_PARTICLERENDER_H

#include "ParticleTicker.h"

namespace OpenParticle::ParticleRender {

    void doRenderSingleThread(uint8_t *buffer, size_t particleCount, float tickDelta,
                              float cameraX, float cameraY, float cameraZ,
                              float rx, float ry, float rz, float rw,
                              const std::vector<std::optional<SingleParticleNodeCache>> *lastTickCache,
                              const std::vector<std::optional<SingleParticleNodeCache>> *currentTickCache);

    void doRenderMultiThread(uint8_t *buffer, size_t particleCount, float tickDelta,
                             float cameraX, float cameraY, float cameraZ,
                             float rx, float ry, float rz, float rw,
                             const std::vector<std::optional<SingleParticleNodeCache>> *lastTickCache,
                             const std::vector<std::optional<SingleParticleNodeCache>> *currentTickCache);

}// namespace OpenParticle::ParticleRender

#endif//OPENPARTICLE_PARTICLERENDER_H
