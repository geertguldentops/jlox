package be.guldentops.geert.lox.parser.impl;

import be.guldentops.geert.lox.error.api.Error;
import be.guldentops.geert.lox.error.api.ErrorReporter;
import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.lexer.api.Token;
import be.guldentops.geert.lox.lexer.api.Token.Type;
import be.guldentops.geert.lox.parser.api.Parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static be.guldentops.geert.lox.lexer.api.Token.Type.BANG;
import static be.guldentops.geert.lox.lexer.api.Token.Type.BANG_EQUAL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.EOF;
import static be.guldentops.geert.lox.lexer.api.Token.Type.EQUAL_EQUAL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.FALSE;
import static be.guldentops.geert.lox.lexer.api.Token.Type.GREATER;
import static be.guldentops.geert.lox.lexer.api.Token.Type.GREATER_EQUAL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.LEFT_PAREN;
import static be.guldentops.geert.lox.lexer.api.Token.Type.LESS;
import static be.guldentops.geert.lox.lexer.api.Token.Type.LESS_EQUAL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.MINUS;
import static be.guldentops.geert.lox.lexer.api.Token.Type.NIL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.NUMBER;
import static be.guldentops.geert.lox.lexer.api.Token.Type.PLUS;
import static be.guldentops.geert.lox.lexer.api.Token.Type.RIGHT_PAREN;
import static be.guldentops.geert.lox.lexer.api.Token.Type.SEMICOLON;
import static be.guldentops.geert.lox.lexer.api.Token.Type.SLASH;
import static be.guldentops.geert.lox.lexer.api.Token.Type.STAR;
import static be.guldentops.geert.lox.lexer.api.Token.Type.STRING;
import static be.guldentops.geert.lox.lexer.api.Token.Type.TRUE;

public class RecursiveDecentParser implements Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    private final List<ErrorReporter> errorReporters = new ArrayList<>();

    public RecursiveDecentParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    @Override
    public void addErrorReporter(ErrorReporter errorReporter) {
        this.errorReporters.add(errorReporter);
    }

    public Expression parse() {
        try {
            if (tokens != null && !tokens.isEmpty()) {
                return expression();
            } else {
                return null;
            }
        } catch (ParseError error) {
            return null;
        }
    }

    private Expression expression() {
        return equality();
    }

    private Expression equality() {
        return parseLeftAssociativeBinaryExpressions(this::comparison, BANG_EQUAL, EQUAL_EQUAL);
    }

    private Expression comparison() {
        return parseLeftAssociativeBinaryExpressions(this::addition, GREATER, GREATER_EQUAL, LESS, LESS_EQUAL);
    }

    private Expression addition() {
        return parseLeftAssociativeBinaryExpressions(this::multiplication, MINUS, PLUS);
    }

    private Expression multiplication() {
        return parseLeftAssociativeBinaryExpressions(this::unary, SLASH, STAR);
    }

    private Expression unary() {
        if (match(BANG, MINUS)) {
            Token operator = previous();
            Expression right = unary();
            return new Expression.Unary(operator, right);
        }

        return primary();
    }

    private Expression primary() {
        if (match(FALSE)) return new Expression.Literal(false);
        if (match(TRUE)) return new Expression.Literal(true);
        if (match(NIL)) return new Expression.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expression.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            Expression expression = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expression);
        }

        throw error(peek(), "Expect expression.");
    }

    private Token consume(Type type, String message) {
        if (check(type)) return advance();

        throw error(peek(), message);
    }

    private ParseError error(Token token, String message) {
        reportError(token, message);
        return new ParseError();
    }

    private void reportError(Token token, String message) {
        for (ErrorReporter errorReporter : errorReporters) {
            if (token.type == EOF) {
                errorReporter.handle(new Error(token.line, " at end", message));
            } else {
                errorReporter.handle(new Error(token.line, String.format(" at '%s'", token.lexeme), message));
            }
        }
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type == SEMICOLON) return;

            switch (peek().type) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            advance();
        }
    }

    private Expression parseLeftAssociativeBinaryExpressions(Supplier<Expression> operand, Type... types) {
        Expression expression = operand.get();

        while (match(types)) {
            Token operator = previous();
            Expression right = operand.get();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private boolean match(Type... types) {
        for (Type type : types) {
            if (check(type)) {
                advance();
                return true;
            }
        }

        return false;
    }

    private boolean check(Type type) {
        if (isAtEnd()) return false;
        return peek().type == type;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return peek().type == EOF;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
