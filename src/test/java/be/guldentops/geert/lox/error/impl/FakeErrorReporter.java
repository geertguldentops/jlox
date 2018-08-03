package be.guldentops.geert.lox.error.impl;

import be.guldentops.geert.lox.error.api.Error;
import be.guldentops.geert.lox.error.api.ErrorReporter;
import be.guldentops.geert.lox.error.api.RuntimeError;

public class FakeErrorReporter implements ErrorReporter {

    private Error error;
    private RuntimeError runtimeError;

    public Error getError() {
        return error;
    }

    public RuntimeError getRuntimeError() {
        return runtimeError;
    }

    @Override
    public void handle(Error error) {
        this.error = error;
    }

    @Override
    public boolean receivedError() {
        return error != null;
    }

    @Override
    public void handle(RuntimeError error) {
        this.runtimeError = error;
    }

    @Override
    public boolean receivedRuntimeError() {
        return runtimeError != null;
    }

    @Override
    public void reset() {
        error = null;
        runtimeError = null;
    }
}