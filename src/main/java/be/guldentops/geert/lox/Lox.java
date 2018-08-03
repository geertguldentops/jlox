package be.guldentops.geert.lox;

import be.guldentops.geert.lox.error.api.ErrorReporter;
import be.guldentops.geert.lox.error.impl.ConsoleErrorReporter;
import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.interpreter.api.Interpreter;
import be.guldentops.geert.lox.interpreter.impl.PostOrderTraversalInterpreter;
import be.guldentops.geert.lox.lexer.api.Scanner;
import be.guldentops.geert.lox.lexer.api.Token;
import be.guldentops.geert.lox.lexer.impl.SimpleScanner;
import be.guldentops.geert.lox.parser.api.Parser;
import be.guldentops.geert.lox.parser.impl.RecursiveDecentParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    private final ErrorReporter errorReporter = new ConsoleErrorReporter();

    // MUST be a global variable so REPL sessions can reuse the same interpreter!
    private final Interpreter interpreter = new PostOrderTraversalInterpreter();

    public static void main(String[] args) throws IOException {
        new Lox().run(args);
    }

    private void run(String[] args) throws IOException {
        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            runFile(args[0]);
        } else {
            runPrompt();
        }
    }

    private void runFile(String path) throws IOException {
        byte[] bytes = Files.readAllBytes(Paths.get(path));
        run(new String(bytes, Charset.defaultCharset()));

        // Indicate an error in the exit code.
        if (errorReporter.receivedError()) System.exit(65);
        if (errorReporter.receivedRuntimeError()) System.exit(70);
    }

    private void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.print("> ");
            run(reader.readLine());
            errorReporter.reset();
        }
    }

    private void run(String sourceCode) {
        Scanner scanner = new SimpleScanner(sourceCode);
        scanner.addErrorReporter(errorReporter);
        List<Token> tokens = scanner.scanTokens();

        Parser parser = new RecursiveDecentParser(tokens);
        parser.addErrorReporter(errorReporter);
        Expression expression = parser.parse();

        // Stop if there was a syntax error.
        if (errorReporter.receivedError()) return;


        interpreter.addErrorReporter(errorReporter);
        Object result = interpreter.interpret(expression);

        // Stop if there was a runtime error.
        if (errorReporter.receivedRuntimeError()) return;

        System.out.println(stringify(result));
    }

    private String stringify(Object object) {
        if (object == null) return "nil";

        // Hack. Work around Java adding ".0" to integer-valued doubles.
        if (object instanceof Double) {
            String text = object.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return object.toString();
    }
}
