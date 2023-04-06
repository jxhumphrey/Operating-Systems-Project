#include "syscall.h"
#include "stdio.h"

int main(){
    char *argv2[] = {"arg1", "arg2"};
    exec("echo.coff", 2, argv);
    char *argv[] = {""};
    exec("halt.coff", 1, argv);
    join(1, 1);
    exit(1);
}