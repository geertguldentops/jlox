package be.guldentops.geert.lox.error.api;

import be.guldentops.geert.lox.error.impl.ConsoleErrorReporter;

public interface ErrorReporter {

    static ErrorReporter console() {
        return new ConsoleErrorReporter();
    }

    void handle(Error error);

    boolean receivedError();

    void reset();
}
