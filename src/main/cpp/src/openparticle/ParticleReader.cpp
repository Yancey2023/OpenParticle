//
// Created by Yancey on 2024/4/8.
//

#include "ParticleReader.h"

namespace OpenParticle {

    DataReader::DataReader(std::istream &istream)
        : istream(istream) {
        union {
            int32_t int32 = 0x01020304;
            int8_t int8;
        } data;
        if (data.int8 == 0x04) {
            isSmallEndian = true;
        } else if (data.int8 == 0x01) {
            isSmallEndian = false;
        } else {
            throw std::runtime_error("unknown byte order");
        }
    }

    int8_t DataReader::readByte() {
        int8_t value;
        istream.read(reinterpret_cast<char *>(&value), sizeof(int8_t));
        return value;
    }

    uint16_t DataReader::readUnsignedShort() {
        uint16_t value;
        istream.read(reinterpret_cast<char *>(&value), sizeof(uint16_t));
        return isSmallEndian ? (value >> 8) | (value << 8) : value;
    }

    int32_t DataReader::readInt() {
        int32_t value;
        istream.read(reinterpret_cast<char *>(&value), sizeof(int32_t));
        return isSmallEndian ? ((value >> 24) & 0xFF) | ((value >> 8) & 0xFF00) |
                                       ((value << 8) & 0xFF0000) | (value << 24)
                             : value;
    }

    float DataReader::readFloat() {
        union {
            int32_t i;
            float f;
        } pun{readInt()};
        return pun.f;
    }

    std::string DataReader::readString() {
        uint16_t length = readUnsignedShort();
        std::vector<char> buffer(length);
        istream.read(buffer.data(), length);
        return {buffer.begin(), buffer.end()};
    }

    Identifier::Identifier(const std::optional<std::string> &nameSpace,
                           std::string value)
        : nameSpace(nameSpace),
          value(std::move(value)) {}

    Identifier::Identifier(DataReader &dataReader)
        : nameSpace(dataReader.readBoolean() ? std::optional<std::string>() : dataReader.readString()),
          value(dataReader.readString()) {}

    std::vector<Identifier> readIdentifierList(DataReader &dataReader,
                                               const std::function<void(Identifier &identifier)> &setSprite) {
        int32_t size = dataReader.readInt();
        if (size <= 0) {
            throw std::runtime_error("list size should be a positive number");
        }
        std::vector<Identifier> identifiers;
        identifiers.reserve(size);
        for (int32_t i = 0; i < size; ++i) {
            identifiers.emplace_back(dataReader);
        }
        for (int32_t i = 0; i < size; i++) {
            setSprite(identifiers[i]);
            if (identifiers[i].sprites.empty()) {
                throw std::runtime_error("sprites can not be empty");
            }
        }
        return identifiers;
    }

    Eigen::Matrix4f readMatrix(DataReader &dataReader) {
        float data[16];
        for (float &i: data) {
            i = dataReader.readFloat();
        }
        return Eigen::Matrix4f(data);
    }

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

