**其他语言版本: [English](README.md), [中文](README_zh.md).**
# RootSocketKit - Android Root权限通信套件

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com)

**通过Socket通信实现高效、通用的Android Root权限操作**

## 项目背景

在Android开发中，当需要在JNI层执行需要root权限的操作时，我们常常面临以下问题：
- 传统的libsu方案只能在Magisk等特定环境下工作，对于KernelSU、APatch等内核级Root方案支持不足
- 在加固应用（如360加固）中，由于类加载机制被修改，libsu无法正常绑定服务
- 使用文件通信效率低下，而app_process方案需要复杂的反射和绑定操作

RootSocketKit提供了一种新颖的解决方案：
🚀 **通过Unix Socket进行进程间通信**，将需要root权限的操作放在独立服务中
🔒 **客户端无需root权限**，只需与服务端通信即可执行root操作
⚡ **高效稳定**，实测通信延迟小于5ms

## 技术架构

```mermaid
graph LR
A[Android应用] -->|JNI调用| B[客户端]
B -->|Unix Socket| C[Root服务端]
C -->|执行root操作| D[内核驱动]
D -->|操作硬件| E[硬件]
```

## 核心特性

- **广泛兼容**：支持Magisk、KernelSU、APatch等多种Root环境
- **加固无忧**：绕过类加载机制，可在360加固等环境中使用
- **高效通信**：基于Socket的通信机制，延迟低，吞吐量高
- **代码简洁**：核心C++代码仅需几个文件，易于集成和维护
- **安全可靠**：服务端以守护进程运行，自动重启，稳定可靠

## 快速开始

### 1. 克隆仓库
```bash
git clone https://github.com/yourname/RootSocketKit.git
```

### 2. 集成到项目
将以下文件复制到您的Android项目中：
- `service.cpp`：Root服务端代码
- `ly.cpp`：JNI客户端代码

### 3. 修改CMakeLists.txt
在您的CMakeLists.txt中添加以下内容：
```cmake
add_library(ly SHARED ly.cpp service.cpp)

# 添加其他依赖...

target_link_libraries(ly
    android
    log)
```

### 4. 初始化服务
在Java/Kotlin代码中启动Root服务：
```kotlin
// 启动Root服务
Runtime.getRuntime().exec("su -c /data/local/tmp/ly_service")
```

### 5. 调用示例
```kotlin
// 打开进程
val handle = Native.OpenProcess(pid)

// 读取内存
val result = Native.ReadMemory(handle, address, size, true)
```

## 应用场景

- 游戏内存修改
- 系统级文件操作
- 内核模块交互
- 设备深度定制
- 加固环境下的特权操作

## 许可协议

本项目基于 [MIT 许可证](LICENSE) 开源，欢迎自由使用于个人和商业项目。
