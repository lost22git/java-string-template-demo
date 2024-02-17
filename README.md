# Some string-template demo

[JEP 465: String Templates](https://openjdk.org/jeps/465)

## string_template_RUNSHELL.java

### Usage:

```java
try {
    switch(RUNSHELL."java --help") {
        case RunShellResult.Ok(var stdout) -> out.println(stdout);
        case RunShellResult.Err(var exitCode, var stdout, var stderr) -> {
            out.println(exitCode);
            out.println(stdout);
            out.println(stderr);
        };
    }
} catch (IOException e) {
    e.printStackTrace();
}
```

```java
try {
    switch(RUNSHELL."java -x 2>&1") {
        case RunShellResult.Ok(var stdout) -> out.println(stdout);
        case RunShellResult.Err(var exitCode, var stdout, var stderr) -> {
            out.println(exitCode);
            out.println(stdout);
            out.println(stderr);
        };
    }
} catch (IOException e) {
    e.printStackTrace();
}
```

### Run Testing

```shell
jbang run string_template_RUNSHELL.java
``` 
