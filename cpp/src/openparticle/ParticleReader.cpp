//
// Created by Yancey on 2024/4/8.
//

#include "ParticleReader.h"

namespace OpenParticle {

    Identifier::Identifier(const std::optional<std::string> &nameSpace,
                           std::string value)
        : nameSpace(nameSpace),
          value(std::move(value)) {}

    DataMatrix::DataMatrix(DataMatrix &&dataMatrix) noexcept
        : type(dataMatrix.type) {
        switch (type) {
            case MatrixType::NONE:
                break;
            case MatrixType::STATIC:
                matrix = dataMatrix.matrix;
                break;
            case MatrixType::FREE:
                matrices = dataMatrix.matrices;
                break;
        }
    }

    DataMatrix::~DataMatrix() {
        if (type == MatrixType::FREE) {
            delete[] matrices;
        }
    }

    DataColor::DataColor(DataColor &&dataColor) noexcept
        : type(dataColor.type) {
        switch (type) {
            case ColorType::NONE:
                break;
            case ColorType::STATIC:
                color = dataColor.color;
                break;
            case ColorType::FREE:
                colors = dataColor.colors;
                break;
        }
    }

    DataColor::~DataColor() {
        if (type == ColorType::FREE) {
            delete[] colors;
        }
    }

    [[maybe_unused]] Particle::Particle(ParticleType::ParticleType type)
        : type(type) {}

    [[maybe_unused]] ParticleSingle::ParticleSingle(Identifier *identifier,
                                                    int32_t age)
        : Particle(ParticleType::SINGLE),
          identifier(identifier),
          age(age) {}

    [[maybe_unused]] ParticleCompound::ParticleCompound(const std::vector<Particle *> &children)
        : Particle(ParticleType::COMPOUND),
          children(children) {
        for (const auto &item: children) {
            if (item->type == ParticleType::COMPOUND) {
                throw std::runtime_error("compound particle's child can not have compound particle");
            }
        }
    }

    [[maybe_unused]] ParticleTransform::ParticleTransform(Particle *child,
                                                          DataMatrix &&dataMatrix,
                                                          DataColor &&dataColor,
                                                          int32_t tickAdd)
        : Particle(ParticleType::TRANSFORM),
          child(child),
          dataMatrix(std::move(dataMatrix)),
          dataColor(std::move(dataColor)),
          tickAdd(tickAdd) {
        if (child->type == ParticleType::TRANSFORM) {
            throw std::runtime_error("transform particle's child can not be a transform particle");
        }
    }

    std::optional<Eigen::Matrix4f> ParticleTransform::getTransform(int32_t age) const {
        switch (dataMatrix.type) {
            case MatrixType::NONE:
                return std::nullopt;
            case MatrixType::STATIC:
                return dataMatrix.matrix;
            case MatrixType::FREE:
#if OpenParticleDebug == true
                if (age < 0 || age >= dataMatrix.size) {
                    throw std::runtime_error("age is not in range of (0, dataMatrix.size)");
                }
#endif
                return dataMatrix.matrices[age];
            default:
                throw std::runtime_error("error transform type when getting transform");
        }
    }

    std::optional<int32_t> ParticleTransform::getColor(int32_t age) const {
        switch (dataColor.type) {
            case ColorType::NONE:
                return std::nullopt;
            case ColorType::STATIC:
                return dataColor.color;
            case ColorType::FREE:
#if OpenParticleDebug == true
                if (age < 0 || age >= dataColor.size) {
                    throw std::runtime_error("age is not in range of (0, dataColor.size)");
                }
#endif
                return dataColor.colors[age];
            default:
                throw std::runtime_error("error color type when getting color");
        }
    }

    [[maybe_unused]] ParticleData::ParticleData(std::vector<Identifier> &&identifiers,
                                                std::vector<std::unique_ptr<Particle>> &&particles,
                                                Particle *root)
        : identifiers(std::move(identifiers)),
          particles(std::move(particles)),
          root(root) {
        if (root->type == ParticleType::COMPOUND) {
            throw std::runtime_error("you can't use compound node as root");
        }
    }

}// namespace OpenParticle
