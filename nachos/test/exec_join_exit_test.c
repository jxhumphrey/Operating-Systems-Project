#include "syscall.h"
#include "stdio.h"

int main(){
    char *argv2[] = {"arg1", "arg2"};
    int processID1 = exec("echo.coff", 2, argv);
    char *argv[] = {""};
    int processID2 = exec("halt.coff", 1, argv);
    join(processID1, 1);
    join(processID1, 1);
    std::cout << "exit code: " << exit(1);
}