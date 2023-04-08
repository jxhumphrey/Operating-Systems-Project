#include "stdio.h"
#include "syscall.h"

int main() {
    int fd = creat("helloWorld.bin");

    if (fd == -1) {
        printf("Failed to create helloWorld.bin\n");
        exit(1);
    }
    //Create and open 16 files, the max amount for this file system
    for(int i = 0; i++; i<17) {
        char temp[] = itoa(i)
        int fd = creat(temp);
        int fd_open = open(fd);
        if(fd_open == -1) {
            printf("Open syscall does not work correctly\n");
            exit(1);
        }
    }

    //Try to create a file
    int fd_creat_one_more = creat("newFile.bin");
    if(fd_creat_one_more == -1) {
            printf("Reached the limit to create new files!\n");
            exit(1);
        }
    
    
 
}
