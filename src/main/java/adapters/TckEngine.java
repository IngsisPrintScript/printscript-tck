package adapters;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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

public class TckEngine implements PrintScriptInterpreter, PrintScriptFormatter, PrintScriptLinter {
  private final Engine engine;

  public TckEngine(Engine engine) {
    this.engine = engine;
  }

  @Override
  public void lint(InputStream src, String version, InputStream config, ErrorHandler handler) {
    try (var redirect = new SystemRedirection(handler)) {
      Result<String> result = engine.analyze(src, config, Version.fromString(version));
      if (!result.isCorrect()) {
        System.err.println(result.error());
      }
    }
  }

  @Override
  public void format(InputStream src, String version, InputStream config, Writer writer) {
    Result<String> result = engine.format(src, config, writer, Version.fromString(version));
    if (!result.isCorrect()) {
      System.err.println(result.error());
    }
  }

    @Override
    public void execute(InputStream src, String version, PrintEmitter emitter,
                        ErrorHandler handler, InputProvider provider) {
        try (var redirect = new SystemRedirection(provider, emitter, handler)) {
            DefaultRuntime.getInstance().push();
            try {
                Result<String> interpretResult =
                        engine.interpret(src, Version.fromString(version));

                if (!interpretResult.isCorrect()) {
                    System.err.println(interpretResult.error());
                }

            } finally {
                DefaultRuntime.getInstance().pop();
            }

        } catch (OutOfMemoryError error) {
            handler.reportError("Java heap space");
        } catch (Exception exception) {
            handler.reportError(exception.getMessage());
        }
    }
}
