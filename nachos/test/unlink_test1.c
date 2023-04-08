#include "stdio.h"
#include "syscall.h"


int main() {
    int fd = creat("helloWorld.bin");

    if (fd == -1) {
        printf("Failed to create helloWorld.bin\n");
        exit(1);
    }
    
    //Should work
    int fd_unlink_valid = unlink("helloWorld.bin");

    //Should not work - file is deleted
    int fd_unlink_invalid = unlink("helloWorld.bin");
    if(fd_unlink_invalid == -1) {
        printf("File not found to unlink\n");
        exit(1);
    }
}