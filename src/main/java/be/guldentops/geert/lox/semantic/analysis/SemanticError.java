package be.guldentops.geert.lox.semantic.analysis;

import be.guldentops.geert.lox.error.Error;
import be.guldentops.geert.lox.lexer.Token;

record SemanticError(Token token, String message) implements Error {

    @Override
    public String toString() {
        return String.format("[line %d] SemanticError: at '%s' %s", token.line(), token.lexeme(), message);
    }
}
