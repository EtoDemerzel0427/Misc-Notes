# Linux Basics notes

## C compiler

Assume we have a C program `count.c`  as below:

```c
#include <stdio.h>

/* print the numbers from 1 to 10 */

int
main(int argc, char **argv)
{
    int i;

    puts("Now I will count from 1 to 10");
    for(i = 1; i <= 10; i++) {
        printf("%d\n", i);
    }

    return 0;
}
```

We compile and run it:

```shell
$ c99 -g3 -o count count.c
$ ./count
Now I will count from 1 to 10
1
2
3
4
5
6
7
8
9
10
$
```

The command calls `gcc` as `c99`(or `gcc -std=c99`) with argument `-g3`(enable maximum debugging info), `-o` (specify executable file name, otherwise defaults to `a.out`)，and `count.c`(the source file to compile).

**This tells `gcc` that we should compile `count.c` to `count` in C99 mode with maximum debugging info included in the executable file.**

By default gcc does not check **everything** that might be wrong with your program, but if you give a few extra arguments, it will warn you about many potential problems:

```shell
c99 -g3 -Wall -pedantic -o foo foo.c
```



## Unix command-line programs

| command   | usage                                       | More info                                                    |
| --------- | ------------------------------------------- | ------------------------------------------------------------ |
| **man**   | show the online documentation               | `man -k string`                                              |
| **ls**    | list all the files in the current directory | `ls some/other/dir`：list files in other dir;`ls -l` ： long output format |
| **mkdir** | create new directory                        | None                                                         |
| **rmdir** | delete a directory                          | None                                                         |
| **pwd**   | print working directory                     | None                                                         |
| **mv**    | rename a file                               | None                                                         |
| **cp**    | make a copy of a file                       | `cp old-name new-name`                                       |
| **rm**    | delete a file                               | None                                                         |
| **chmod** | change the permission                       | `chmod 644/600/755/700 file` refer to different modes        |



## Vim commands

### Normal mode

| commands      | usage                                                        |
| ------------- | ------------------------------------------------------------ |
| `:h`          | get help                                                     |
| `i`           | enter insert mode                                            |
| `u`           | undo                                                         |
| `:w`          | write the current file to disk;`:w filename` write it to `filename` |
| `:e filename` | edit a different file                                        |
| `:q`          | quit; throw away unwritten files:`:wa`/`:q!`; write:`:x`/`:wq` |
| `h,j,k,l`     | move cursor left/down/up/right                               |
| `x`           | delete current character                                     |
| `D`           | delete to end of line                                        |
| `dd`          | delete all the current line. `5dd` deletes the next 5 lines;see also `d$`/`dj`/`d%`/`dG` |
| `yy`          | like *copy* ; `5yy`, `y$`, `yj`, `y%`                        |
| `p`           | like *paste*                                                 |
| `<<`/`>>`     | outdent/indent                                               |
| `:make`       | run `make` in the current directory                          |
| `:!`          | run a command like `:! gdb`                                  |

## Insert mode

**control-P and control-N**： completion commands

**control-O and control-I**：jump to the last cursor position

**ESC**: get out of insert mode



## Make

Make is a "rull-based expert system" that figures out how to compile programs given a little bit of information about their components.  A typical Makefile could be found in the lecture notes.

