package adapters;

import com.ingsis.utils.evalstate.io.InputSupplier;
import com.ingsis.utils.value.Value;

import interpreter.InputProvider;

public class InputSupplierAdapter implements InputSupplier {
  private final InputProvider provider;

  public InputSupplierAdapter(InputProvider provider) {
    this.provider = provider;
  }

  @Override
  public Value supply() {
    return new Value.StringValue(provider.input(""));
  }
}
