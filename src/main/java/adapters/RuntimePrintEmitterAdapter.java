package adapters;

import com.ingsis.utils.runtime.PrintEmitter;

public final class RuntimePrintEmitterAdapter implements PrintEmitter {

    private final interpreter.PrintEmitter delegate;

    public RuntimePrintEmitterAdapter(interpreter.PrintEmitter delegate) {
        this.delegate = delegate;
    }

    @Override
    public void print(String value) {
        if (value == null) return;
        delegate.print(value);
    }
}
