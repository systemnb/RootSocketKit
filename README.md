### English Version README.md

# RootSocketKit - Android Root Privilege Communication Kit

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android-green.svg)](https://www.android.com)

**Efficient and universal Android root operations via Socket communication**

## Project Background

When performing root operations at the JNI layer in Android development, we often face the following issues:
- Traditional solutions like libsu only work in specific environments such as Magisk, with poor support for kernel-level root solutions like KernelSU and APatch
- In reinforced apps (e.g., 360 Reinforcement), libsu fails to bind services due to modified class loading mechanisms
- File communication is inefficient, while the app_process approach requires complex reflection and binding operations

RootSocketKit provides a novel solution:
🚀 **Inter-process communication via Unix Socket**, isolating root operations in a separate service
🔒 **Client requires no root permissions**, executing root operations by communicating with the server
⚡ **Efficient and stable** with measured communication latency under 5ms

## Technical Architecture

```mermaid
graph LR
A[Android App] -->|JNI Call| B[Client]
B -->|Unix Socket| C[Root Server]
C -->|Execute root operations| D[Kernel Driver]
D -->|Hardware operations| E[Hardware]
```

## Key Features

- **Broad Compatibility**: Supports Magisk, KernelSU, APatch and other root environments
- **Reinforcement-proof**: Bypasses class loading mechanisms, works in 360 Reinforcement scenarios
- **Efficient Communication**: Socket-based communication with low latency and high throughput
- **Concise Code**: Core C++ code requires only a few files, easy to integrate and maintain
- **Secure and Reliable**: Server runs as a daemon with auto-restart for stability

## Quick Start

### 1. Clone Repository
```bash
git clone https://github.com/yourname/RootSocketKit.git
```

### 2. Integrate into Project
Copy the following files to your Android project:
- `service.cpp`: Root server code
- `ly.cpp`: JNI client code

### 3. Modify CMakeLists.txt
Add the following to your CMakeLists.txt:
```cmake
add_library(ly SHARED ly.cpp service.cpp)

# Add other dependencies...

target_link_libraries(ly
    android
    log)
```

### 4. Initialize Service
Start the root service in Java/Kotlin code:
```kotlin
// Start root service
Runtime.getRuntime().exec("su -c /data/local/tmp/ly_service")
```

### 5. Usage Example
```kotlin
// Open process
val handle = Native.OpenProcess(pid)

// Read memory
val result = Native.ReadMemory(handle, address, size, true)
```

## Use Cases

- Game memory modification
- System-level file operations
- Kernel module interaction
- Deep device customization
- Privileged operations in reinforced environments

## License

This project is open-sourced under the [MIT License](LICENSE), free for personal and commercial use.
