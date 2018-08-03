package be.guldentops.geert.lox.parser.impl;

import be.guldentops.geert.lox.error.api.Error;
import be.guldentops.geert.lox.error.api.ErrorReporter;
import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.grammar.Statement;
import be.guldentops.geert.lox.lexer.api.Token;
import be.guldentops.geert.lox.lexer.api.Token.Type;
import be.guldentops.geert.lox.parser.api.Parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static be.guldentops.geert.lox.lexer.api.Token.Type.BANG;
import static be.guldentops.geert.lox.lexer.api.Token.Type.BANG_EQUAL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.EOF;
import static be.guldentops.geert.lox.lexer.api.Token.Type.EQUAL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.EQUAL_EQUAL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.FALSE;
import static be.guldentops.geert.lox.lexer.api.Token.Type.GREATER;
import static be.guldentops.geert.lox.lexer.api.Token.Type.GREATER_EQUAL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.IDENTIFIER;
import static be.guldentops.geert.lox.lexer.api.Token.Type.LEFT_BRACE;
import static be.guldentops.geert.lox.lexer.api.Token.Type.LEFT_PAREN;
import static be.guldentops.geert.lox.lexer.api.Token.Type.LESS;
import static be.guldentops.geert.lox.lexer.api.Token.Type.LESS_EQUAL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.MINUS;
import static be.guldentops.geert.lox.lexer.api.Token.Type.NIL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.NUMBER;
import static be.guldentops.geert.lox.lexer.api.Token.Type.PLUS;
import static be.guldentops.geert.lox.lexer.api.Token.Type.PRINT;
import static be.guldentops.geert.lox.lexer.api.Token.Type.RIGHT_BRACE;
import static be.guldentops.geert.lox.lexer.api.Token.Type.RIGHT_PAREN;
import static be.guldentops.geert.lox.lexer.api.Token.Type.SEMICOLON;
import static be.guldentops.geert.lox.lexer.api.Token.Type.SLASH;
import static be.guldentops.geert.lox.lexer.api.Token.Type.STAR;
import static be.guldentops.geert.lox.lexer.api.Token.Type.STRING;
import static be.guldentops.geert.lox.lexer.api.Token.Type.TRUE;
import static be.guldentops.geert.lox.lexer.api.Token.Type.VAR;

public class RecursiveDescentParser implements Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    private final List<ErrorReporter> errorReporters = new ArrayList<>();

    public RecursiveDescentParser(List<Token> tokens) {
        this.tokens = tokens;
    }

    @Override
    public void addErrorReporter(ErrorReporter errorReporter) {
        this.errorReporters.add(errorReporter);
    }

    @Override
    public List<Statement> parse() {
        if (tokens == null || tokens.isEmpty()) return Collections.emptyList();

        return program();
    }

    private List<Statement> program() {
        var statements = new ArrayList<Statement>();
        while (!isAtEnd()) {
            statements.add(declaration());
        }

        return statements;
    }

    private Statement declaration() {
        try {
            if (match(VAR)) return variableDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Statement variableDeclaration() {
        Token name = consume(IDENTIFIER, "Expected variable name");

        Expression initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "Expect ';' after variable declaration.");
        return new Statement.Variable(name, initializer);
    }

    private Statement statement() {
        if (match(PRINT)) return printStatement();
        if (match(LEFT_BRACE)) return new Statement.Block(block());

        return expressionStatement();
    }

    private List<Statement> block() {
        var statements = new ArrayList<Statement>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "Expect '}' after block.");
        return statements;
    }

    private Statement expressionStatement() {
        var expression = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Statement.Expression(expression);
    }

    private Statement printStatement() {
        var expression = expression();
        consume(SEMICOLON, "Expect ';' after value.");
        return new Statement.Print(expression);
    }

    private Expression expression() {
        return assignment();
    }

    private Expression assignment() {
        Expression expr = equality();

        if (match(EQUAL)) {
            Token equals = previous();
            Expression value = assignment();

            if (expr instanceof Expression.Variable) {
                Token name = ((Expression.Variable) expr).name;
                return new Expression.Assign(name, value);
            }

            reportError(equals, "Invalid assignment target.");
        }

        return expr;
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
        if (match(TRUE)) return new Expression.Literal(true);
        if (match(FALSE)) return new Expression.Literal(false);
        if (match(NIL)) return new Expression.Literal(null);

        if (match(NUMBER, STRING)) {
            return new Expression.Literal(previous().literal);
        }

        if (match(LEFT_PAREN)) {
            var expression = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expression.Grouping(expression);
        }

        if (match(IDENTIFIER)) {
            return new Expression.Variable(previous());
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
        for (var errorReporter : errorReporters) {
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
        var expression = operand.get();

        while (match(types)) {
            Token operator = previous();
            Expression right = operand.get();
            expression = new Expression.Binary(expression, operator, right);
        }

        return expression;
    }

    private boolean match(Type... types) {
        for (var type : types) {
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