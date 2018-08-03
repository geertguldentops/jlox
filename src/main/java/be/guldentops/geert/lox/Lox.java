package be.guldentops.geert.lox;

import be.guldentops.geert.lox.error.api.ErrorReporter;
import be.guldentops.geert.lox.error.impl.ConsoleErrorReporter;
import be.guldentops.geert.lox.lexer.Scanner;
import be.guldentops.geert.lox.lexer.Token;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {

    private final ErrorReporter errorReporter = new ConsoleErrorReporter();

    private boolean hadError = false;

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
        if (hadError) System.exit(65);
    }

    private void runPrompt() throws IOException {
        InputStreamReader input = new InputStreamReader(System.in);
        BufferedReader reader = new BufferedReader(input);

        for (; ; ) {
            System.out.print("> ");
            run(reader.readLine());
            hadError = false;
        }
    }

    private void run(String sourceCode) {
        Scanner scanner = new Scanner(sourceCode);
        scanner.addErrorReporter(errorReporter);

        List<Token> tokens = scanner.scanTokens();

        // For now, just print the tokens.
        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}
