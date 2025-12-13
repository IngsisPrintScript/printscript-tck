package implementation;

import com.ingsis.engine.InMemoryEngine;

import adapters.TckEngine;
import com.ingsis.utils.iterator.safe.result.DefaultIterationResultFactory;
import com.ingsis.utils.result.factory.DefaultResultFactory;
import interpreter.PrintScriptFormatter;
import interpreter.PrintScriptInterpreter;
import interpreter.PrintScriptLinter;

public class CustomImplementationFactory implements PrintScriptFactory {

  @Override
  public PrintScriptInterpreter interpreter() {
    return new TckEngine(new InMemoryEngine(new DefaultResultFactory(),new DefaultIterationResultFactory()));
  }

  @Override
  public PrintScriptFormatter formatter() {
    return new TckEngine(new InMemoryEngine(new DefaultResultFactory(), new DefaultIterationResultFactory()));
  }

  @Override
  public PrintScriptLinter linter() {
    return new TckEngine(new InMemoryEngine(new DefaultResultFactory(), new DefaultIterationResultFactory()));
  }
}
