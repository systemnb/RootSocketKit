
# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html.
# For more examples on how to use CMake, see https://github.com/android/ndk-samples.

# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name. The project name can be accessed via ${ PROJECT_NAME},
# Since this is the top level CMakeLists.txt, the project name is also accessible
# with ${CMAKE_PROJECT_NAME} (both CMake variables are in-sync within the top level
# build script scope).
project("ly")

# 创建并命名一个库，将其设置为 STATIC
# 或 SHARED，并提供其源代码的相对路径。
# 您可以定义多个库，CMake 会为您构建它们。
# Gradle 会自动将共享库与您的 APK 打包。
#
# 在这个顶级CMakeLists.txt中，${CMAKE_PROJECT_NAME} 用于定义
# 目标库名称;在子模块的CMakeLists.txt中，${PROJECT_NAME}
# 是出于相同目的的首选。
#
# 为了从 Java/Kotlin 将库加载到您的应用程序中，您必须调用
# System.loadLibrary（） 并传递此处定义的库的名称;
# 对于 GameActivity/NativeActivity 派生应用程序，相同的库名称必须是
# 用于AndroidManifest.xml文件。

add_library(${CMAKE_PROJECT_NAME} SHARED
    # List C/C++ source files with relative paths to this CMakeLists.txt.
        ly.cpp)

# 指定 CMake 应链接到目标库的库。你
# 可以链接来自不同来源的库，例如在此中定义的库
# 构建脚本、预构建的第三方库或 Android 系统库。
target_link_libraries(${CMAKE_PROJECT_NAME}
    # List libraries link to the target library
    android
    log)

add_executable(ly_service service.cpp)

set_target_properties(ly_service PROPERTIES OUTPUT_NAME "ly_service")

target_link_libraries(ly_service
        log)