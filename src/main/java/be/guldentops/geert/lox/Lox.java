package be.guldentops.geert.lox;

import be.guldentops.geert.lox.error.ErrorReporter;
import be.guldentops.geert.lox.interpreter.Interpreter;
import be.guldentops.geert.lox.lexer.Scanner;
import be.guldentops.geert.lox.parser.Parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

class Lox {

    private final ErrorReporter errorReporter;

    // MUST be a global variable so REPL sessions can reuse the same interpreter!
    private final Interpreter interpreter;

    Lox(ErrorReporter errorReporter) {
        this.errorReporter = errorReporter;
        this.interpreter = Interpreter.createDefault();
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
        var scanner = Scanner.createDefault(sourceCode);
        scanner.addErrorReporter(errorReporter);
        var tokens = scanner.scanTokens();

        var parser = Parser.createDefault(tokens);
        parser.addErrorReporter(errorReporter);
        var statements = parser.parse();

        // Stop if there was a syntax error.
        if (errorReporter.receivedError()) return;

        interpreter.interpret(statements);
    }
}
