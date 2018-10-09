# Debugging 

Use debugging tool `gdb` and memory error checker `valgrind`.

## Debugging in general

1. ___Know what your program is supposed to do.___
2. Detect when it doesn't.
3. Fix it.

A tempting mistake is to skip step 1.

## Assertions

In C programs, use `<assert.h>`. It provides you with the `assert` macro.

e.g：

```c
#include <assert.h>

int
main(int argc, char **argv)
{
    assert(2+2 == 5);
    return 0;
}
```

Compile and run:

```shell
$ gcc -o no no.c
$ ./no
no: no.c:6: main: Assertion `2+2 == 5' failed.
```



## gdb

`gdb` is the standard debugger on Linux. For OS X, you might also have better results using the standard OS X debugger `lldb`.  Also, most IDEs that support C include debugging tools themselves.

Suppose we have a program called `bogus.c`(see the lecture notes for more precise example) and we want to debug.

So first, make sure you include the flag `-g3` to tell the compiler to include debugging information, **this allows `gdb` to translate machine addresses back into identifiers and line numbers in the original programs for us**.

### gdb commands

**help**

​	get a description of gdb's command

**run**

 	run your programs. You can give it arguments.

**quit**

​	leave gdb.

**break**

​	set a breakpoint, which is a place where gdb will **automatically stop your program**. e.g: `-break somefunction` stops before executing the first line `somefunction`. `-break 117` stops before executing line number 117.

**list**

​	show part of your source file with line numbers. e.g:  `-list somefunc` lists all lines of 				`somefunc`, `-list 117-123` lists lines through 117 to 123.

**next**

​	execute the next line of the program.

**finish**

​	continue until you get out of the current procedure.

**cont**

​	continue until: 

  1. the end of the program

  2. a fatal error

  3. a breakpoint

     `cont 1000`(or any other numbers) will skip over that many breakpoints before stopping.

**print**

​	print the value of some expression

**display**

​	like `print`, but runs automatically every time the program stops.

**backtrace**

​	show all the function calls on the stack with arguments. abbr: `bt`. `bt full`: see local variables in each function.



## Valgrind

`valgrind` program can be used to detect some common errors in C programs that use pointers and dynamic storage allocation.

I will not write much about it here, check the details in the lecture notes.



## debugging output

Actually as for myself, it may be the most common debugging technique...But Aspnes tells us:

> A tempting but usually bad approach to debugging is to put lots of `printf` statements in your code to show what is going on. The problem with this compared to using `assert` is that there is no built-in test to see if the output is actually what you'd expect. The problem compared to `gdb` is that it's not flexible: you can't change your mind about what is getting printed out without editing the code. A third problem is that the output can be misleading: in particular, `printf` output is usually buffered, which means that **if your program dies suddenly there may be output still in the buffer that is never flushed to `stdout`. This can be very confusing, and can lead you to believe that your program fails earlier than it actually does**.

If we INSIST on debugging output：

1. Use `fprintf(stderr, ...)` instead. 
2. If you must output to `stdout`, put `fflush(stdout)` after any output operation you suspect is getting lost in the buffer.
3. Keep all arguments passed to `printf` as simple as possible and beware of faults in your debugging code itself.
4. **Wrap your debugging output in an `#ifdef` so you can turn it on and off easily**.

