#include "stdio.h"
#include "syscall.h"

char* fileNames[] = {"0.bin", "1.bin", "2.bin", "3.bin", "4.bin", "5.bin", "6.bin", "7.bin", "8.bin", "9.bin",
"10.bin", "11.bin", "12.bin", "13.bin", "14.bin", "15.bin"};

int main() {
    int fd = creat("helloWorld.bin");

    if (fd == -1) {
        printf("Failed to create helloWorld.bin\n");
        exit(1);
    }
    //Create and open 16 files, the max amount for this file system
    int i;
    for(i = 0; i++; i<16) {
        char* temp = fileNames[i];
        int fd = creat(temp);
        if(fd == -1) {
            printf("Creat syscall does not work correctly\n");
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
