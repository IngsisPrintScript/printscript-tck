package adapters;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.Queue;

import com.ingsis.engine.factories.charstream.CharStreamFactory;
import com.ingsis.engine.factories.charstream.InMemoryCharStreamFactory;
import com.ingsis.engine.factories.interpreter.DefaultProgramInterpreterFactory;
import com.ingsis.engine.factories.interpreter.ProgramInterpreterFactory;
import com.ingsis.engine.factories.lexer.InMemoryLexerFactory;
import com.ingsis.engine.factories.lexer.LexerFactory;
import com.ingsis.engine.factories.semantic.DefaultSemanticFactory;
import com.ingsis.engine.factories.semantic.SemanticFactory;
import com.ingsis.engine.factories.syntactic.DefaultSyntacticFactory;
import com.ingsis.engine.factories.syntactic.SyntacticFactory;
import com.ingsis.engine.factories.tokenstream.DefaultTokenStreamFactory;
import com.ingsis.engine.factories.tokenstream.TokenStreamFactory;
import com.ingsis.interpreter.ProgramInterpreter;
import com.ingsis.interpreter.visitor.expression.strategies.factories.DefaultSolutionStrategyFactory;
import com.ingsis.interpreter.visitor.expression.strategies.factories.SolutionStrategyFactory;
import com.ingsis.interpreter.visitor.factory.DefaultInterpreterVisitorFactory;
import com.ingsis.interpreter.visitor.factory.InterpreterVisitorFactory;
import com.ingsis.lexer.tokenizers.factories.FirstTokenizerFactory;
import com.ingsis.lexer.tokenizers.factories.SecondTokenizerFactory;
import com.ingsis.lexer.tokenizers.factories.TokenizerFactory;
import com.ingsis.nodes.factories.DefaultNodeFactory;
import com.ingsis.nodes.factories.NodeFactory;
import com.ingsis.result.Result;
import com.ingsis.result.factory.DefaultResultFactory;
import com.ingsis.result.factory.LoggerResultFactory;
import com.ingsis.result.factory.ResultFactory;
import com.ingsis.runtime.DefaultRuntime;
import com.ingsis.syntactic.factories.DefaultParserChainFactory;
import com.ingsis.syntactic.factories.ParserChainFactory;
import com.ingsis.syntactic.parsers.factories.DefaultParserFactory;
import com.ingsis.tokens.factories.DefaultTokensFactory;
import com.ingsis.tokens.factories.TokenFactory;

import interpreter.ErrorHandler;
import interpreter.InputProvider;
import interpreter.PrintEmitter;
import interpreter.PrintScriptInterpreter;

public class InterpreterAdapter implements PrintScriptInterpreter {
  @Override
  public void execute(InputStream src,
      String version,
      PrintEmitter emitter,
      ErrorHandler handler,
      InputProvider provider) {
    PrintStream originalOut = System.out;
    InputStream originalIn = System.in;

    ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
    PrintStream wrappedOut = new PrintStream(outBuffer);

    InputStream inputFromProvider = new InputStream() {
      private Queue<Character> buffer = new LinkedList<>();

      @Override
      public int read() {
        if (buffer.isEmpty()) {
          String value = provider.input("");
          if (value == null)
            return -1; // EOF
          for (char c : value.toCharArray())
            buffer.add(c);
          buffer.add('\n');
        }
        return buffer.poll();
      }
    };

    System.setOut(wrappedOut);
    System.setIn(inputFromProvider);

    try {
      Result<String> interpretResult = createProgramInterpreter(version, src).interpret();

      // Forward captured prints
      emitter.print(outBuffer.toString());

      // Report errors if interpreter failed
      if (!interpretResult.isCorrect()) {
        handler.reportError(DefaultRuntime.getInstance().getExecutionError().error());
      }

    } catch (IOException e) {
      handler.reportError(e.getMessage());
    } finally {
      // Restore original streams
      System.setOut(originalOut);
      System.setIn(originalIn);
    }
  }

  private ProgramInterpreter createProgramInterpreter(String version, InputStream inputStream) throws IOException {
    return createProgramInterpreterFactory(version).fromInputStream(inputStream);
  }

  private ProgramInterpreterFactory createProgramInterpreterFactory(String version) {
    ResultFactory resultFactory = new LoggerResultFactory(new DefaultResultFactory(), DefaultRuntime.getInstance());
    SemanticFactory semanticFactory = createSemanticFactory(version, resultFactory);
    SolutionStrategyFactory solutionStrategyFactory = new DefaultSolutionStrategyFactory(DefaultRuntime.getInstance());
    InterpreterVisitorFactory interpreterVisitorFactory = new DefaultInterpreterVisitorFactory(solutionStrategyFactory,
        resultFactory);
    return new DefaultProgramInterpreterFactory(
        semanticFactory, interpreterVisitorFactory, DefaultRuntime.getInstance());
  }

  private SemanticFactory createSemanticFactory(String version, ResultFactory resultFactory) {
    CharStreamFactory charStreamFactory = new InMemoryCharStreamFactory();
    TokenFactory tokenFactory = new DefaultTokensFactory();
    TokenizerFactory tokenizerFactory;
    switch (version) {
      case "1.0":
        tokenizerFactory = new FirstTokenizerFactory(tokenFactory, resultFactory);
        break;
      case "1.1":
        tokenizerFactory = new SecondTokenizerFactory(tokenFactory, resultFactory);
        break;
      default:
        throw new IllegalArgumentException("Unsupported version: " + version);
    }
    LexerFactory lexerFactory = new InMemoryLexerFactory(charStreamFactory, tokenizerFactory);
    TokenStreamFactory tokenStreamFactory = new DefaultTokenStreamFactory(lexerFactory, resultFactory);
    NodeFactory nodeFactory = new DefaultNodeFactory();
    ParserChainFactory parserChainFactory = new DefaultParserChainFactory(
        new DefaultParserFactory(tokenFactory, nodeFactory));
    SyntacticFactory syntacticFactory = new DefaultSyntacticFactory(tokenStreamFactory, parserChainFactory);
    return new DefaultSemanticFactory(
        syntacticFactory, resultFactory, DefaultRuntime.getInstance());
  }
}
