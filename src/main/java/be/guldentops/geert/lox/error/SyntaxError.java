package be.guldentops.geert.lox.error;

public record SyntaxError(int line, String location, String message) implements Error {

    public SyntaxError(int line, String message) {
        this(line, null, message);
    }

    @Override
    public String toString() {
        return String.format("[line %d] SyntaxError: %s", line, location == null ? message : "at '" + location + "' " + message);
    }
}
