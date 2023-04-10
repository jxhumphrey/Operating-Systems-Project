#include "stdio.h"
#include "syscall.h"

char hello[] = "Hello!\n";

int main() {
    int fd = creat("helloWorld.bin");

    if (fd == -1) {
        printf("Failed to create helloWorld.bin\n");
        exit(1);
    }

    int written = write(fd, hello, sizeof(hello) - 1);
    if (written == -1) {
        printf("failed to write\n");
        exit(1);
    }   

    //Should fail: count cannot be smaller than 0
    char buf[128];
    int readRet = read(fd, buf, -1);
    if (readRet != sizeof(hello) - 1) {
        printf("Failed to read the correct amount from file\n");
        exit(1);
    }
}

