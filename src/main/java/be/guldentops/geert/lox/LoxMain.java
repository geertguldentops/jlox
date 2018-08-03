package be.guldentops.geert.lox;

import be.guldentops.geert.lox.error.ErrorReporter;

import java.io.IOException;

/**
 * This class is NOT tested since it:
 * <p>
 * * Exposes the static method main which makes it harder to test.
 * * When errors occur or the program is incorrectly used it performs a System.exit which is hard to test.
 * <p>
 * Because it is not tested its logic is kept to a bare minimum.
 */
class LoxMain {

    public static void main(String[] args) throws IOException {
        var syntaxErrorReporter = ErrorReporter.console();
        var semanticErrorReporter = ErrorReporter.console();
        var runtimeErrorReporter = ErrorReporter.console();
        var lox = new Lox(syntaxErrorReporter, semanticErrorReporter, runtimeErrorReporter);

        if (args.length > 1) {
            System.out.println("Usage: jlox [script]");
            System.exit(64);
        } else if (args.length == 1) {
            lox.runFile(args[0]);

            if (syntaxErrorReporter.receivedError() || semanticErrorReporter.receivedError()) System.exit(65);
            if (runtimeErrorReporter.receivedError()) System.exit(70);
        } else {
            lox.runPrompt();
        }
    }
}
