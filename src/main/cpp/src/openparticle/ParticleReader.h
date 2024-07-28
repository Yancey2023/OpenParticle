//
// Created by Yancey on 2024/4/8.
//

#ifndef OPENPARTICLE_PARTICLE_H
#define OPENPARTICLE_PARTICLE_H

namespace OpenParticle {

    class DataReader {
    public:
        std::istream &istream;
        bool isSmallEndian;

        explicit DataReader(std::istream &istream);

        bool readBoolean() {
            return readByte();
        }

        int8_t readByte();

        uint16_t readUnsignedShort();

        int32_t readInt();

        float readFloat();

        std::string readString();
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

        explicit Identifier(DataReader &dataReader);
    };

    Eigen::Matrix4f readMatrix(DataReader &dataReader);

    static int32_t readColor(DataReader &dataReader) {
        int32_t rgba = dataReader.readInt();
        return rgba << 8 | (rgba >> 24 & 0x000000FF);
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

        explicit DataMatrix(DataReader &dataReader);

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

        explicit DataColor(DataReader &dataColor);

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
    public:
        explicit Particle(ParticleType::ParticleType type);

    public:
        virtual ~Particle() = default;
    };

    static Particle *
    readParticleId(DataReader &dataReader, const std::vector<std::unique_ptr<Particle>> &particles) {
        int32_t index = dataReader.readInt();
        if (index < 0 || index >= particles.size()) {
            throw std::runtime_error("particle id error");
        }
        return particles[index].get();
    }

    Identifier *readIdentifierId(DataReader &dataReader,
                                 std::vector<Identifier> &identifiers);

    std::vector<Particle *> readParticleIdList(DataReader &dataReader,
                                               const std::vector<std::unique_ptr<Particle>> &particles);

    class ParticleSingle : public Particle {
    public:
        Identifier *identifier;
        const int32_t age;

        [[maybe_unused]]
        ParticleSingle(Identifier *identifier,
                       int32_t age);

        explicit ParticleSingle(DataReader &dataReader,
                                std::vector<Identifier> &identifiers);
    };

    class ParticleCompound : public Particle {
    public:
        const std::vector<Particle *> children;

        [[maybe_unused]]
        explicit ParticleCompound(const std::vector<Particle *> &children);

        ParticleCompound(DataReader &dataReader,
                         const std::vector<std::unique_ptr<Particle>> &particles);
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
                          const std::vector<std::unique_ptr<Particle>> &particles);

        std::optional<Eigen::Matrix4f> getTransform(int32_t age);

        std::optional<int32_t> getColor(int32_t age);
    };

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
                              const std::function<void(Identifier &identifier)> &setSprite);
    };

}// namespace OpenParticle

#endif//OPENPARTICLE_PARTICLE_H
