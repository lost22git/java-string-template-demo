# Some string-template demo

[JEP 465: String Templates](https://openjdk.org/jeps/465)

## string_template_RUNSHELL.java

### Usage:

```java
switch(RUNSHELL."java --help") {
    case RunShellResult.Ok(var stdout) -> out.println(stdout);
    case RunShellResult.Fail(var exitCode, var stdout, var stderr) -> {
        out.println(exitCode);
        out.println(stdout);
        out.println(stderr);
    };
    case RunShellResult.Err(var e) -> e.printStackTrace();
}
```

```java
switch(RUNSHELL."java -x 2>&1") {
    case RunShellResult.Ok(var stdout) -> out.println(stdout);
    case RunShellResult.Fail(var exitCode, var stdout, var stderr) -> {
        out.println(exitCode);
        out.println(stdout);
        out.println(stderr);
    };
    case RunShellResult.Err(var e) -> e.printStackTrace();
}
```

### Run Testing

```shell
jbang run string_template_RUNSHELL.java
``` 
