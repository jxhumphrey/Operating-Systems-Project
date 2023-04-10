#include "stdio.h"
#include "syscall.h"

int main() {
    //Attempt to open file with file descriptor of 1 (does not exist)
    int fd_close = close(1);
    if (fd_close != -1) {
        printf("Error\n");
        exit(1);
    } else { //This block of code is supposed to execute
        printf("Cannot close a file that does not exist\n");
        exit(1);
    }
}

