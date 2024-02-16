# Some string-template demo

## string_template_RUNSHELL.java

- something like [\`shell cmd\`](https://crystal-lang.org/reference/1.11/syntax_and_semantics/literals/command.html) in crystal-lang.
- `cmd.exe -c "shell cmd"` on windows, `sh -c "shell cmd"` on unix-like
- `inherit STDERR` and `pipe STDOUT`
- throws `IOException` when read IO or running thread is interrupted

### Usage:

```java
try {
    var helpString = RUNSHELL."java --help";
} catch (IOException e) {
    e.printStackTrace();
}
```

```java
try {
    var errorString = RUNSHELL."java -x 2>&1";
} catch (IOException e) {
    e.printStackTrace();
}
```

### Run Testing

```shell
jbang run string_template_RUNSHELL.java
``` 
