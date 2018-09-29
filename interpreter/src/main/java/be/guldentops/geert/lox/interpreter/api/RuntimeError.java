package be.guldentops.geert.lox.interpreter.api;

import be.guldentops.geert.lox.error.api.Error;
import be.guldentops.geert.lox.lexer.api.Token;

public class RuntimeError extends RuntimeException implements Error {

    private final Token token;

    public RuntimeError(Token token, String message) {
        super(message);

        this.token = token;
    }

    @Override
    public String toString() {
        return String.format("[line %d] RuntimeError: at '%s' %s", token.line, token.lexeme, getMessage());
    }
}
