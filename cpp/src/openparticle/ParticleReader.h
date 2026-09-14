//
// Created by Yancey on 2024/4/8.
//

#ifndef OPENPARTICLE_PARTICLE_H
#define OPENPARTICLE_PARTICLE_H

namespace OpenParticle {

    class DataReader {
    public:
        std::istream &istream;

        explicit DataReader(std::istream &istream)
            : istream(istream) {}

        [[nodiscard]] bool readBoolean() const {
            return readByte();
        }

        [[nodiscard]] int8_t readByte() const {
            int8_t value;
            istream.read(reinterpret_cast<char *>(&value), sizeof(int8_t));
            return value;
        }

        [[nodiscard]] uint16_t readUnsignedShort() const {
            uint16_t value;
            istream.read(reinterpret_cast<char *>(&value), sizeof(uint16_t));
            if constexpr (std::endian::native == std::endian::little) {
                return static_cast<uint16_t>((value >> 8) | (value << 8));
            }
            return value;
        }

        [[nodiscard]] int32_t readInt() const {
            int32_t value;
            istream.read(reinterpret_cast<char *>(&value), sizeof(int32_t));
            const uint32_t bits = static_cast<uint32_t>(value);
            if constexpr (std::endian::native == std::endian::little) {
                return static_cast<int32_t>(((bits >> 24) & 0x000000FFU) |
                                            ((bits >> 8) & 0x0000FF00U) |
                                            ((bits << 8) & 0x00FF0000U) |
                                            ((bits << 24) & 0xFF000000U));
            }
            return value;
        }

        [[nodiscard]] float readFloat() const {
            const union {
                int32_t i;
                float f;
            } int2float{readInt()};
            return int2float.f;
        }

        [[nodiscard]] std::string readString() const {
            uint16_t length = readUnsignedShort();
            std::vector<char> buffer(length);
            istream.read(buffer.data(), length);
            return {buffer.begin(), buffer.end()};
        }
    };

    struct Sprite {
        float minU, minV, maxU, maxV;
    };

    class Identifier {
    public:
        const std::optional<std::string> nameSpace;
        const std::string value;
        std::vector<Sprite> sprites;

        Identifier(const std::optional<std::string> &nameSpace,
                   std::string value);

        explicit Identifier(DataReader &dataReader)
            : nameSpace(dataReader.readBoolean() ? std::optional<std::string>() : dataReader.readString()),
              value(dataReader.readString()) {}
    };

    inline Eigen::Matrix4f readMatrix(const DataReader &dataReader) {
        float data[16];
        for (float &i: data) {
            i = dataReader.readFloat();
        }
        return Eigen::Matrix4f(data);
    }

    inline int32_t readColor(DataReader &dataReader) {
        int32_t rgba = dataReader.readInt();
        return rgba << 24 | (rgba >> 8 & 0x00FFFFFF);
    }

