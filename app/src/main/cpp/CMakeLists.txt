cmake_minimum_required(VERSION 3.22.1)

project("medicalcompanion")

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Path to whisper.cpp source (relative from cpp folder)
# cpp/ -> main/ -> src/ -> app/ -> project root
set(WHISPER_DIR ${CMAKE_SOURCE_DIR}/../../../../whisper.cpp)

# Get whisper.cpp version
file(READ "${WHISPER_DIR}/CMakeLists.txt" MAIN_CMAKE_CONTENT)
string(REGEX MATCH "project\\(\"whisper\\.cpp\" VERSION ([0-9]+\\.[0-9]+\\.[0-9]+)\\)" VERSION_MATCH "${MAIN_CMAKE_CONTENT}")
if(CMAKE_MATCH_1)
    set(WHISPER_VERSION ${CMAKE_MATCH_1})
else()
    set(WHISPER_VERSION "unknown")
endif()

message(STATUS "Building Medical Companion with Whisper version: ${WHISPER_VERSION}")

# Include directories
include_directories(
    ${WHISPER_DIR}
    ${WHISPER_DIR}/src
    ${WHISPER_DIR}/include
    ${WHISPER_DIR}/ggml/include
    ${WHISPER_DIR}/ggml/src
    ${WHISPER_DIR}/ggml/src/ggml-cpu
    ${CMAKE_SOURCE_DIR}/whisper
    ${CMAKE_SOURCE_DIR}/native_bridge
)

# Find required libraries
find_library(LOG_LIB log)

# Add ggml as subdirectory (simpler than FetchContent, avoids long paths)
set(GGML_BUILD_TESTS OFF CACHE BOOL "" FORCE)
set(GGML_BUILD_EXAMPLES OFF CACHE BOOL "" FORCE)
add_subdirectory(${WHISPER_DIR}/ggml ${CMAKE_BINARY_DIR}/ggml)

# Collect source files
set(WHISPER_SOURCES
    ${WHISPER_DIR}/src/whisper.cpp
    ${CMAKE_SOURCE_DIR}/native_bridge/whisper_jni.cpp
)

# Build the main whisper library
add_library(whisper SHARED ${WHISPER_SOURCES})

target_compile_definitions(whisper PUBLIC GGML_USE_CPU)
target_compile_definitions(whisper PRIVATE WHISPER_VERSION="${WHISPER_VERSION}")

target_include_directories(whisper PRIVATE
    ${WHISPER_DIR}
    ${WHISPER_DIR}/src
    ${WHISPER_DIR}/include
    ${WHISPER_DIR}/ggml/include
    ${WHISPER_DIR}/ggml/src
    ${WHISPER_DIR}/ggml/src/ggml-cpu
)

# Release optimizations
if(NOT CMAKE_BUILD_TYPE STREQUAL "Debug")
    target_compile_options(whisper PRIVATE -O3)
    target_compile_options(whisper PRIVATE -fvisibility=hidden -fvisibility-inlines-hidden)
    target_compile_options(whisper PRIVATE -ffunction-sections -fdata-sections)
    target_link_options(whisper PRIVATE -Wl,--gc-sections)
    target_link_options(whisper PRIVATE -Wl,--exclude-libs,ALL)
    target_link_options(whisper PRIVATE -flto)
endif()

target_link_libraries(whisper ${LOG_LIB} android ggml)

# Architecture-specific optimized variants
if(${ANDROID_ABI} STREQUAL "arm64-v8a")
    # ARM64 with FP16 support
    add_library(whisper_v8fp16_va SHARED ${WHISPER_SOURCES})
    target_compile_definitions(whisper_v8fp16_va PUBLIC GGML_USE_CPU)
    target_compile_definitions(whisper_v8fp16_va PRIVATE WHISPER_VERSION="${WHISPER_VERSION}")
    target_compile_options(whisper_v8fp16_va PRIVATE -march=armv8.2-a+fp16)
    target_include_directories(whisper_v8fp16_va PRIVATE
        ${WHISPER_DIR}
        ${WHISPER_DIR}/src
        ${WHISPER_DIR}/include
        ${WHISPER_DIR}/ggml/include
        ${WHISPER_DIR}/ggml/src
        ${WHISPER_DIR}/ggml/src/ggml-cpu
    )
    if(NOT CMAKE_BUILD_TYPE STREQUAL "Debug")
        target_compile_options(whisper_v8fp16_va PRIVATE -O3 -fvisibility=hidden -ffunction-sections -fdata-sections)
        target_link_options(whisper_v8fp16_va PRIVATE -Wl,--gc-sections -Wl,--exclude-libs,ALL -flto)
    endif()
    target_link_libraries(whisper_v8fp16_va ${LOG_LIB} android ggml)

elseif(${ANDROID_ABI} STREQUAL "armeabi-v7a")
    # ARM32 with VFPv4
    add_library(whisper_vfpv4 SHARED ${WHISPER_SOURCES})
    target_compile_definitions(whisper_vfpv4 PUBLIC GGML_USE_CPU)
    target_compile_definitions(whisper_vfpv4 PRIVATE WHISPER_VERSION="${WHISPER_VERSION}")
    target_compile_options(whisper_vfpv4 PRIVATE -mfpu=neon-vfpv4)
    target_include_directories(whisper_vfpv4 PRIVATE
        ${WHISPER_DIR}
        ${WHISPER_DIR}/src
        ${WHISPER_DIR}/include
        ${WHISPER_DIR}/ggml/include
        ${WHISPER_DIR}/ggml/src
        ${WHISPER_DIR}/ggml/src/ggml-cpu
    )
    if(NOT CMAKE_BUILD_TYPE STREQUAL "Debug")
        target_compile_options(whisper_vfpv4 PRIVATE -O3 -fvisibility=hidden -ffunction-sections -fdata-sections)
        target_link_options(whisper_vfpv4 PRIVATE -Wl,--gc-sections -Wl,--exclude-libs,ALL -flto)
    endif()
    target_link_libraries(whisper_vfpv4 ${LOG_LIB} android ggml)
endif()
