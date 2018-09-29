package be.guldentops.geert.lox.semantic.analysis.impl;

import be.guldentops.geert.lox.error.api.Error;
import be.guldentops.geert.lox.lexer.api.Token;

public class SemanticError implements Error {

    private final Token token;
    private final String message;

    SemanticError(Token token, String message) {
        this.token = token;
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("[line %d] SemanticError: at '%s' %s", token.line, token.lexeme, message);
    }
}
