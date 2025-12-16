package implementation;

import adapters.TckEngine;

import com.ingsis.engine.DefaultEngine;
import com.ingsis.engine.services.ExecuteService;
import com.ingsis.engine.services.FormatService;
import com.ingsis.engine.services.LintService;
import interpreter.PrintScriptFormatter;
import interpreter.PrintScriptInterpreter;
import interpreter.PrintScriptLinter;

public class CustomImplementationFactory implements PrintScriptFactory {

  @Override
  public PrintScriptInterpreter interpreter() {
    return new TckEngine(new DefaultEngine(
        new ExecuteService(),
        new FormatService(),
        new LintService()));
  }

  @Override
  public PrintScriptFormatter formatter() {
    return new TckEngine(new DefaultEngine(
        new ExecuteService(),
        new FormatService(),
        new LintService()));
  }

  @Override
  public PrintScriptLinter linter() {
    return new TckEngine(new DefaultEngine(
        new ExecuteService(),
        new FormatService(),
        new LintService()));
  }
}
