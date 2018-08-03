package be.guldentops.geert.lox.error;

import be.guldentops.geert.lox.lexer.Token;

public class RuntimeError extends RuntimeException {

    public final Token token;

    public RuntimeError(Token token, String message) {
        super(message);

        this.token = token;
    }
}
