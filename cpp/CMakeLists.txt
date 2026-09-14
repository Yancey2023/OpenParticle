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

if (MSVC)
    add_compile_options("/utf-8")
endif ()

if (CMAKE_SYSTEM_NAME MATCHES "Linux" AND NOT CMAKE_CXX_FLAGS MATCHES "-pthread")
    set(CMAKE_CXX_FLAGS ${CMAKE_CXX_FLAGS} "-pthread")
endif ()

set(OpenParticleCoreFiles
        src/openparticle/ParticleReader.cpp
        src/openparticle/ParticleReader.h
        src/openparticle/ParticleTicker.cpp
        src/openparticle/ParticleTicker.h
        src/openparticle/ParticleRender.cpp
        src/openparticle/ParticleRender.h
        src/openparticle/ParticleManager.cpp
        src/openparticle/ParticleManager.h)

add_library(OpenParticle SHARED
        src/extern/yancey_openparticle_api_common_nativecore_OpenParticleProject.cpp
        src/extern/yancey_openparticle_api_common_nativecore_OpenParticleProject.h
        ${OpenParticleCoreFiles}
)
target_include_directories(OpenParticle PUBLIC jni)
target_include_directories(OpenParticle PUBLIC include)
target_link_libraries(OpenParticle PUBLIC Eigen3::Eigen)
target_precompile_headers(OpenParticle PRIVATE include/pch.h)
if (CMAKE_SYSTEM_NAME MATCHES "Linux")
    target_include_directories(OpenParticle PUBLIC jni/linux)
elseif (CMAKE_SYSTEM_NAME MATCHES "Windows")
    target_include_directories(OpenParticle PUBLIC jni/win32)
endif ()

add_executable(OpenParticleTest
        src/test/test.cpp
        ${OpenParticleCoreFiles}
)
target_include_directories(OpenParticleTest PUBLIC include)
target_link_libraries(OpenParticleTest PRIVATE Eigen3::Eigen)
target_precompile_headers(OpenParticleTest PRIVATE include/pch.h)
