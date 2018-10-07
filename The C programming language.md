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



## Structure of a C program

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



## Overflow and the C standard

We have assumed that overflow implicitly applies a (mode $2^b$) operation, but as of the C11 standard, this is defined behavior **only for unsigned integer types**.

Undefined behavior is often exploited by compilers to speed up compiled code by omitting otherwise necessary instructions to force a particular outcome. This is especially true if you turn on the optimizer using the `-0` flag.



## C99 fixed-width types

`stdint.h` defines integer types with known size independent of machine architecture.

e.g.:

`int8_t`： a signed type that holds exactly 8 bits (instead of `signed char`)

`uint64_t`: a 64-bit unsigned integer type (instead of `long long`)

Also there are `int_least16_t` `int_fast16_t`.

Advantage: if someone port your code to a new architecture, `stdint.h` should choose the right type automatically

disadvantage: not universally available on all C compilers. And the built-in routines for printing and parsing integers and the mechanisms for specifying the size of an integer constant are not adapted to deal with them.

For the latter problem, the larger `inttype.h` can help you solve this. See the lecture notes for details.



## `size_t` and `ptrdiff_t`

These two are provided in `stddef.h` to represent the return type of the `sizeof` operation and pointer subtraction. 

The place where you will most often see `size_t` is as an argument to `malloc`, where it gives the number of bytes to allocate.

`stdlib.h` includes `stddef.h`.



## integer constants

* in usual decimal notations.
* in **octal** (base 8), when leading digit is 0.(octal is still conventional for representing Unix file permissions).
* in **hexadecimal** (base 16), when prefixed with `0x`.
* using a **character constant**, which is a single ASCII character or an **escape sequence** inside single quotes.



You can also insist an integer constant is unsigned or long by putting a `u` or `l` after it. For `long long` constants, use `ll`.It is also permitted to write the `l` as `L` to avoid confusion if the `l` looks too much like a `1`.

e.g:

| `'a'`          | `int`                                   |
| -------------- | --------------------------------------- |
| `97`           | `int`                                   |
| `97u`          | `unsigned int`                          |
| `0xbea00d1ful` | `unsigned long`, written in hexadecimal |
| `0777s`        | `short`, written in octal               |

There is no way to write a binary integer directly in C. 



## Naming constants

The traditional approach is to use the C preprocessor.

e.g:

To define `EOF`, the file `/usr/include/stdio.h` includes the text:

```c
#define EOF (-1)
```

The parentheses around -1 are customary to ensure that -1 gets treated as a separate constant.

**Like `typedef`s, `#define`s that are intended to be globally visible are best done in header files. **in large programs you will want to `#include` them in many source files. The usual convention is to write `#define`d names in all-caps to remind the user that they are macros and not real variables.



## Integer operators

Division of two integers: C99 standard specified that integer division always removes the fractional part.

| `x`  | `y`  | expression | value |
| ---- | ---- | ---------- | ----- |
| 0011 | 0101 | `x&y`      | 0001  |
| 0011 | 0101 | `x|y`      | 0111  |
| 0011 | 0101 | `x^y`      | 0110  |
| 0011 | 0101 | `~x`       | 1100  |

These are mostly used for manipulating individual bits or small groups of bits inside larger words, as in the expression `x & 0x0f`, which strips off the bottom four bits stored in `x`.

unsigned char x:

| `x`        | `y`  | `x<<y`     | `x>>y`     |
| ---------- | ---- | ---------- | ---------- |
| `00000001` | `1`  | `00000010` | `00000000` |
| `11111111` | `3`  | `11111000` | `00011111` |

signed char x:

| `x`        | `y`  | `x<<y`     | `x>>y`     |
| ---------- | ---- | ---------- | ---------- |
| `00000001` | `1`  | `00000010` | `00000000` |
| `11111111` | `3`  | `11111000` | `11111111` |

if `y` is negative, the behavior of shift operations is undefined.



## Converting to/from string

from: `atoi`(to int) or `atol`(to long) declared in `stdlib.h`; C99 also provides `atoll`. Returning 0 is the only way to signal an error for these functions.

to: `sprintf`.

