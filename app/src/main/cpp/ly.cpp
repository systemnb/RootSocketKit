// ly.cpp
#include <sys/socket.h>
#include <sys/un.h>
#include <unistd.h>
#include <jni.h>
#include <cstdint>
#include "include/MemoryReaderWriter38.h"

#define SOCKET_PATH "/dev/socket/ly_service"

enum Command {
    CMD_CONNECT,
    CMD_OPEN_PROCESS,
    CMD_READ_MEMORY,
    CMD_WRITE_MEMORY,
    CMD_VIRTUAL_QUERY_EX,
    CMD_GET_PROCESS_PHYSICAL_MEMORY_SIZE,
    CMD_GET_PROCESS_CMD_LINE,
    CMD_SET_PROCESS_ROOT,
    CMD_CLOSE_HANDLE,
    CMD_GET_PID_LIST,
    CMD_EXIT
};

struct Request {
    uint64_t cmd;
    uint64_t pid;
    uint64_t handle;
    uint64_t address;
    uint64_t size;
    bool force;
    char data[1024];
    uint64_t data_len;
};

struct Response {
    uint64_t status;
    uint64_t data;
    char data_buf[1024];
    uint64_t data_len;
};

int connectToService() {
    int sock = socket(AF_UNIX, SOCK_STREAM, 0);
    if (sock < 0) return -1;

    sockaddr_un addr;
    memset(&addr, 0, sizeof(addr));
    addr.sun_family = AF_UNIX;
    strncpy(addr.sun_path, SOCKET_PATH, sizeof(addr.sun_path) - 1);

    if (connect(sock, reinterpret_cast<sockaddr*>(&addr), sizeof(addr)) < 0) {
        close(sock);
        return -1;
    }
    return sock;
}

bool send_request(int sock, Request& req, Response& res) {
    if (write(sock, &req, sizeof(req)) != sizeof(req)) return false;
    if (read(sock, &res, sizeof(res)) != sizeof(res)) return false;
    return true;
}

extern "C" JNIEXPORT jobject JNICALL
Java_com_ly_service_Native_00024Companion_OpenProcess(JNIEnv *env, jobject thiz, jint pid) {
    int sock = connectToService();
    if (sock < 0) {
        jclass responseClass = env->FindClass("com/ly/type/Response");
        jmethodID constructor = env->GetMethodID(responseClass, "<init>", "()V");
        jobject responseObj = env->NewObject(responseClass, constructor);
        jfieldID statusField = env->GetFieldID(responseClass, "status", "J");
        env->SetLongField(responseObj, statusField, static_cast<jlong>(-2));
        return responseObj;
    }

    Request req{};
    req.cmd = CMD_OPEN_PROCESS;
    req.pid = static_cast<uint64_t>(pid);

    Response res{};
    if (!send_request(sock, req, res)) {
        close(sock);
        jclass responseClass = env->FindClass("com/ly/type/Response");
        jmethodID constructor = env->GetMethodID(responseClass, "<init>", "()V");
        jobject responseObj = env->NewObject(responseClass, constructor);
        jfieldID statusField = env->GetFieldID(responseClass, "status", "J");
        env->SetLongField(responseObj, statusField, static_cast<jlong>(-2));
        return responseObj;
    }
    close(sock);

    jclass responseClass = env->FindClass("com/ly/type/Response");
    jmethodID constructor = env->GetMethodID(responseClass, "<init>", "()V");
    jobject responseObj = env->NewObject(responseClass, constructor);

    jfieldID statusField = env->GetFieldID(responseClass, "status", "J");
    jfieldID dataField = env->GetFieldID(responseClass, "data", "J");
    jfieldID dataBufField = env->GetFieldID(responseClass, "data_buf", "[B");
    jfieldID dataLenField = env->GetFieldID(responseClass, "data_len", "J");

    env->SetLongField(responseObj, statusField, static_cast<jlong>(res.status));
    env->SetLongField(responseObj, dataField, static_cast<jlong>(res.data));
    env->SetLongField(responseObj, dataLenField, static_cast<jlong>(res.data_len));

    jbyteArray byteArray = env->NewByteArray(static_cast<jsize>(res.data_len));
    env->SetByteArrayRegion(byteArray, 0, static_cast<jsize>(res.data_len),
                            reinterpret_cast<jbyte*>(res.data_buf));
    env->SetObjectField(responseObj, dataBufField, byteArray);

    return responseObj;
}
extern "C"
JNIEXPORT void JNICALL
Java_com_ly_service_Native_00024Companion_Exit(JNIEnv *env, jobject thiz) {
    int sock = connectToService();
    if (sock < 0) return;

    Request req{};
    req.cmd = CMD_EXIT;

    Response res{};
    if (!send_request(sock, req, res)) {
        close(sock);
        return;
    }
    close(sock);
}
extern "C"
JNIEXPORT jobject JNICALL
Java_com_ly_service_Native_00024Companion_ReadMemory(JNIEnv *env, jobject thiz, jlong h_process,
                                                     jlong address, jlong size, jboolean is_force) {
    int sock = connectToService();
    if (sock < 0) {
        jclass responseClass = env->FindClass("com/ly/type/Response");
        jmethodID constructor = env->GetMethodID(responseClass, "<init>", "()V");
        jobject responseObj = env->NewObject(responseClass, constructor);
        jfieldID statusField = env->GetFieldID(responseClass, "status", "J");
        env->SetLongField(responseObj, statusField, static_cast<jlong>(-2));
        return responseObj;
    }

    Request req{};
    req.cmd = CMD_READ_MEMORY;
    req.handle = h_process;
    req.address = address;
    req.size = size;
    req.force = is_force;

    Response res{};
    if (!send_request(sock, req, res)) {
        close(sock);
        jclass responseClass = env->FindClass("com/ly/type/Response");
        jmethodID constructor = env->GetMethodID(responseClass, "<init>", "()V");
        jobject responseObj = env->NewObject(responseClass, constructor);
        jfieldID statusField = env->GetFieldID(responseClass, "status", "J");
        env->SetLongField(responseObj, statusField, static_cast<jlong>(-2));
        return responseObj;
    }
    close(sock);

    jclass responseClass = env->FindClass("com/ly/type/Response");
    jmethodID constructor = env->GetMethodID(responseClass, "<init>", "()V");
    jobject responseObj = env->NewObject(responseClass, constructor);

    jfieldID statusField = env->GetFieldID(responseClass, "status", "J");
    jfieldID dataField = env->GetFieldID(responseClass, "data", "J");
    jfieldID dataBufField = env->GetFieldID(responseClass, "data_buf", "[B");
    jfieldID dataLenField = env->GetFieldID(responseClass, "data_len", "J");

    env->SetLongField(responseObj, statusField, static_cast<jlong>(res.status));
    env->SetLongField(responseObj, dataField, static_cast<jlong>(res.data));
    env->SetLongField(responseObj, dataLenField, static_cast<jlong>(res.data_len));

    jbyteArray byteArray = env->NewByteArray(static_cast<jsize>(res.data_len));
    if (byteArray) {
        env->SetByteArrayRegion(
                byteArray,
                0,
                static_cast<jsize>(res.data_len),
                reinterpret_cast<const jbyte*>(res.data_buf)
        );
    } else {
        // 处理内存分配失败
        res.status = -15;
    }
    if (byteArray)
        env->SetObjectField(responseObj, dataBufField, byteArray);
    return responseObj;
}