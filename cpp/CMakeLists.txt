cmake_minimum_required(VERSION 3.27)
project(OpenParticle CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

include(FetchContent)
set(BUILD_TESTING OFF CACHE BOOL "Disable dependency tests" FORCE)
FetchContent_Declare(
        Eigen3
        GIT_REPOSITORY https://gitlab.com/libeigen/eigen.git
        GIT_TAG 3.4.0
        GIT_SHALLOW TRUE
)
FetchContent_MakeAvailable(Eigen3)

# Use the JNI headers bundled with the local JDK instead of vendoring them.
find_package(JNI REQUIRED)

if (MSVC)
    add_compile_options("/utf-8")
endif ()

set(THREADS_PREFER_PTHREAD_FLAG ON)
find_package(Threads REQUIRED)

set(OpenParticleCoreFiles
        src/openparticle/ParticleReader.cpp
        src/openparticle/ParticleTicker.cpp
        src/openparticle/ParticleRender.cpp
)

add_library(OpenParticle SHARED
        src/extern/yancey_openparticle_api_common_nativecore_OpenParticleProject.cpp
        src/extern/yancey_openparticle_api_common_nativecore_OpenParticleProject.h
        ${OpenParticleCoreFiles}
)
target_include_directories(OpenParticle PRIVATE src)
target_link_libraries(OpenParticle PRIVATE Eigen3::Eigen Threads::Threads JNI::JNI)

add_executable(OpenParticleTest
        tests/test.cpp
        ${OpenParticleCoreFiles}
)
target_include_directories(OpenParticleTest PRIVATE src)
target_link_libraries(OpenParticleTest PRIVATE Eigen3::Eigen Threads::Threads)
