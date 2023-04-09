#include "stdio.h"
#include "syscall.h"

int main() {
    int fd = creat("write_test.bin");
    if (fd == -1) {
        printf("Failed to create write_test.bin\n");
        exit(1);
    }

    //Should work
    int written = write(fd, "Hello\n", 6);
    if (written == -1) {
        printf("Failed to write\n");
        exit(1);
    }

    //Does not work
    written = write(200, "Hello\n", 6);
    if (written == -1) {
        printf("Failed to write\n");
        exit(1);
    } else {
        printf("Write does not work properly\n");
        exit(1);
    }
}
