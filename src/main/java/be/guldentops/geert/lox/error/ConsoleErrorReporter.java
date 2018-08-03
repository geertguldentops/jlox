package be.guldentops.geert.lox.error;

class ConsoleErrorReporter implements ErrorReporter {

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
