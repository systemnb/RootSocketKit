// service.cpp
#include <cstdio>
#include <unistd.h>
#include <cstdlib>
#include <cstring>
#include <sys/socket.h>
#include <sys/un.h>
#include <pwd.h>
#include <cstdint>
#include "include/MemoryReaderWriter38.h"

#define SOCKET_PATH "/dev/socket/ly_service"
#define procNodeAuthKey "c2a2b5792edd296763fdfc72cff44380"

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

void handle_client(int client_fd) {
    CMemoryReaderWriter rwDriver;
    int result = rwDriver.ConnectDriver(procNodeAuthKey);
    if (result < 0) {
        Response response = {static_cast<uint64_t>(-1), 0, {0}, 0};
        send(client_fd, &response, sizeof(response), 0);
        close(client_fd);
        return;
    }

    while (true) {
        Request request = {};
        ssize_t bytes_received = recv(client_fd, &request, sizeof(request), 0);

        if (bytes_received <= 0) {
            break; // 客户端断开连接或出错
        }

        Response response = {0, 0, {0}, 0};
        BOOL res = FALSE;

        switch (request.cmd) {
            case CMD_CONNECT:
                response.status = 0;
                break;

            case CMD_OPEN_PROCESS:
                response.data = static_cast<uint64_t>(rwDriver.OpenProcess(request.pid));
                response.status = 0;
                break;

            case CMD_READ_MEMORY: {
                size_t bytesRead = 0;
                res = rwDriver.ReadProcessMemory(
                        request.handle,
                        request.address,
                        response.data_buf,
                        static_cast<size_t>(request.size),
                        &bytesRead,
                        request.force
                );
                response.data_len = static_cast<uint64_t>(bytesRead);
                response.status = res ? 0 : static_cast<uint64_t>(-3);
                break;
            }

            case CMD_WRITE_MEMORY: {
                size_t bytesWritten = 0;
                res = rwDriver.WriteProcessMemory(
                        request.handle,
                        request.address,
                        request.data,
                        static_cast<size_t>(request.size),
                        &bytesWritten,
                        request.force
                );
                response.data_len = static_cast<uint64_t>(bytesWritten);
                response.status = res ? 0 : static_cast<uint64_t>(-4);
                break;
            }

            case CMD_VIRTUAL_QUERY_EX: {
                std::vector<DRIVER_REGION_INFO> vMaps;
                BOOL b = rwDriver.VirtualQueryExFull(request.handle, request.force, vMaps);
                if (!b) {
                    response.status = static_cast<uint64_t>(-5);
                    break;
                }
                response.data_len = static_cast<uint64_t>(sizeof(DRIVER_REGION_INFO) * vMaps.size());
                if (response.data_len > sizeof(response.data_buf)) {
                    response.status = static_cast<uint64_t>(-6);
                    break;
                }
                memcpy(response.data_buf, vMaps.data(), static_cast<size_t>(response.data_len));
                response.status = 0;
                break;
            }

            case CMD_GET_PROCESS_PHYSICAL_MEMORY_SIZE: {
                uint64_t outRss = 0;
                BOOL b = rwDriver.GetProcessPhyMemSize(request.handle, outRss);
                if (!b) {
                    response.status = static_cast<uint64_t>(-7);
                    break;
                }
                response.data = outRss;
                response.status = 0;
                break;
            }

            case CMD_GET_PROCESS_CMD_LINE: {
                char cmdline[1024] = {0};
                BOOL b = rwDriver.GetProcessCmdline(request.handle, cmdline, sizeof(cmdline));
                if (!b) {
                    response.status = static_cast<uint64_t>(-8);
                    break;
                }
                response.data_len = strlen(cmdline) + 1;
                if (response.data_len > sizeof(response.data_buf)) {
                    response.status = static_cast<uint64_t>(-9);
                    break;
                }
                memcpy(response.data_buf, cmdline, static_cast<size_t>(response.data_len));
                response.status = 0;
                break;
            }

            case CMD_SET_PROCESS_ROOT: {
                BOOL b = rwDriver.SetProcessRoot(request.handle);
                response.status = b ? 0 : static_cast<uint64_t>(-10);
                break;
            }

            case CMD_CLOSE_HANDLE: {
                BOOL b = rwDriver.CloseHandle(request.handle);
                response.status = b ? 0 : static_cast<uint64_t>(-11);
                break;
            }

            case CMD_GET_PID_LIST: {
                std::vector<int> vPID;
                BOOL b = rwDriver.GetPidList(vPID);
                if (!b) {
                    response.status = static_cast<uint64_t>(-12);
                    break;
                }
                response.data_len = static_cast<uint64_t>(sizeof(int) * vPID.size());
                if (response.data_len > sizeof(response.data_buf)) {
                    response.status = static_cast<uint64_t>(-13);
                    break;
                }
                memcpy(response.data_buf, vPID.data(), static_cast<size_t>(response.data_len));
                response.status = 0;
                break;
            }
            case CMD_EXIT:
                exit(0);
                kill(getpid(), SIGKILL);
                break;
            default:
                response.status = static_cast<uint64_t>(-14);
                break;
        }

        if (send(client_fd, &response, sizeof(response), 0) <= 0) {
            break; // 发送失败，断开连接
        }
    }

    close(client_fd);
}

int main() {
    if (getuid() != 0) {
        exit(EXIT_FAILURE);
    }
    pid_t pid = fork();
    if (pid < 0) {
        exit(EXIT_FAILURE);
    }
    if (pid > 0) {
        exit(EXIT_SUCCESS);
    }
    setsid();
    pid = fork();
    if (pid > 0) {
        exit(EXIT_SUCCESS);
    }
    umask(0);
    chdir("/");
    for (int i = sysconf(_SC_OPEN_MAX); i >= 0; i--) {
        close(i);
    }
    int nullfd = open("/dev/null", O_RDWR);
    dup2(nullfd, STDIN_FILENO);
    dup2(nullfd, STDOUT_FILENO);
    dup2(nullfd, STDERR_FILENO);
    close(nullfd);
    prctl(PR_SET_NAME, "ly_service", 0, 0, 0);
    struct sigaction sa;
    sa.sa_handler = SIG_IGN;
    sigemptyset(&sa.sa_mask);
    sa.sa_flags = 0;
    sigaction(SIGCHLD, &sa, NULL);

    int server_fd = socket(AF_UNIX, SOCK_STREAM, 0);
    if (server_fd < 0) {
        exit(EXIT_FAILURE);
    }

    sockaddr_un addr;
    memset(&addr, 0, sizeof(addr));
    addr.sun_family = AF_UNIX;
    strncpy(addr.sun_path, SOCKET_PATH, sizeof(addr.sun_path) - 1);

    unlink(SOCKET_PATH);

    if (bind(server_fd, (sockaddr *) &addr, sizeof(addr)) < 0) {
        close(server_fd);
        exit(EXIT_FAILURE);
    }

    listen(server_fd, 5);
    chmod(SOCKET_PATH, 0777);

    while (true) {
        int client_fd = accept(server_fd, nullptr, nullptr);
        if (client_fd < 0) {
            continue;
        }
        pid_t pid = fork();
        if (pid < 0) {
            close(client_fd);
            continue;
        }
        if (pid == 0) {
            close(server_fd);
            handle_client(client_fd);
            close(client_fd);
            _exit(0);
        } else {
            close(client_fd);
        }
        close(client_fd);
    }

    close(server_fd);
    return 0;
}