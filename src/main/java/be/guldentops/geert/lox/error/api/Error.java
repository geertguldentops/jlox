package be.guldentops.geert.lox.error.api;

public class Error {

    public final int line;
    public final String location;
    public final String message;

    public Error(int line, String location, String message) {
        this.line = line;
        this.location = location;
        this.message = message;
    }

    public Error(int line, String message) {
        this(line, null, message);
    }

    @Override
    public String toString() {
        return "[line " + line + "] Error" + location + ": " + message;
    }
}
