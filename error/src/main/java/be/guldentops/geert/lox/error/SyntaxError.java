package be.guldentops.geert.lox.error;

public class SyntaxError implements Error {

    private final int line;
    private final String location;
    private final String message;

    public SyntaxError(int line, String location, String message) {
        this.line = line;
        this.location = location;
        this.message = message;
    }

    public SyntaxError(int line, String message) {
        this(line, null, message);
    }

    @Override
    public String toString() {
        return String.format("[line %d] SyntaxError: %s", line, location == null ? message : "at '" + location + "' " + message);
    }
}
