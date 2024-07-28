#ifdef __GNUC__
#ifndef _GLIBCXX_HAS_GTHREADS
#error you maybe forget add -pthread param when compiling in linux
#endif
#endif

#include "../openparticle/ParticleManager.h"
#include <chrono>
#include <iostream>
#include <thread>

int test1() {
    std::string path = R"(D:\CLion\project\OpenParticle\run\315000.par)";
    //    std::string path = R"(/home/yancey/CLionProjects/OpenParticle/run/315000.par)";
    std::chrono::high_resolution_clock::time_point start, end;
    start = std::chrono::high_resolution_clock::now();
    OpenParticle::ParticleManager<20> nodeManager(path.c_str(), [](OpenParticle::Identifier &identifier) {
        identifier.sprites.push_back(OpenParticle::Sprite{0.1, 0.2, 0.3, 0.4});
    });
    nodeManager.waitPreRender();
    end = std::chrono::high_resolution_clock::now();
    std::cout << "init successfully ("
              << std::chrono::duration_cast<std::chrono::duration<float, std::milli>>(end - start).count()
              << "ms)"
              << std::endl;
    start = std::chrono::high_resolution_clock::now();
    int32_t tickEnd = nodeManager.getTickEnd();
    std::chrono::milliseconds duration(50);
    int framePerTick = 3;
    for (int j = 0; j < 1; j++) {
        for (int32_t i = 0; i < tickEnd; ++i) {
            std::chrono::high_resolution_clock::time_point beforeTick = std::chrono::high_resolution_clock::now();
            nodeManager.tick(i);
            auto tickCache = nodeManager.getTickCache(i);
            std::chrono::high_resolution_clock::time_point afterTick = std::chrono::high_resolution_clock::now();
            std::cout << "tick: "
                      << i
                      << " ("
                      << std::chrono::duration_cast<std::chrono::duration<float, std::milli>>(afterTick - beforeTick).count()
                      << "ms)"
                      << std::endl;
            if (tickCache == nullptr) {
                continue;
            }
            std::chrono::high_resolution_clock::time_point beforeTick2 = std::chrono::high_resolution_clock::now();
            int32_t size;
            uint8_t *buffer;
            for (int k = 0; k < framePerTick; ++k) {
                size = nodeManager.getVBOSize();
                if (size != 0) {
                    buffer = new uint8_t[size];
                    nodeManager.doRender(false, buffer, (float) k / (float) framePerTick, 0, 0, 0, 0, 0, 0, 1);
                    delete[] buffer;
                }
            }
            std::chrono::high_resolution_clock::time_point afterTick2 = std::chrono::high_resolution_clock::now();
            std::cout << "render: "
                      << i
                      << " ("
                      << std::chrono::duration_cast<std::chrono::duration<float, std::milli>>(afterTick2 - beforeTick2).count()
                      << "ms)"
                      << std::endl;

            std::this_thread::sleep_for(duration - (afterTick2 - beforeTick));

            //std::this_thread::sleep_for(duration - (afterTick - beforeTick));
        }
    }
    end = std::chrono::high_resolution_clock::now();
    std::cout << "run particle successfully ("
              << std::chrono::duration_cast<std::chrono::duration<float, std::milli>>(end - start).count()
              << "ms)"
              << std::endl;
    return 0;
}

int test2() {
    std::string tickPath = R"(D:\CLion\project\OpenParticle\run\tick.txt)";
    std::string renderPath = R"(D:\CLion\project\OpenParticle\run\render.txt)";
    std::string path = R"(D:\CLion\project\OpenParticle\run\1.par)";
    //    std::string path = R"(/home/yancey/CLionProjects/OpenParticle/run/1.par)";
    std::chrono::high_resolution_clock::time_point start, end;
    start = std::chrono::high_resolution_clock::now();
    std::ifstream istream(path, std::ios::in | std::ios::binary);
    OpenParticle::DataReader dataReader(istream);
    auto *particleData = new OpenParticle::ParticleData(dataReader, [](OpenParticle::Identifier &identifier) {
        identifier.sprites.push_back(OpenParticle::Sprite{0.1, 0.2, 0.3, 0.4});
    });
    istream.close();
    auto *particleTicker = new OpenParticle::ParticleTicker(particleData);
    end = std::chrono::high_resolution_clock::now();
    std::cout << "init successfully ("
              << std::chrono::duration_cast<std::chrono::duration<float, std::milli>>(end - start).count()
              << "ms)"
              << std::endl;
    start = std::chrono::high_resolution_clock::now();
    int32_t tickEnd = particleTicker->root->tickEnd;
    FILE *tickFile = fopen(tickPath.c_str(), "w");
    fprintf(tickFile, "tick range: [0, %d)\n\n", tickEnd);
    FILE *renderFile = fopen(renderPath.c_str(), "w");
    for (int32_t i = 0; i < tickEnd; ++i) {
        auto lastTickCache = particleTicker->buildCache(i == 0 ? i : i - 1);
        auto tickCache = particleTicker->buildCache(i);
        fprintf(tickFile, "---- tick: %d ------\n", i);
        int index = 0;
        for (const auto &item: tickCache) {
            if (item.has_value()) {
                index++;
                fprintf(tickFile, "%d. pos:(%.1f, %.1f, %.1f), ARGB:(%d, %d, %d, %d)\n",
                        index++,
                        item->x,
                        item->y,
                        item->z,
                        (item->color >> 24 & 0xFF),
                        (item->color >> 16 & 0xFF),
                        (item->color >> 8 & 0xFF),
                        (item->color >> 0 & 0xFF));
            }
        }
        fprintf(tickFile, "\n");

        int particleCount = 0;
        for (const auto &item: tickCache) {
            if (item.has_value()) {
                particleCount++;
            }
        }
        int32_t size = particleCount * 112;
        auto *buffer = new uint8_t[size];

        const int framePerTick = 4;
        for (int frame = 1; frame <= framePerTick; ++frame) {
            float tickDelta = (float) frame / framePerTick;
            OpenParticle::ParticleRender::doRenderMultiThread(buffer, particleCount, tickDelta,
                                                              0, 0, 0,
                                                              0, 0, 0, 1,
                                                              &lastTickCache,
                                                              &tickCache);
            fprintf(renderFile, "---- tick: %d (%.2f) ------\n", i, tickDelta);
            for (int j = 0; j < particleCount; ++j) {
                fprintf(renderFile, "%d. ", j + 1);
                for (int k = 0; k < 4; k++) {
                    int offset = j * 112 + k * 28;
                    fprintf(renderFile, "pos:(%.1f, %.1f, %.1f), ARGB:(%d, %d, %d, %d) | ",
                            *(float *) (buffer + offset + 0),
                            *(float *) (buffer + offset + 4),
                            *(float *) (buffer + offset + 8),
                            *(uint8_t *) (buffer + offset + 20),
                            *(uint8_t *) (buffer + offset + 21),
                            *(uint8_t *) (buffer + offset + 22),
                            *(uint8_t *) (buffer + offset + 23));
                }
                fprintf(renderFile, "\n");
            }
            fprintf(renderFile, "\n");
        }
        fprintf(renderFile, "\n");
        delete[] buffer;
    }
    fclose(tickFile);
    fclose(renderFile);
    end = std::chrono::high_resolution_clock::now();
    std::cout << "run particle successfully ("
              << std::chrono::duration_cast<std::chrono::duration<float, std::milli>>(end - start).count()
              << "ms)"
              << std::endl;
    return 0;
}

int main() {
    test2();
}