package be.guldentops.geert.lox.error.impl;

import be.guldentops.geert.lox.error.api.Error;
import be.guldentops.geert.lox.error.api.ErrorReporter;

public class ConsoleErrorReporter implements ErrorReporter {

    @Override
    public void handle(Error error) {
        System.err.println(error);
    }
}
