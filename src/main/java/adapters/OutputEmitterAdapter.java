package adapters;

import com.ingsis.utils.evalstate.io.OutputEmitter;

import interpreter.PrintEmitter;

public class OutputEmitterAdapter implements OutputEmitter {
  private final PrintEmitter printEmitter;

  public OutputEmitterAdapter(PrintEmitter printEmitter) {
    this.printEmitter = printEmitter;
  }

  @Override
  public void emit(String value) {
    printEmitter.print(value);
  }

}