    inline std::vector<Identifier> readIdentifierList(DataReader &dataReader,
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

    namespace MatrixType {

        enum MatrixType : int8_t {
            NONE = 0,
            STATIC = 1,
            FREE = 2
        };

    }// namespace MatrixType

    namespace ColorType {

        enum ColorType : int8_t {
            NONE = 0,
            STATIC = 1,
            FREE = 2
        };

    }// namespace ColorType

    class DataMatrix {
    public:
        const MatrixType::MatrixType type;
        union {
            Eigen::Matrix4f matrix;
#if OpenParticleDebug == true
            struct {
                int32_t size;
#endif
                Eigen::Matrix4f *matrices;
#if OpenParticleDebug == true
            };
#endif
        };

        DataMatrix(DataMatrix &&dataMatrix) noexcept;

        explicit DataMatrix(const DataReader &dataReader)
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

        ~DataMatrix();
    };

    class DataColor {
    public:
        const ColorType::ColorType type;
        const union {
            int32_t color;
#if OpenParticleDebug == true
            struct {
                int32_t size;
#endif
                int32_t *colors;
#if OpenParticleDebug == true
            };
#endif
        };

        DataColor(DataColor &&dataColor) noexcept;

        explicit DataColor(DataReader &dataReader)
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

        ~DataColor();
    };

    namespace ParticleType {

        enum ParticleType : uint8_t {
            SINGLE = 0,
            COMPOUND = 1,
            TRANSFORM = 2
        };

    }// namespace ParticleType

    class Particle {
    public:
        const ParticleType::ParticleType type;

    protected:
        explicit Particle(ParticleType::ParticleType type);

    public:
        virtual ~Particle() = default;
    };

    inline Particle *readParticleId(DataReader &dataReader, const std::vector<std::unique_ptr<Particle>> &particles) {
        int32_t index = dataReader.readInt();
        if (index < 0 || index >= particles.size()) {
            throw std::runtime_error("particle id error");
        }
        return particles[index].get();
    }

    inline Identifier *readIdentifierId(const DataReader &dataReader,
                                 std::vector<Identifier> &identifiers) {
        int32_t index = dataReader.readInt();
        if (index < 0 || index >= identifiers.size()) {
            throw std::runtime_error("error identifier id");
        }
        return &identifiers[index];
    }

    inline std::vector<Particle *> readParticleIdList(DataReader &dataReader,
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

    class ParticleSingle : public Particle {
    public:
        Identifier *identifier;
        const int32_t age;

        [[maybe_unused]]
        ParticleSingle(Identifier *identifier,
                       int32_t age);

        explicit ParticleSingle(const DataReader &dataReader,
                                std::vector<Identifier> &identifiers)
            : Particle(ParticleType::SINGLE),
              identifier(readIdentifierId(dataReader, identifiers)),
              age(dataReader.readInt()) {}
    };

    class ParticleCompound : public Particle {
    public:
        const std::vector<Particle *> children;

        [[maybe_unused]] explicit ParticleCompound(const std::vector<Particle *> &children);

        ParticleCompound(DataReader &dataReader,
                         const std::vector<std::unique_ptr<Particle>> &particles)
            : Particle(ParticleType::COMPOUND),
              children(readParticleIdList(dataReader, particles)) {
            for (const auto &item: children) {
                if (item->type == ParticleType::COMPOUND) {
                    throw std::runtime_error("compound particle's child can not have compound particle");
                }
            }
        }
    };

    class ParticleTransform : public Particle {
    public:
        Particle *child;
        const DataMatrix dataMatrix;
        const DataColor dataColor;
        const int32_t tickAdd;

        [[maybe_unused]]
        ParticleTransform(Particle *child,
                          DataMatrix &&dataMatrix,
                          DataColor &&dataColor,
                          int32_t tickAdd);

        ParticleTransform(DataReader &dataReader,
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

        [[nodiscard]] std::optional<Eigen::Matrix4f> getTransform(int32_t age) const;

        [[nodiscard]] std::optional<int32_t> getColor(int32_t age) const;
    };

    inline std::unique_ptr<Particle> readParticle(DataReader &dataReader,
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

    inline std::vector<std::unique_ptr<Particle>> readParticleList(DataReader &dataReader,
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

    class ParticleData {
    public:
        std::vector<Identifier> identifiers;
        const std::vector<std::unique_ptr<Particle>> particles;
        Particle *root;

        [[maybe_unused]]
        ParticleData(std::vector<Identifier> &&identifiers,
                     std::vector<std::unique_ptr<Particle>> &&particles,
                     Particle *root);

        explicit ParticleData(DataReader &dataReader,
                              const std::function<void(Identifier &identifier)> &setSprite)
            : identifiers(readIdentifierList(dataReader, setSprite)),
              particles(readParticleList(dataReader, identifiers)),
              root(readParticleId(dataReader, particles)) {
            if (root->type == ParticleType::COMPOUND) {
                throw std::runtime_error("you can't use compound node as root");
            }
        }
    };

}// namespace OpenParticle

#endif//OPENPARTICLE_PARTICLE_H
