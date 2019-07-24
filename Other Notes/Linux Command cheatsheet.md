# Linux Command cheatsheet

[TOC]

## About help docs

brief description:

```shell
$whatis command
```

regular expression:

```shell
$whatis -w "loca*"
```

detailed info:

```shell
$info command
```

use `man` to check the doc

```shell
$man command
```

There are 9 categories in `man` docs,  so we can use `whatis` to determine which category we want:

```shell
$whatis printf
```

The output is:

```
printf               (1)  - format and print data
printf               (1p)  - write formatted output
printf               (3)  - formatted output conversion
printf               (3p)  - print formatted output
printf [builtins]    (1)  - bash built-in commands, see bash(1)
```

see the binary file path:

```shell
$which command
```

see the search path:

```shell
$whereis command
```



## file and directory management

### create or delete

* create: `mkdir`

* delete: `rm`

* delete non-empty directory: `rm -rf`

* delete logs: `rm *log`

* move： `mv`

* copy: `cp`

* check the number of files in the current directory:

  ```shell
  $find ./ | wc -l
  ```

* copy a directory:

  ```sh
  $cp -r source_dir dest_dir
  ```
### switch
* switch directory: `cd some_dir`

* switch to the previous directory: `cd -`

* switch to home: `cd` or `cd ~`

* show current: `pwd`

### show list
* show file list in current directory: `ls`
* show list in time order: `ls -lrt`
* show list with id: `ls | cat -n`

### find/locate

find file or directory:

```shell
$find ./ -name "core*" | xargs file
```

find `obj` files in current directory:

```shell
$find ./ -name "*.o"
```

find `obj` files and delete them in current directory:

```shell
$find ./ -name "*.o" -exec rm {} \;
```

`find` is a real-time search command, for faster finding, use `locate`, which builds a database for the file system. For updates in files, do:

```shell
$updatedb
```

to update the database.

find paths with `string`:

```shell
$locate string
```

###  view content

command list:

`cat`/`vi`/`head`/`tail`/`more`

* show with line number:

  ```shell
  $cat -n
  ```

* show in pages:

  ```shell
  $ls -al | more
  ```

* show only the first 10 lines:

  ```shell
  $head - 10 **
  ```

* show the first line:

  ```shell
  $head -1 filename
  ```

* show the last 5 lines:

  ```shell
  $tail -5 filename
  ```

* show diff:

  ```shell
  $diff file1 file2
  ```

