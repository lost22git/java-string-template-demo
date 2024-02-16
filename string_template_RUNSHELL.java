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

import java.util.Arrays;
import java.io.IOException;
import java.io.UncheckedIOException;

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

  // RUNSHELL."shell cmd"
  // - something like `shell cmd` in crystal-lang
  // - inherit stderr and return stdout
  // - throws IOException
  static final StringTemplate.Processor<String, IOException> RUNSHELL = st -> {
    var shellCmd = st.interpolate();
    var cmdAndArgs = OS_WIN ? new String[] { "cmd.exe", "/c", shellCmd } : new String[] { "sh", "-c", shellCmd };

    var p = new ProcessBuilder()
        .command(cmdAndArgs)
        .redirectError(ProcessBuilder.Redirect.INHERIT)
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start();

    String output = "";
    try (var in = p.getInputStream()) {
      output = new String(in.readAllBytes());
    } catch (IOException e) {
      p.destroyForcibly();
      throw e;
    }

    try {
      p.waitFor();
    } catch (InterruptedException e) {
      p.destroyForcibly();
      throw new IOException(e);
    }

    return output;
  };

  @Test
  void test_run_shellcmd_stdout() throws IOException {
    var cmd = "java";
    var args = "--help";
    var output = RUNSHELL."\{cmd} \{args}";
    assertTrue(output.contains("Usage:"));
  }

  @Test
  void test_run_shellcmd_stderr() throws IOException {
    var cmd = "java";
    var output = RUNSHELL."\{cmd}";
    out.println(STR."\{output}");
    assertEquals(output, "");
  }

  @Test
  void test_run_shellcmd_stderr_redirect_to_stdout() throws IOException {
    var cmd = "java";
    var args = "2>&1";
    var output = RUNSHELL."\{cmd} \{args}";
    assertTrue(output.contains("Usage:"));
  }

  @Test
  void test_run_shellcmd_quote() throws IOException {
    var cmd = "fd.exe";
    var args = "-d 1 -g \"*.java\"";
    var output = RUNSHELL."\{cmd} \{args}";
    assertTrue(output.contains(".java"));
  }
}