    DataMatrix::DataMatrix(DataReader &dataReader)
        : type(static_cast<const MatrixType::MatrixType>(dataReader.readByte())) {
        switch (type) {
            case MatrixType::NONE:
                break;
            case MatrixType::STATIC:
                matrix = readMatrix(dataReader);
                break;
            case MatrixType::FREE:
#if OpenParticleDebug != true
                int32_t size;
#endif
                size = dataReader.readInt();
                if (size <= 0) {
                    throw std::runtime_error("list size should be a positive number");
                }
                matrices = new Eigen::Matrix4f[size];
                for (int32_t i = 0; i < size; i++) {
                    matrices[i] = readMatrix(dataReader);
                }
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

    DataColor::DataColor(DataReader &dataReader)
        : type(static_cast<const ColorType::ColorType>(dataReader.readByte())) {
        switch (type) {
            case ColorType::NONE:
                break;
            case ColorType::STATIC:
                color = readColor(dataReader);
                break;
            case ColorType::FREE:
#if OpenParticleDebug != true
                int32_t size;
#endif
                size = dataReader.readInt();
                if (size <= 0) {
                    throw std::runtime_error("list size should be a positive number");
                }
                colors = new int32_t[size];
                for (int32_t i = 0; i < size; i++) {
                    colors[i] = readColor(dataReader);
                }
                break;
        }
    }

    DataColor::~DataColor() {
        if (type == ColorType::FREE) {
            delete[] colors;
        }
    }

    Identifier *readIdentifierId(DataReader &dataReader,
                                 std::vector<Identifier> &identifiers) {
        int32_t index = dataReader.readInt();
        if (index < 0 || index >= identifiers.size()) {
            throw std::runtime_error("error identifier id");
        }
        return &identifiers[index];
    }

    std::vector<Particle *> readParticleIdList(DataReader &dataReader,
                                               const std::vector<std::unique_ptr<Particle>> &particles) {
        int32_t size = dataReader.readInt();
        if (size <= 0) {
            throw std::runtime_error("list size should be a positive number");
        }
        std::vector<Particle *> result;
        result.reserve(size);
        for (int32_t i = 0; i < size; i++) {
            result.push_back(readParticleId(dataReader, particles));
        }
        return result;
    }

    [[maybe_unused]] Particle::Particle(ParticleType::ParticleType type)
        : type(type) {}

    [[maybe_unused]] ParticleSingle::ParticleSingle(Identifier *identifier,
                                                    int32_t age)
        : Particle(ParticleType::SINGLE),
          identifier(identifier),
          age(age) {}

    ParticleSingle::ParticleSingle(DataReader &dataReader,
                                   std::vector<Identifier> &identifiers)
        : Particle(ParticleType::SINGLE),
          identifier(readIdentifierId(dataReader, identifiers)),
          age(dataReader.readInt()) {}

    [[maybe_unused]] ParticleCompound::ParticleCompound(const std::vector<Particle *> &children)
        : Particle(ParticleType::COMPOUND),
          children(children) {
        for (const auto &item: children) {
            if (item->type == ParticleType::COMPOUND) {
                throw std::runtime_error("compound particle's child can not have compound particle");
            }
        }
    }

    ParticleCompound::ParticleCompound(DataReader &dataReader,
                                       const std::vector<std::unique_ptr<Particle>> &particles)
        : Particle(ParticleType::COMPOUND),
          children(readParticleIdList(dataReader, particles)) {
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

    [[maybe_unused]] ParticleTransform::ParticleTransform(DataReader &dataReader,
                                                          const std::vector<std::unique_ptr<Particle>> &particles)
        : Particle(ParticleType::TRANSFORM),
          child(readParticleId(dataReader, particles)),
          dataMatrix(dataReader),
          dataColor(dataReader),
          tickAdd(dataReader.readInt()) {
        if (child->type == ParticleType::TRANSFORM) {
            throw std::runtime_error("transform particle's child can not be a transform particle");
        }
    }

    std::optional<Eigen::Matrix4f> ParticleTransform::getTransform(int32_t age) {
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

    std::optional<int32_t> ParticleTransform::getColor(int32_t age) {
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

    static std::unique_ptr<Particle>
    readParticle(DataReader &dataReader,
                 std::vector<Identifier> &identifiers,
                 const std::vector<std::unique_ptr<Particle>> &particles) {
        std::unique_ptr<Particle> particle;
        auto particleType = static_cast<ParticleType::ParticleType>(dataReader.readByte());
        switch (particleType) {
            case ParticleType::SINGLE:
                particle = std::make_unique<ParticleSingle>(dataReader, identifiers);
                break;
            case ParticleType::COMPOUND:
                particle = std::make_unique<ParticleCompound>(dataReader, particles);
                break;
            case ParticleType::TRANSFORM:
                particle = std::make_unique<ParticleTransform>(dataReader, particles);
                break;
            default:
                throw std::runtime_error("error particle type when reading file: " + std::to_string(particleType));
        }
        return particle;
    }

    static std::vector<std::unique_ptr<Particle>> readParticleList(DataReader &dataReader,
                                                                   std::vector<Identifier> &identifiers) {
        int32_t size = dataReader.readInt();
        if (size <= 0) {
            throw std::runtime_error("list size should be a positive number");
        }
        std::vector<std::unique_ptr<Particle>> particles;
        particles.reserve(size);
        for (int32_t i = 0; i < size; ++i) {
            particles.push_back(readParticle(dataReader, identifiers, particles));
        }
        return particles;
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

    ParticleData::ParticleData(DataReader &dataReader,
                               const std::function<void(Identifier &identifier)> &setSprite)
        : identifiers(readIdentifierList(dataReader, setSprite)),
          particles(readParticleList(dataReader, identifiers)),
          root(readParticleId(dataReader, particles)) {
        if (root->type == ParticleType::COMPOUND) {
            throw std::runtime_error("you can't use compound node as root");
        }
    }

}// namespace OpenParticle
