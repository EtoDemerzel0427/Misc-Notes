# The C programming language

I've learnt about C programming in the first year of my university, however at that time I know nothing about it but the basic language usages. Here in CPSC 223, Aspnes suprisingly makes the C programming language an approach to introduce the whole computer system and the compile principles.

## History

C programming language was developed in Bell Lab as the system language for **Unix**, based on BCPL and B. Limited by the hardware condition, its priority is ___using as few resources as possible___.

Four major version of C:

* The original **K&R C**(1978)
* **ANSI C**(1988, also known as C89 I think)
* **C99** : added some features from C++ and many new features for high-performance numerical computing
* **C11**：relaxed some of the requirements of C99 that most compilers hadn't bothered implementing and added a few extra features

What you have to know about C99 and C11 is:

> In particular, Microsoft pretty much gave up on adding any features after ANSI C, and support for C99 and C11 is spotty in `gcc` and `clang`, the two dominant open source C compilers. So if you want to write portable C code, it is safest to limit yourself to features in ANSI C.

By using `gcc --std=c99` or `c99`,  you can compile with C99 support; Straight `gcc` will give you GNU's only dialect of C, basically ANSI C with some extras. 

Safest way: `gcc -ansi -pedantic` expects straight ANSI C and will complain about any extensions.



## Structure of  a C program

A tiny example of a C program(`sumRange.c`) could be found in the lecture notes. 

In that example, `main` called some library functions like `printf`. These functions must all be declared before being used, and the **include files** contain declaration of them that contain enough information about their return types and arguments that **the compiler knows how to generate machine code to call them**. 

The **C preprocessor** will pastes in the contents of any file specified by the `#include` command and strips out any comments and does some other tricks that allow you to muck with the source code before the actual compiler sees it (Macros). See the output of the preprocessor by using `-E`: `c99 -E sumRange.c`.

 After runing the preprocessor, the compiler generates **assembly language** code. It is a *human-readable description of the ultimate machine code for your target CPU*. See its output by using the `-S` option(will create a `sumRange.s`.

Then the **assembler** translates the assembly language(`sumRange.s`) into machine code(if we are not compiling a single program all at ones will be stored in `sumRange.o`). 

The missing parts are the addresses of each function and global variables are generally left unspecified, so that they can be moved around to make room for other functions and variables coming from other files and from system libraries. The job of stitching all of these pieces together, putting everything in the right place, filling in any placeholder addresses, and generating the **executable file** `sumRange` that we can actually run is given to the **linker** `ld`.

The whole process:

```
sumRange.c (source code)
   |
   v
[preprocessor (cpp)]
   |
   v
preprocessed version of sumRange.c
   |
   v
[compiler (gcc)]
   |
   v
sumRange.s (assembly code)
   |
   v
[assembler (as)]
   |
   v
sumRange.o (machine code)
   |
   v
[linker (ld)] <- system library (glibc.a)
   |
   v
sumRange (executable)
```



## Numeric data type

The **address space** of a process might be much larger than the space of a RAM, so a **memory mapper** built in to the CPU will translate the large addresses to smaller ones corresponding to actual RAM locations.In some cases, regions of memory that have not been used in a while will be **swapped out** to disk, leaving more RAM free for other parts of the process (or other processes)(**virtual memory**).

Note that:

> 1. Many compilers also support a `long long` type that is usually twice the length of a long (e.g. 64 bits on i386 machines). This type was not officially added to the C standard prior to C99, so it may or may not be available if you insist on following the ANSI specification strictly.
> 2. There is a slight gotcha for character processing with input function like `getchar` and `getc`. These return the special value `EOF` (defined in `stdio.h` to be −1) to indicate end of file. But 255, which represents `'ÿ'` in the ISO Latin-1 alphabet and in Unicode and which may also appear quite often in binary files, will map to −1 if you put it in a character. So you should store the output of these functions in an `int` if you need to test for end of file. After you have done this test, it's OK to store a non-end-of-file character in a `char`.

```c
   /* right */
    int c;

    while((c = getchar()) != EOF) {
        putchar(c);
    }
    /* WRONG */
    char c;

    while((c = getchar()) != EOF) {  /* <- DON'T DO THIS! */
        putchar(c);
    }
```

