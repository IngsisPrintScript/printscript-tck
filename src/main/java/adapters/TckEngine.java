package adapters;

import java.io.InputStream;
import java.io.Writer;

import com.ingsis.engine.Engine;
import com.ingsis.engine.versions.Version;
import com.ingsis.utils.result.Result;

import interpreter.ErrorHandler;
import interpreter.InputProvider;
import interpreter.PrintEmitter;
import interpreter.PrintScriptFormatter;
import interpreter.PrintScriptInterpreter;
import interpreter.PrintScriptLinter;

public class TckEngine
    implements PrintScriptInterpreter, PrintScriptFormatter, PrintScriptLinter {

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
    engine.format(src, config, writer, Version.fromString(version));
  }

  @Override
  public void execute(
      InputStream src,
      String version,
      PrintEmitter emitter,
      ErrorHandler handler,
      InputProvider provider) {
    try {
      Result<String> result = engine.interpret(
          Version.fromString(version),
          new OutputEmitterAdapter(emitter),
          new InputSupplierAdapter(provider),
          src);
      if (!result.isCorrect()) {
        handler.reportError(result.error());
      }
    } catch (OutOfMemoryError exception) {
      handler.reportError(exception.getMessage());
    }
  }
}
