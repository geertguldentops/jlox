package be.guldentops.geert.lox.main;

import be.guldentops.geert.lox.error.api.ErrorReporter;
import be.guldentops.geert.lox.interpreter.api.Environment;
import be.guldentops.geert.lox.interpreter.api.Interpreter;
import be.guldentops.geert.lox.lexer.api.Scanner;
import be.guldentops.geert.lox.parser.api.Parser;
import be.guldentops.geert.lox.semantic.analysis.api.Resolver;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Lox {

    private final ErrorReporter syntaxErrorReporter;
    private final ErrorReporter semanticErrorReporter;
    private final ErrorReporter runtimeErrorReporter;

    // MUST be a global variable so REPL sessions can reuse the same interpreter!
    private final Interpreter interpreter;

    public Lox(ErrorReporter syntaxErrorReporter, ErrorReporter semanticErrorReporter, ErrorReporter runtimeErrorReporter) {
        this.syntaxErrorReporter = syntaxErrorReporter;
        this.semanticErrorReporter = semanticErrorReporter;
        this.runtimeErrorReporter = runtimeErrorReporter;
        this.interpreter = Interpreter.createDefault(Environment.createGlobal());
        this.interpreter.addErrorReporter(runtimeErrorReporter);
    }

    public void runFile(String path) throws IOException {
        var bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));
    }

    void runPrompt() throws IOException {
        var reader = new BufferedReader(new InputStreamReader(System.in));

        while (true) {
            System.out.print("> ");
            run(reader.readLine());
            resetAllErrorReporters();
        }
    }

    private void resetAllErrorReporters() {
        syntaxErrorReporter.reset();
        semanticErrorReporter.reset();
        runtimeErrorReporter.reset();
    }

    private void run(String sourceCode) {
        var scanner = Scanner.createDefault(sourceCode);
        scanner.addErrorReporter(syntaxErrorReporter);
        var tokens = scanner.scanTokens();

        var parser = Parser.createDefault(tokens);
        parser.addErrorReporter(syntaxErrorReporter);
        var statements = parser.parse();

        // Stop if the parser found a syntax error.
        if (syntaxErrorReporter.receivedError()) return;

        var resolver = Resolver.createDefault(interpreter);
        resolver.addErrorReporter(semanticErrorReporter);
        resolver.resolve(statements);

        // Stop if the resolver found a semantic error.
        if (semanticErrorReporter.receivedError()) return;

        interpreter.interpret(statements);
    }
}