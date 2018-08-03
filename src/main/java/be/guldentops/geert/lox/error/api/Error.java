package be.guldentops.geert.lox.error.api;

public class Error {

    private int line;
    private String location;
    private String message;

    public Error(int line, String location, String message) {
        this.line = line;
        this.location = location;
        this.message = message;
    }

    public Error(int line, String message) {
        this.line = line;
        this.message = message;
    }

    public int getLine() {
        return line;
    }

    public String getLocation() {
        return location;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "[line " + line + "] Error" + location + ": " + message;
    }
}
