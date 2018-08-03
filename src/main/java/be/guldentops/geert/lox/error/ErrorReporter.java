package be.guldentops.geert.lox.error;

public interface ErrorReporter {

    static ErrorReporter console() {
        return new ConsoleErrorReporter();
    }

    void handle(Error error);

    boolean receivedError();

    void handle(RuntimeError error);

    boolean receivedRuntimeError();

    void reset();
}
