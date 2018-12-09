# Makefile Tutorial

Reference: 

1. https://www.tutorialspoint.com/makefile/
2. https://opensource.com/article/18/8/what-how-makefile

Compiling the source code files can be tiring, especially when you have to include ___several___ source files and type the compiling command every time you need to compile. Makefiles are the solution to simplify this task.

The situation is:

1. Re-compiling large programs takes longer time than short programs.
2. Usually, we only work on a small section of the program and much of the remaining program is unchanged.

And that's why we need Makefile.

## Basic examples

### target, prerequisites and recipe

1. create an empty directory `myproject` containing a file `Makefile` with this content:

   ```shell
   say_hello:
   	echo "Hello world"
   ```

2. Then run the file by typing `make` in `myproject`， the output will be:

   ```shell
   $make
   echo "Hello world"
   Hello world
   ```



In this example, `say_hello` behaves like a function name, and this is called the *target*, the *prerequisites* or *dependencies* follow the target, however we don't include them in this example for simplicity.

The command `echo "Hello world"` is called the *recipe*. **The recipe uses prerequisites to make a target**, and they three together make a rule.

Syntax:

```shell
target: prerequisites
<TAB> recipe
```

As an example, a target might be a **binary file** that depends on prerequisites(**source files**). On the other hand, a prerequisite can also be a **target** depends on other dependencies:

```shell
final_target: sub_target final_target.c
        Recipe_to_create_final_target

sub_target: sub_target.c
        Recipe_to_create_sub_target
```

The target is not necessarily a file; it could just be a name for the recipe, and in these cases they are called "phony targets". 

Go back to the example, to avoid the entire echo command being displayed, start `echo` with `@`:

```shell
say_hello:
        @echo "Hello World"
```



### multiple targets

Add more phony targets: `generate` and `clean` to the `Makefile`:

```shell
say_hello:
        @echo "Hello World"

generate:
        @echo "Creating empty text files..."
        touch file-{1..10}.txt

clean:
        @echo "Cleaning up..."
        rm *.txt
```

Run `make`, only the first `say_hello` will be executed because only the first target is the default target. And this behavior could be overridden by using a special phony target called `.DEFAULT_GOAL`.

Include this at the beginning of our `makefile`:

```shell
.DEFAULT_GOAL := generate
```

This will run the target `generate` as the default.

However, this phony target can run only one target at a time, so most Makefiles include `all` as a target that can call as many targets as needed.

```shell
.PHONY: all say_hello generate clean

all: say_hello generate

say_hello:
        @echo "Hello World"

generate:
        @echo "Creating empty text files..."
        touch file-{1..10}.txt

clean:
        @echo "Cleaning up..."
        rm *.txt
```

Note the special phony target, `.PHONY`. We define all the targets **that are not files** using it. **`make` will run its recipe regardless of whether a file with that name exists or what its last modification time is**.

After this modification, the `make` command should call both `say_hello` and `generate`.

>It is a good practice not to call `clean` in `all` or put it as the first target. `clean` should be called manually when cleaning is needed as a first argument to `make`:
>
>```shell
>$ make clean
>Cleaning up...
>rm *.txt
>```



## Advanced examples

> using variables and patterns.

### Variable

Define a variable in a Makefile: use `=` operator. For example assign the command `gcc` to a variable `CC`:

```shell
CC = gcc
```

It is used in a rule as below:

```shell
hello: hello.c
	${CC} hello,.c -o hello
```

Both `${CC}` and `$(CC)` are valid to call `gcc`. But when reassigning a variable to itself, it will cause a infinite loop. To avoid this, we can use the `:=` operator and by using this, we can have no problem running this:

```shell
CC := gcc
CC := ${CC}

all:
    @echo ${CC}
```



### Patterns and functions

```shell
# Usage:
# make        # compile all binary
# make clean  # remove ALL binaries and objects

.PHONY = all clean

CC = gcc                        # compiler to use

LINKERFLAG = -lm

SRCS := $(wildcard *.c)
BINS := $(SRCS:%.c=%)

all: ${BINS}

%: %.o
        @echo "Checking.."
        ${CC} ${LINKERFLAG} $< -o $@

%.o: %.c
        @echo "Creating object.."
        ${CC} -c $<

clean:
        @echo "Cleaning up..."
        rm -rvf *.o ${BINS}
```

Explanation:

* `SRCS := $(wildcard *.c)`:  all files with the `.c` extension will be stored in variable `SRCS`.

* `BINS := $(SRCS:%.c=%)`: if `SRC` has values '`foo.c bar.c`', BINS will have '`foo bar`'.

* Line `all: ${BINS}`: The phony target `all` calls values in`${BINS}` as individual targets.

* Rule:

  ```shell
  %: %.o
    @echo "Checking.."
    ${CC} ${LINKERFLAG} $&lt; -o $@
  ```

  Let's look at an example to understand this rule. Suppose `foo` is one of the values in `${BINS}`. Then `%` will match `foo`(`%` can match any target name). Below is the rule in its expanded form:

  ```shell
  foo: foo.o
    @echo "Checking.."
    gcc -lm foo.o -o foo
  ```

  As shown, `%` is replaced by `foo`. `$<` is replaced by `foo.o`. `$<` is patterned to match prerequisites and `$@` matches the target. This rule will be called for every value in `${BINS}`

* Rule:

  ```shell
  %.o: %.c
    @echo "Creating object.."
    ${CC} -c $&lt;
  ```

  Every prerequisite in the previous rule is considered a target for this rule. Below is the rule in its expanded form:

  ```shell
  foo.o: foo.c
    @echo "Creating object.."
    gcc -c foo.c
  ```

## Macros

Some special macros predefined:

* `$@` is the name of the file to be made.
* `$?` is the names of the changed dependents.
* `$<` is the name of the related file that caused the action
* `$*` the prefix shared by target and dependent files.

For example:

```shell
hello: main.cpp hello.cpp factorial.cpp
	$(CC) $(CFLAG) $? $(LDFLAGS) -o $@
```

Alternatively:

```shell
hello: main.cpp hello.cpp factorial.cpp
   $(CC) $(CFLAGS) $@.cpp $(LDFLAGS) -o $@
```

`$@` here represents `hello` and `$?` or `@.cpp` picks up all the changed source files.

