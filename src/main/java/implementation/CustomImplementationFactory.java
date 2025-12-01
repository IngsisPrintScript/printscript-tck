package implementation;

import com.ingsis.engine.InMemoryEngine;

import adapters.TckEngine;
import interpreter.PrintScriptFormatter;
import interpreter.PrintScriptInterpreter;
import interpreter.PrintScriptLinter;

public class CustomImplementationFactory implements PrintScriptFactory {

  @Override
  public PrintScriptInterpreter interpreter() {
    return new TckEngine(new InMemoryEngine());
  }

  @Override
  public PrintScriptFormatter formatter() {
    return new TckEngine(new InMemoryEngine());
  }

  @Override
  public PrintScriptLinter linter() {
    return new TckEngine(new InMemoryEngine());
  }
}
