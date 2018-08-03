package be.guldentops.geert.lox.error;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static be.guldentops.geert.lox.lexer.TokenObjectMother.plus;
import static org.assertj.core.api.Assertions.assertThat;

class ConsoleErrorReporterTest {

    private PrintStream originalErr;
    private ByteArrayOutputStream errContent;

    private ErrorReporter consoleErrorReporter;

    @BeforeEach
    void setUp() {
        originalErr = System.err;
        consoleErrorReporter = new ConsoleErrorReporter();
        errContent = new ByteArrayOutputStream();

        System.setErr(new PrintStream(errContent));
    }

    @AfterEach
    void tearDown() {
        System.setErr(originalErr);
    }

    @Test
    void printsErrorToSystemErr() {
        assertThat(consoleErrorReporter.receivedError()).isFalse();

        consoleErrorReporter.handle(new Error(1, " at end", ""));

        assertThat(consoleErrorReporter.receivedError()).isTrue();
        assertThat(errContent.toString()).isEqualTo("[line 1] Error at end: \n");
    }

    @Test
    void printsRuntimeErrorToSystemErr() {
        assertThat(consoleErrorReporter.receivedRuntimeError()).isFalse();

        consoleErrorReporter.handle(new RuntimeError(plus(), "Operand must be a number"));

        assertThat(consoleErrorReporter.receivedRuntimeError()).isTrue();
        assertThat(errContent.toString()).isEqualTo("Operand must be a number\n[line 1]\n");
    }

    @Test
    void resetAfterError() {
        consoleErrorReporter.handle(new Error(1, " at end", ""));
        assertThat(consoleErrorReporter.receivedError()).isTrue();

        consoleErrorReporter.reset();

        assertThat(consoleErrorReporter.receivedError()).isFalse();
    }

    @Test
    void resetAfterRuntimeError() {
        consoleErrorReporter.handle(new RuntimeError(plus(), "Operand must be a number"));
        assertThat(consoleErrorReporter.receivedRuntimeError()).isTrue();

        consoleErrorReporter.reset();

        assertThat(consoleErrorReporter.receivedRuntimeError()).isFalse();
    }

}