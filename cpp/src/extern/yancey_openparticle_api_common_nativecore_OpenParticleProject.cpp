#ifndef _GLIBCXX_HAS_GTHREADS
#error you maybe forget add -pthread param when compiling in linux
#endif

#include "yancey_openparticle_api_common_nativecore_OpenParticleProject.h"
#include "../openparticle/ParticleManager.h"

const int cacheSize = 20;

jlong Java_yancey_openparticle_api_common_nativecore_OpenParticleProject_readFile(JNIEnv *env, jclass, jstring path, jobject bridge) {
    const char *cstr = env->GetStringUTFChars(path, nullptr);
    OpenParticle::ParticleManager<cacheSize> *particleManager;
    try {
        particleManager = new OpenParticle::ParticleManager<cacheSize>(
                cstr,
                [&env, &bridge](OpenParticle::Identifier &identifier) {
                    jmethodID methodID = env->GetMethodID(env->GetObjectClass(bridge),
                                                          "getParticleSpritesData",
                                                          "(Ljava/lang/String;Ljava/lang/String;)[F");
                    jfloatArray floatArray = (jfloatArray) env->CallObjectMethod(
                            bridge, methodID,
                            env->NewStringUTF(identifier.nameSpace.value_or("minecraft").c_str()),
                            env->NewStringUTF(identifier.value.c_str()));
                    jsize floatArrayLength = env->GetArrayLength(floatArray);
                    size_t size = floatArrayLength / 4;
                    identifier.sprites.reserve(size);
                    jfloat *cFloatArray = env->GetFloatArrayElements(floatArray, nullptr);
                    for (int i = 0; i < size; ++i) {
                        int index = 4 * i;
                        identifier.sprites.push_back(OpenParticle::Sprite{
                                cFloatArray[index],
                                cFloatArray[index + 1],
                                cFloatArray[index + 2],
                                cFloatArray[index + 3]});
                    }
                    env->ReleaseFloatArrayElements(floatArray, cFloatArray, 0);
                });
        particleManager->waitPreRender();
    } catch (std::exception &error) {
        particleManager = nullptr;
    }
    env->ReleaseStringUTFChars(path, cstr);
    return reinterpret_cast<jlong>(particleManager);
}

void Java_yancey_openparticle_api_common_nativecore_OpenParticleProject_release(JNIEnv *, jclass, jlong particleManagerPointer) {
    delete reinterpret_cast<OpenParticle::ParticleManager<cacheSize> *>(particleManagerPointer);
}

jint Java_yancey_openparticle_api_common_nativecore_OpenParticleProject_getTickEnd(JNIEnv *, jclass, jlong particleManagerPointer) {
    return reinterpret_cast<OpenParticle::ParticleManager<cacheSize> *>(particleManagerPointer)->getTickEnd();
}

void Java_yancey_openparticle_api_common_nativecore_OpenParticleProject_prepareTickCache(JNIEnv *, jclass, jlong particleManagerPointer, jint tick) {
    return reinterpret_cast<OpenParticle::ParticleManager<cacheSize> *>(particleManagerPointer)->tick(tick);
}

jint Java_yancey_openparticle_api_common_nativecore_OpenParticleProject_getParticleCount(JNIEnv *, jclass, jlong particleManagerPointer) {
    return reinterpret_cast<OpenParticle::ParticleManager<cacheSize> *>(particleManagerPointer)->getParticleCount();
}

jint Java_yancey_openparticle_api_common_nativecore_OpenParticleProject_getVBOSize(JNIEnv *, jclass, jlong particleManagerPointer) {
    return reinterpret_cast<OpenParticle::ParticleManager<cacheSize> *>(particleManagerPointer)->getVBOSize();
}

void Java_yancey_openparticle_api_common_nativecore_OpenParticleProject_render(JNIEnv *env, jclass, jlong particleManagerPointer,
                                                                               jobject directBuffer,
                                                                               jboolean isSingleThread,
                                                                               jfloat tickDelta,
                                                                               jfloat cameraX, jfloat cameraY, jfloat cameraZ,
                                                                               jfloat rx, jfloat ry, jfloat rz, jfloat rw) {
    auto *buffer = static_cast<uint8_t *>(env->GetDirectBufferAddress(directBuffer));
    reinterpret_cast<OpenParticle::ParticleManager<cacheSize> *>(particleManagerPointer)->doRender(isSingleThread, buffer, tickDelta, cameraX, cameraY, cameraZ, rx, ry, rz, rw);
}