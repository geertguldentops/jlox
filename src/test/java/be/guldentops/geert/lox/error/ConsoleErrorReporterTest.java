package be.guldentops.geert.lox.error;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

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

        consoleErrorReporter.handle(new SyntaxError(1, "@", "unexpected character"));

        assertThat(consoleErrorReporter.receivedError()).isTrue();
        assertThat(errContent.toString()).isEqualTo("[line 1] SyntaxError: at '@' unexpected character\n");
    }

    @Test
    void resetAfterError() {
        consoleErrorReporter.handle(new SyntaxError(1, "@", "unexpected character"));
        assertThat(consoleErrorReporter.receivedError()).isTrue();

        consoleErrorReporter.reset();

        assertThat(consoleErrorReporter.receivedError()).isFalse();
    }
}