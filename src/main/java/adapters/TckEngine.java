package adapters;

import java.io.InputStream;
import java.io.Writer;
import java.util.List;

import com.ingsis.engine.Engine;
import com.ingsis.engine.versions.Version;
import com.ingsis.utils.result.Result;
import com.ingsis.utils.runtime.DefaultRuntime;

import interpreter.ErrorHandler;
import interpreter.InputProvider;
import interpreter.PrintEmitter;
import interpreter.PrintScriptFormatter;
import interpreter.PrintScriptInterpreter;
import interpreter.PrintScriptLinter;

public class TckEngine
        implements PrintScriptInterpreter, PrintScriptFormatter, PrintScriptLinter {

  private static final boolean DEBUG_TCK = false; // ← activalo solo para debug

  private final Engine engine;

  public TckEngine(Engine engine) {
    this.engine = engine;
  }

  @Override
  public void lint(InputStream src, String version, InputStream config, ErrorHandler handler) {
    Result<String> result = engine.analyze(src, config, Version.fromString(version));
    if (!result.isCorrect()) {
      handler.reportError(result.error());
    }
  }

  @Override
  public void format(InputStream src, String version, InputStream config, Writer writer) {
    Result<String> result = engine.format(src, config, writer, Version.fromString(version));
    if (!result.isCorrect()) {
    }
  }

  @Override
  public void execute(
          InputStream src,
          String version,
          PrintEmitter emitter,
          ErrorHandler handler,
          InputProvider provider) {
    DefaultRuntime runtime = DefaultRuntime.getInstance();
    runtime.setEmitter(new RuntimePrintEmitterAdapter(emitter));
    runtime.push();
    try {
      Result<String> result =
              engine.interpret(src, Version.fromString(version));
      if (!result.isCorrect() && runtime.getExecutionError() != null) {
        handler.reportError(runtime.getExecutionError().error());
      }
    } catch (OutOfMemoryError oom) {
      handler.reportError("Java heap space");

    } finally {
      runtime.setExecutionError(null);
      runtime.setEmitter(null);
      runtime.pop();
    }
  }
}
