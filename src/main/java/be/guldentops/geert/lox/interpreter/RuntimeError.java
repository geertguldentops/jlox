package be.guldentops.geert.lox.interpreter;

import be.guldentops.geert.lox.error.Error;
import be.guldentops.geert.lox.lexer.Token;

class RuntimeError extends RuntimeException implements Error {

    private final Token token;

    RuntimeError(Token token, String message) {
        super(message);

        this.token = token;
    }

    @Override
    public String toString() {
        return String.format("[line %d] RuntimeError: at '%s' %s", token.line(), token.lexeme(), getMessage());
    }
}
