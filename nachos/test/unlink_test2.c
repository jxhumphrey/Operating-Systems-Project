#include "stdio.h"
#include "syscall.h"

int main() {
    int fd = creat("helloWorld.bin");

    if (fd == -1) {
        printf("Failed to create helloWorld.bin\n");
        exit(1);
    }
    
    int fd_unlink = unlink("helloWorld.bin");
    
    if(fd_unlink != 0) {
        printf("Unlink failed\n");
        exit(1);
    }
    
}


