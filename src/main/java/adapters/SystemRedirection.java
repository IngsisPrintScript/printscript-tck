package adapters;

import java.io.*;
import java.nio.charset.StandardCharsets;

import interpreter.ErrorHandler;
import interpreter.InputProvider;
import interpreter.PrintEmitter;

public final class SystemRedirection implements AutoCloseable {

  private final InputStream originalIn;
  private final PrintStream originalOut;
  private final PrintStream originalErr;

  public SystemRedirection(
      InputProvider provider,
      PrintEmitter emitter,
      ErrorHandler handler) {
    this.originalIn = System.in;
    this.originalOut = System.out;
    this.originalErr = System.err;

    System.setIn(createInputStream(provider));
    System.setOut(createPrintStream(emitter));
    System.setErr(createErrorStream(handler));
  }

  public SystemRedirection(
      ErrorHandler handler) {
    this.originalIn = System.in;
    this.originalOut = System.out;
    this.originalErr = System.err;

    System.setErr(createErrorStream(handler));
  }

  // ----------------------
  // Input Redirection
  // ----------------------
  private InputStream createInputStream(InputProvider provider) {
    return new InputStream() {
      private byte[] buffer = new byte[0];
      private int index = 0;

      @Override
      public int read() {
        if (index >= buffer.length) {
          String next = provider.input("stdin");
          if (next == null)
            return -1;
          buffer = (next + "\n").getBytes(StandardCharsets.UTF_8);
          index = 0;
        }
        return buffer[index++];
      }
    };
  }

  // ----------------------
  // Output Redirection
  // ----------------------
  private PrintStream createPrintStream(PrintEmitter emitter) {
    return new PrintStream(new OutputStream() {
      private final StringBuilder buffer = new StringBuilder();

      @Override
      public void write(int b) {
        char c = (char) b;
        buffer.append(c);

        // Flush on newline
        if (c == '\n') {
          flushBuffer();
        }
      }

      @Override
      public void flush() {
        flushBuffer();
      }

      private void flushBuffer() {
        if (buffer.length() > 0) {
          emitter.print(buffer.toString().trim());
          buffer.setLength(0);
        }
      }
    }, true); 
  }

  // ----------------------
  // Error Redirection
  // ----------------------
  private PrintStream createErrorStream(ErrorHandler handler) {
    return new PrintStream(new OutputStream() {
      private final StringBuilder buffer = new StringBuilder();

      @Override
      public void write(int b) {
        char c = (char) b;
        buffer.append(c);

        if (c == '\n') {
          flushBuffer();
        }
      }

      @Override
      public void flush() {
        flushBuffer();
      }

      private void flushBuffer() {
        if (buffer.length() > 0) {
          handler.reportError(buffer.toString().trim());
          buffer.setLength(0);
        }
      }
    }, true);
  }

  // ----------------------
  // Restore originals
  // ----------------------
  @Override
  public void close() {
    System.setIn(originalIn);
    System.setOut(originalOut);
    System.setErr(originalErr);
  }
}
