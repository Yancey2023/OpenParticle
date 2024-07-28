//
// Created by Yancey on 2024/4/9.
//

#ifndef OPENPARTICLE_PARTICLETICKER_H
#define OPENPARTICLE_PARTICLETICKER_H

#include "ParticleReader.h"
#include "pch.h"

namespace OpenParticle {

    struct TransformNodeCache {
        std::optional<Eigen::Matrix4f> cachePosition;
        std::optional<int32_t> cacheColor;
    };

    struct SingleParticleNodeCache {
        float x, y, z;
        int32_t color;
        Sprite *sprite;
    };

    class Node {
    public:
        bool isTransformNode;
        Particle *particle;
        int32_t tickStart, tickEnd;
        union {
            size_t id;
            struct {
                size_t childrenSize;
                Node **children;
            };
        };

        Node();

        ~Node();

        void init(Particle *particle0,
                  Node *nodeList,
                  size_t &nextIndex);

        void setTickStart(int32_t tickStart0);

        void buildCache(int32_t tick,
                        const TransformNodeCache &parentCache,
                        std::vector<std::optional<SingleParticleNodeCache>> &caches) const;
    };

    size_t getRootNodeListSize(Particle *particle);

    size_t getNodeListSize(Particle *particle);

    void collectNode(Particle *particle,
                     Node *nodeList,
                     size_t &nextIndex,
                     Node **rootNodes);

    class ParticleTicker {
    public:
        Node *nodeList;
        size_t particleSingleNodeListSize;
        Node **particleSingleNodeList;
        Node *root;

        explicit ParticleTicker(ParticleData *particleData);

        ~ParticleTicker();

        [[nodiscard]] std::vector<std::optional<SingleParticleNodeCache>> buildCache(int32_t tick) const;
    };


}// namespace OpenParticle

#endif//OPENPARTICLE_PARTICLETICKER_H
