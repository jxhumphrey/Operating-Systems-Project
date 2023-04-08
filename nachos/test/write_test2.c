#include "stdio.h"
#include "syscall.h"

int main() {
    int fd = creat("write_test.bin");
    if (fd == -1) {
        printf("Failed to create write_test.bin\n");
        exit(1);
    }
    
    int written = write(fd_open, "Hello\n", 1);
    if (written == -1) {
        printf("Failed to write\n");
        exit(1);
    }
}


