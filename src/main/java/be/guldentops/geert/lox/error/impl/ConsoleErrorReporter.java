package be.guldentops.geert.lox.error.impl;

import be.guldentops.geert.lox.error.api.Error;
import be.guldentops.geert.lox.error.api.ErrorReporter;

public class ConsoleErrorReporter implements ErrorReporter {

    private boolean receivedError = false;

    @Override
    public void handle(Error error) {
        receivedError = true;
        System.err.println(error);
    }

    @Override
    public boolean receivedError() {
        return receivedError;
    }

    @Override
    public void reset() {
        this.receivedError = false;
    }
}
