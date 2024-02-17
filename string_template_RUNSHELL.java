///usr/bin/env jbang "$0" "$@" ; exit $?

import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.LoggingListener;

import static java.lang.System.out;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

//JAVA 21
//NATIVE_OPTIONS --no-fallback -H:+ReportExceptionStackTraces --enable-preview --enable-monitoring
//COMPILE_OPTIONS --enable-preview --release 21
//RUNTIME_OPTIONS --enable-preview -XX:+UseZGC -XX:NativeMemoryTracking=summary -XX:+HeapDumpOnOutOfMemoryError

//DEPS org.junit:junit-bom:5.10.1@pom
//DEPS org.junit.jupiter:junit-jupiter-api
//DEPS org.junit.jupiter:junit-jupiter-engine
//DEPS org.junit.platform:junit-platform-launcher

public class string_template_RUNSHELL {
  public static void main(final String... args) {
    final LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
        .selectors(selectClass(string_template_RUNSHELL.class))
        .build();
    final Launcher launcher = LauncherFactory.create();
    final LoggingListener logListener = LoggingListener.forBiConsumer((t, m) -> {
      System.out.println(m.get());
      if (t != null) {
        t.printStackTrace();
      }
      ;
    });
    final SummaryGeneratingListener execListener = new SummaryGeneratingListener();
    launcher.registerTestExecutionListeners(execListener, logListener);
    launcher.execute(request);
    execListener.getSummary().printTo(new java.io.PrintWriter(out));
  }

  static final boolean OS_WIN = System.getProperty("os.name").startsWith("Windows");

  sealed static interface RunShellResult permits RunShellResult.Ok, RunShellResult.Fail, RunShellResult.Err {
    record Ok(String stdout) implements RunShellResult {
    }

    record Fail(int exitCode, String stdout, String stderr) implements RunShellResult {
    }

    record Err(IOException e) implements RunShellResult {}
  }

  static final StringTemplate.Processor<RunShellResult, RuntimeException> RUNSHELL = st -> {
    var shellCmd = st.interpolate();
    var cmdAndArgs = OS_WIN ? new String[] { "cmd.exe", "/c", shellCmd } : new String[] { "sh", "-c", shellCmd };

    Path stderrRedirectPath = null;
    Process p = null;
    try{
      stderrRedirectPath = Files.createTempFile("RUNSHELL-", null);
      p = new ProcessBuilder()
          .command(cmdAndArgs)
          .redirectOutput(ProcessBuilder.Redirect.PIPE)
          .redirectError(stderrRedirectPath.toFile())
          .start();

      String stdout = "";
      try (var in = p.getInputStream()) {
        stdout = new String(in.readAllBytes());
      } 

      int  exitCode = p.waitFor();

      if (exitCode == 0) {
        return new RunShellResult.Ok(stdout);
      } else {
        String stderr = Files.readString(stderrRedirectPath);
        return new RunShellResult.Fail(exitCode, stdout, stderr);
      }
    } catch(IOException | InterruptedException e){
      if (p != null) p.destroyForcibly();
      return new RunShellResult.Err(e instanceof IOException ? (IOException) e : new IOException(e));
    } finally {
      if (stderrRedirectPath != null) {
        try {
          Files.deleteIfExists(stderrRedirectPath);
        } catch (IOException ignore) {}
      }
    }
  };

  @Test
  void test_run_shellcmd_stdout() {
    var cmd = "java";
    var args = "--help";
    switch (RUNSHELL."\{cmd} \{args}") {
      case RunShellResult.Ok(var stdout) -> assertTrue(stdout.contains("Usage:"));
      default -> fail();
    }
  }

  @Test
  void test_run_shellcmd_stderr(){
    var cmd = "java";
    switch (RUNSHELL."\{cmd}") {
      case RunShellResult.Fail(_, _, var stderr) -> assertTrue(stderr.contains("Usage:"));
      default -> fail();
    }
  }

  @Test
  void test_run_shellcmd_stderr_redirect_to_stdout() {
    var cmd = "java";
    var args = "2>&1";
    switch (RUNSHELL."\{cmd} \{args}") {
      case RunShellResult.Fail(_, var stdout, _) -> assertTrue(stdout.contains("Usage:"));
      default -> fail();
    }
  }

  @Test
  void test_run_shellcmd_quote() {
    var cmd = "fd.exe";
    var args = "-d 1 -g \"*.java\"";
    switch (RUNSHELL."\{cmd} \{args}") {
      case RunShellResult.Ok(var stdout) -> assertTrue(stdout.contains(".java"));
      default -> fail();
    }
  }
}
