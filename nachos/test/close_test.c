#include "stdio.h"
#include "syscall.h"
#include "stdlib.h"
#include "fcntl.h"

int main() {
    int fd = creat("helloWorld.bin");
    if (fd == -1) {
        printf("Failed to create helloWorld.bin\n");
        exit(1);
    }

    int fd_close = close(fd);
    if (fd_close == -1) {
        printf("Failed to close helloWorld.bin\n");
        exit(1);
    }
}
