#include "syscall.h"
#include "stdio.h"

int main(){
	char *argv[] = {""};
	exec("halt.coff", 1, argv);
	char *argv2[] = {"arg1", "arg2"};
	exec("echo.coff", 2, argv);
}