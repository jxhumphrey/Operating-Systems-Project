#include "syscall.h"
#include "stdio.h"

int main(){
    char *argv1[] = {"arg1", "arg2"};
    int processID1 = exec("echo.coff", 2, argv1);
    char *argv2[] = {""};
    int processID2 = exec("halt.coff", 1, argv2);
    int childStatus1;
    int childStatus2;
    if (join(processID1, &childStatus1) == -1) {
        printf("First join failed\n");
        exit(1);
    }
    if (join(processID2, &childStatus2) == -1) {
        printf("First join failed\n");
        exit(1);
    }
    printf("child exit code 1: %d, child exit code 2: %d\n", childStatus1, childStatus2);
}