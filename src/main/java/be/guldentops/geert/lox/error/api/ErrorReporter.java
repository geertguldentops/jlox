package be.guldentops.geert.lox.error.api;

public interface ErrorReporter {

    void handle(Error error);

    boolean receivedError();

    void handle(RuntimeError error);

    boolean receivedRuntimeError();

    void reset();
}
