package be.guldentops.geert.lox;

import be.guldentops.geert.lox.environment.Environment;
import be.guldentops.geert.lox.error.api.ErrorReporter;
import be.guldentops.geert.lox.interpreter.api.Interpreter;
import be.guldentops.geert.lox.interpreter.api.LoxCallable;
import be.guldentops.geert.lox.interpreter.impl.PostOrderTraversalInterpreter;
import be.guldentops.geert.lox.lexer.impl.SimpleScanner;
import be.guldentops.geert.lox.parser.impl.RecursiveDescentParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    private final ErrorReporter errorReporter;

    // MUST be a global variable so REPL sessions can reuse the same interpreter!
    private final Interpreter interpreter;

    public Lox(ErrorReporter errorReporter) {
        var globals = Environment.createGlobal();
        globals.defineNativeMethod("clock", new LoxCallable() {

            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1_000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        this.errorReporter = errorReporter;
        this.interpreter = new PostOrderTraversalInterpreter(globals);
        this.interpreter.addErrorReporter(errorReporter);
    }

    void runFile(String path) throws IOException {
        var bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    void runPrompt() throws IOException {
        var reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("> ");
            run(reader.readLine());
            errorReporter.reset();
        }
    }

    private void run(String sourceCode) {
        var scanner = new SimpleScanner(sourceCode);
        scanner.addErrorReporter(errorReporter);
        var tokens = scanner.scanTokens();

        var parser = new RecursiveDescentParser(tokens);
        parser.addErrorReporter(errorReporter);
        var statements = parser.parse();

        // Stop if there was a syntax error.
        if (errorReporter.receivedError()) return;

        interpreter.interpret(statements);
    }
}
