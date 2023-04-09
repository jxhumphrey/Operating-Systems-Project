#include "stdio.h"
#include "syscall.h"

char text[] = "IT will be seen that this mere painstaking burrower and " \
"grub -worm of a poor devil of a Sub -Sub appears to have gone " \
"through the long Vaticans and street-stalls of the earth, pick-" \
"ing up whatever random allusions to whales he could anyways " \
"find in any book whatsoever, sacred or profane. Therefore " \
"you must not, in every case at least, take the higgledy-piggledy " \
"whale statements, however authentic, in these extracts, for " \
"veritable gospel cetology. Far from it. As touching the " \
"ancient authors generally, as well as the poets here appearing, " \
"these extracts are solely valuable or entertaining, as affording " \
"a glancing bird's-eye view of what has been promiscuously " \
"said, thought, fancied, and sung of Leviathan, by many " \
"nations and generations, including our own. ";

char textReadIn[sizeof(text)];

int main() {
    int fd = creat("test.bin");

    int i;
    for (i = 0; i < 32; i++) {
        int written = write(fd, text, sizeof(text) - 1);
        if (written != (sizeof(text) - 1)) {
            printf("Failed to write, only wrote: %d bytes\n", written);
            exit(1);
        }
        int readIn = read(fd, textReadIn, sizeof(text) - 1);
        if (readIn == -1) {
            printf("Failed to read\n", readIn);
            exit(1);
        }
    }
}
