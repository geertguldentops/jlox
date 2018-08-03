package be.guldentops.geert.lox.error;

class ConsoleErrorReporter implements ErrorReporter {

    private boolean receivedError = false;
    private boolean receivedRuntimeError = false;

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
    public void handle(RuntimeError error) {
        receivedRuntimeError = true;
        System.err.printf("%s\n[line %s]", error.getMessage(), error.token.line);
        System.err.println();
    }

    @Override
    public boolean receivedRuntimeError() {
        return receivedRuntimeError;
    }

    @Override
    public void reset() {
        this.receivedError = false;
        this.receivedRuntimeError = false;
    }
}
