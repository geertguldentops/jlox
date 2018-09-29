package be.guldentops.geert.lox.error.impl;

import be.guldentops.geert.lox.error.api.Error;
import be.guldentops.geert.lox.error.api.ErrorReporter;

public class ConsoleErrorReporter implements ErrorReporter {

    private boolean receiverError = false;

    @Override
    public void handle(Error error) {
        receiverError = true;
        System.err.println(error);
    }

    @Override
    public boolean receivedError() {
        return receiverError;
    }

    @Override
    public void reset() {
        this.receiverError = false;
    }
}
