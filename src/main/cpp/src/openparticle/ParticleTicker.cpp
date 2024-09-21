//
// Created by Yancey on 2024/4/9.
//

#include "ParticleTicker.h"

#if OpenParticleDebug == true
#define ASSERT_NOTNULL(ptr) \
    if (ptr == nullptr) { throw std::runtime_error(std::string("warn: ") + #ptr + " is null"); }
#else
#define ASSERT_NOTNULL(ptr)
#endif

namespace OpenParticle {

    static std::optional<Eigen::Matrix4f>
    multiply(const std::optional<Eigen::Matrix4f> &matrix1, const std::optional<Eigen::Matrix4f> &matrix2) {
        if (HEDLEY_LIKELY(!matrix1.has_value())) {
            return matrix2;
        } else if (HEDLEY_LIKELY(!matrix2.has_value())) {
            return matrix1;
        }
        return matrix1.value() * matrix2.value();
    }

    Node::Node() {// NOLINT(*-pro-type-member-init)
#if OpenParticleDebug == true
        particle = nullptr;
        isTransformNode = false;
#endif
    }

    Node::~Node() {
        if (HEDLEY_LIKELY(isTransformNode)) {
            delete[] children;
#if OpenParticleDebug == true
            children = nullptr;
#endif
        }
    }

    void Node::init(Particle *particle0,
                    Node *nodeList,
                    size_t &nextIndex) {
        ASSERT_NOTNULL(particle0)
        ASSERT_NOTNULL(nodeList)
        particle = particle0;
        isTransformNode = particle->type == ParticleType::TRANSFORM;
        if (HEDLEY_LIKELY(isTransformNode)) {
            childrenSize = getRootNodeListSize(static_cast<ParticleTransform *>(particle)->child);
            children = new Node *[childrenSize];
            ASSERT_NOTNULL(children)
#if OpenParticleDebug == true
            for (int i = 0; i < childrenSize; ++i) {
                children[i] = nullptr;
            }
#endif
            collectNode(static_cast<ParticleTransform *>(particle)->child, nodeList, nextIndex, children);
#if OpenParticleDebug == true
            for (int i = 0; i < childrenSize; ++i) {
                if (children[i] == nullptr) {
                    throw std::runtime_error("child should not be null");
                }
            }
#endif
        }
        ASSERT_NOTNULL(particle)
    }

    void Node::setTickStart(int32_t tickStart0) {
        ASSERT_NOTNULL(particle)
        if (HEDLEY_LIKELY(isTransformNode)) {
            ASSERT_NOTNULL(children)
            tickStart = tickStart0 + static_cast<ParticleTransform *>(particle)->tickAdd;
            tickEnd = 0;
            for (int i = 0; i < childrenSize; ++i) {
                Node *item = children[i];
                ASSERT_NOTNULL(item)
                item->setTickStart(tickStart);
                tickEnd = std::max(tickEnd, item->tickEnd);
            }
        } else {
            tickStart = tickStart0;
            tickEnd = tickStart + static_cast<ParticleSingle *>(particle)->age;
        }
    }

    void Node::buildCache(int32_t tick,
                          const TransformNodeCache &parentCache,
                          std::vector<std::optional<SingleParticleNodeCache>> &caches) const {
        ASSERT_NOTNULL(particle)
        if (HEDLEY_LIKELY(tick < tickStart || tick >= tickEnd)) {
            return;
        }
        if (HEDLEY_LIKELY(isTransformNode)) {
            ASSERT_NOTNULL(children)
            TransformNodeCache nodeCache;
            nodeCache.cachePosition = multiply(parentCache.cachePosition,
                                               static_cast<ParticleTransform *>(particle)->getTransform(tick - tickStart));
            nodeCache.cacheColor = parentCache.cacheColor.has_value() ? parentCache.cacheColor : static_cast<ParticleTransform *>(particle)->getColor(tick - tickStart);
            for (int i = 0; i < childrenSize; ++i) {
                children[i]->buildCache(tick, nodeCache, caches);
            }
        } else {
            SingleParticleNodeCache nodeCache;// NOLINT(*-pro-type-member-init)
            if (HEDLEY_LIKELY(parentCache.cachePosition.has_value())) {
                nodeCache.x = parentCache.cachePosition.value()(0, 3);
                nodeCache.y = parentCache.cachePosition.value()(1, 3);
                nodeCache.z = parentCache.cachePosition.value()(2, 3);
            } else {
                nodeCache.x = 0;
                nodeCache.y = 0;
                nodeCache.z = 0;
            }
            nodeCache.color = parentCache.cacheColor.value_or(0xFFFFFFFF);
            int age = tick - tickStart;
            int maxAge = tickEnd - tickStart;
            std::vector<Sprite> &sprites = static_cast<ParticleSingle *>(particle)->identifier->sprites;
            nodeCache.sprite = &sprites[age * (sprites.size() - 1) / maxAge];
            caches[id] = nodeCache;
        }
    }

    size_t getRootNodeListSize(Particle *particle) {
        ASSERT_NOTNULL(particle)
        switch (particle->type) {
            case ParticleType::SINGLE:
            case ParticleType::TRANSFORM:
                return 1;
            case ParticleType::COMPOUND:
                return static_cast<ParticleCompound *>(particle)->children.size();
            default:
                throw std::runtime_error("unknown particle type when getting root node list size");
        }
    }

    size_t getNodeListSize(Particle *particle) {
        ASSERT_NOTNULL(particle)
        size_t result = 0;
        switch (particle->type) {
            case ParticleType::SINGLE:
                result++;
                break;
            case ParticleType::TRANSFORM:
                result++;
                result += getNodeListSize(static_cast<ParticleTransform *>(particle)->child);
                break;
            case ParticleType::COMPOUND:
                for (Particle *child: static_cast<ParticleCompound *>(particle)->children) {
                    result += getNodeListSize(child);
                }
                break;
        }
        return result;
    }

    void collectNode(Particle *particle,
                     Node *nodeList,
                     size_t &nextIndex,
                     Node **rootNodes) {
        ASSERT_NOTNULL(particle)
        ASSERT_NOTNULL(nodeList)
        ASSERT_NOTNULL(rootNodes)
        switch (particle->type) {
            case ParticleType::SINGLE:
            case ParticleType::TRANSFORM: {
                size_t index = nextIndex++;
                nodeList[index].init(particle, nodeList, nextIndex);
                ASSERT_NOTNULL(nodeList[index].particle)
                rootNodes[0] = &nodeList[index];
                ASSERT_NOTNULL(rootNodes[0])
            } break;
            case ParticleType::COMPOUND: {
                const std::vector<Particle *> &children = static_cast<ParticleCompound *>(particle)->children;
                for (int i = 0; i < children.size(); ++i) {
                    size_t index = nextIndex++;
                    nodeList[index].init(children[i], nodeList, nextIndex);
                    ASSERT_NOTNULL(nodeList[index].particle)
                    rootNodes[i] = &nodeList[index];
                    ASSERT_NOTNULL(rootNodes[i])
                }
#if OpenParticleDebug == true
                if (((ParticleCompound *) particle)->children.empty()) {
                    throw std::runtime_error("compound particle's child should not be empty");
                }
#endif
            } break;
        }
    }

    ParticleTicker::ParticleTicker(const ParticleData *particleData) {
#if OpenParticleDebug == true
        nodeList = nullptr;
        particleSingleNodeList = nullptr;
        root = nullptr;
#endif
        size_t size = getNodeListSize(particleData->root);
        nodeList = new Node[size];
        ASSERT_NOTNULL(nodeList)
        Node *rootNodes[1];
        size_t nextIndex = 0;
        collectNode(particleData->root, nodeList, nextIndex, rootNodes);
        root = rootNodes[0];
        ASSERT_NOTNULL(root)
        root->setTickStart(0);
        particleSingleNodeListSize = 0;
        for (int i = 0; i < size; ++i) {
            if (HEDLEY_UNLIKELY(!nodeList[i].isTransformNode)) {
                particleSingleNodeListSize++;
            }
        }
        particleSingleNodeList = new Node *[particleSingleNodeListSize];
        ASSERT_NOTNULL(particleSingleNodeList)
#if OpenParticleDebug == true
        for (int i = 0; i < particleSingleNodeListSize; ++i) {
            particleSingleNodeList[i] = nullptr;
        }
#endif
        int index = 0;
        for (int i = 0; i < size; ++i) {
            if (HEDLEY_UNLIKELY(!nodeList[i].isTransformNode)) {
                nodeList[i].id = index;
                particleSingleNodeList[index++] = &nodeList[i];
            }
        }
#if OpenParticleDebug == true
        for (int i = 0; i < particleSingleNodeListSize; ++i) {
            if (particleSingleNodeList[i] == nullptr) {
                throw std::runtime_error("particle single node in list should not be null");
            }
        }
#endif
    }

    ParticleTicker::~ParticleTicker() {
        delete[] nodeList;
        delete[] particleSingleNodeList;
#if OpenParticleDebug == true
        nodeList = nullptr;
        particleSingleNodeList = nullptr;
#endif
    }

    std::vector<std::optional<SingleParticleNodeCache>> ParticleTicker::buildCache(int32_t tick) const {
        std::vector<std::optional<SingleParticleNodeCache>> result(particleSingleNodeListSize);
        root->buildCache(tick, {}, result);
        return result;
    }

}// namespace OpenParticle