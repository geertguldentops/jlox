package be.guldentops.geert.lox.parser;

import be.guldentops.geert.lox.error.ErrorReporter;
import be.guldentops.geert.lox.error.SyntaxError;
import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.grammar.Statement;
import be.guldentops.geert.lox.lexer.Token;
import be.guldentops.geert.lox.lexer.Token.Type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import static be.guldentops.geert.lox.lexer.Token.Type.AND;
import static be.guldentops.geert.lox.lexer.Token.Type.BANG;
import static be.guldentops.geert.lox.lexer.Token.Type.BANG_EQUAL;
import static be.guldentops.geert.lox.lexer.Token.Type.CLASS;
import static be.guldentops.geert.lox.lexer.Token.Type.COMMA;
import static be.guldentops.geert.lox.lexer.Token.Type.DOT;
import static be.guldentops.geert.lox.lexer.Token.Type.ELSE;
import static be.guldentops.geert.lox.lexer.Token.Type.EOF;
import static be.guldentops.geert.lox.lexer.Token.Type.EQUAL;
import static be.guldentops.geert.lox.lexer.Token.Type.EQUAL_EQUAL;
import static be.guldentops.geert.lox.lexer.Token.Type.FALSE;
import static be.guldentops.geert.lox.lexer.Token.Type.FOR;
import static be.guldentops.geert.lox.lexer.Token.Type.FUN;
import static be.guldentops.geert.lox.lexer.Token.Type.GREATER;
import static be.guldentops.geert.lox.lexer.Token.Type.GREATER_EQUAL;
import static be.guldentops.geert.lox.lexer.Token.Type.IDENTIFIER;
import static be.guldentops.geert.lox.lexer.Token.Type.IF;
import static be.guldentops.geert.lox.lexer.Token.Type.LEFT_BRACE;
import static be.guldentops.geert.lox.lexer.Token.Type.LEFT_PAREN;
import static be.guldentops.geert.lox.lexer.Token.Type.LESS;
import static be.guldentops.geert.lox.lexer.Token.Type.LESS_EQUAL;
import static be.guldentops.geert.lox.lexer.Token.Type.MINUS;
import static be.guldentops.geert.lox.lexer.Token.Type.NIL;
import static be.guldentops.geert.lox.lexer.Token.Type.NUMBER;
import static be.guldentops.geert.lox.lexer.Token.Type.OR;
import static be.guldentops.geert.lox.lexer.Token.Type.PLUS;
import static be.guldentops.geert.lox.lexer.Token.Type.PRINT;
import static be.guldentops.geert.lox.lexer.Token.Type.RETURN;
import static be.guldentops.geert.lox.lexer.Token.Type.RIGHT_BRACE;
import static be.guldentops.geert.lox.lexer.Token.Type.RIGHT_PAREN;
import static be.guldentops.geert.lox.lexer.Token.Type.SEMICOLON;
import static be.guldentops.geert.lox.lexer.Token.Type.SLASH;
import static be.guldentops.geert.lox.lexer.Token.Type.STAR;
import static be.guldentops.geert.lox.lexer.Token.Type.STRING;
import static be.guldentops.geert.lox.lexer.Token.Type.SUPER;
import static be.guldentops.geert.lox.lexer.Token.Type.THIS;
import static be.guldentops.geert.lox.lexer.Token.Type.TRUE;
import static be.guldentops.geert.lox.lexer.Token.Type.VAR;
import static be.guldentops.geert.lox.lexer.Token.Type.WHILE;

class RecursiveDescentParser implements Parser {

    private static class ParseError extends RuntimeException {
    }

    private final List<Token> tokens;
    private int current = 0;

    private final List<ErrorReporter> errorReporters = new ArrayList<>();

    RecursiveDescentParser(List<Token> tokens) {
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
            if (match(CLASS)) return classDeclaration();
            if (match(FUN)) return function("function");
            if (match(VAR)) return variableDeclaration();

            return statement();
        } catch (ParseError error) {
            synchronize();
            return null;
        }
    }

    private Statement classDeclaration() {
        Token name = consume(IDENTIFIER, "expect class name.");

        Expression.Variable superclass = null;
        if (match(LESS)) {
            consume(IDENTIFIER, "expected super class name.");
            superclass = new Expression.Variable(previous());
        }

        consume(LEFT_BRACE, "expect '{' before class body.");

        List<Statement.Function> methods = new ArrayList<>();
        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            methods.add(function("method"));
        }

        consume(RIGHT_BRACE, "expect '}' after class body.");

        return new Statement.Class(name, superclass, methods);
    }

    private Statement.Function function(String function) {
        Token name = consume(IDENTIFIER, "expect " + function + " name.");

        consume(LEFT_PAREN, "expect '(' after " + function + " name.");
        List<Token> parameters = new ArrayList<>();
        if (!check(RIGHT_PAREN)) {
            do {
                if (parameters.size() >= 8) {
                    reportError(peek(), "cannot have more than 8 parameters.");
                }

                parameters.add(consume(IDENTIFIER, "expect parameter name."));
            } while (match(COMMA));
        }
        consume(RIGHT_PAREN, "expect ')' after parameters.");

        consume(LEFT_BRACE, "expect '{' before " + function + " body.");
        List<Statement> body = block();
        return new Statement.Function(name, parameters, body);
    }

    private Statement variableDeclaration() {
        Token name = consume(IDENTIFIER, "expected variable name");

        Expression initializer = null;
        if (match(EQUAL)) {
            initializer = expression();
        }

        consume(SEMICOLON, "expect ';' after variable declaration.");
        return new Statement.Variable(name, initializer);
    }

    private Statement statement() {
        if (match(FOR)) return forStatement();
        if (match(IF)) return ifStatement();
        if (match(PRINT)) return printStatement();
        if (match(RETURN)) return returnStatement();
        if (match(WHILE)) return whileStatement();
        if (match(LEFT_BRACE)) return new Statement.Block(block());

        return expressionStatement();
    }

    private Statement returnStatement() {
        Token keyword = previous();
        Expression value = null;
        if (!check(SEMICOLON)) {
            value = expression();
        }

        consume(SEMICOLON, "expect ';' after return value.");
        return new Statement.Return(keyword, value);
    }

    private Statement forStatement() {
        consume(LEFT_PAREN, "expect '(' after 'for'.");

        Statement initializer;
        if (match(SEMICOLON)) {
            initializer = null;
        } else if (match(VAR)) {
            initializer = variableDeclaration();
        } else {
            initializer = expressionStatement();
        }

        Expression condition = null;
        if (!check(SEMICOLON)) {
            condition = expression();
        }
        consume(SEMICOLON, "expect ';' after loop condition.");

        Expression increment = null;
        if (!check(RIGHT_PAREN)) {
            increment = expression();
        }
        consume(RIGHT_PAREN, "expect ')' after 'for increment'.");

        Statement body = statement();

        if (increment != null) {
            body = new Statement.Block(List.of(body, new Statement.Expression(increment)));
        }

        body = new Statement.While(condition != null ? condition : new Expression.Literal(true), body);

        if (initializer != null) {
            body = new Statement.Block(List.of(initializer, body));
        }

        return body;
    }

    private Statement ifStatement() {
        consume(LEFT_PAREN, "expect '(' after 'if'.");
        Expression condition = expression();
        consume(RIGHT_PAREN, "expect ')' after if condition.");

        Statement thenBranch = statement();
        Statement elseBranch = null;
        if (match(ELSE)) {
            elseBranch = statement();
        }

        return new Statement.If(condition, thenBranch, elseBranch);
    }

    private Statement printStatement() {
        var expression = expression();
        consume(SEMICOLON, "expect ';' after value.");
        return new Statement.Print(expression);
    }

    private Statement whileStatement() {
        consume(LEFT_PAREN, "expect '(' after 'while'.");
        Expression condition = expression();
        consume(RIGHT_PAREN, "expect ')' after while condition.");
        Statement body = statement();

        return new Statement.While(condition, body);
    }

    private List<Statement> block() {
        var statements = new ArrayList<Statement>();

        while (!check(RIGHT_BRACE) && !isAtEnd()) {
            statements.add(declaration());
        }

        consume(RIGHT_BRACE, "expect '}' after block.");
        return statements;
    }

    private Statement expressionStatement() {
        var expression = expression();
        consume(SEMICOLON, "expect ';' after value.");
        return new Statement.Expression(expression);
    }

    private Expression expression() {
        return assignment();
    }

    private Expression assignment() {
        var expression = or();

        if (match(EQUAL)) {
            Token equals = previous();
            Expression value = assignment();

            if (expression instanceof Expression.Variable variable) {
                Token name = variable.name();
                return new Expression.Assign(name, value);
            } else if (expression instanceof Expression.Get get) {
                return new Expression.Set(get.object(), get.name(), value);
            }

            reportError(equals, "invalid assignment target.");
        }

        return expression;
    }

    private Expression or() {
        var expression = and();

        while (match(OR)) {
            Token operator = previous();
            Expression right = and();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
    }

    private Expression and() {
        var expression = equality();

        while (match(AND)) {
            Token operator = previous();
            Expression right = equality();
            expression = new Expression.Logical(expression, operator, right);
        }

        return expression;
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

        return call();
    }

    private Expression call() {
        var expression = primary();

        while (true) {
            if (match(LEFT_PAREN)) {
                expression = finishCall(expression);
            } else if (match(DOT)) {
                Token name = consume(IDENTIFIER, "expect property name after '.'.");
                expression = new Expression.Get(expression, name);
            } else {
                break;
            }
        }

        return expression;
    }

    private Expression finishCall(Expression callee) {
        var arguments = new ArrayList<Expression>();

        if (!check(RIGHT_PAREN)) {
            do {
                if (arguments.size() >= 8) {
                    reportError(peek(), "cannot have more than 8 arguments.");
                }

                arguments.add(expression());
            } while (match(COMMA));
        }
        Token paren = consume(RIGHT_PAREN, "expect ')' after arguments.");

        return new Expression.Call(callee, paren, arguments);
    }

    private Expression primary() {
        if (match(TRUE)) return new Expression.Literal(true);
        if (match(FALSE)) return new Expression.Literal(false);
        if (match(NIL)) return new Expression.Literal(null);

        if (match(NUMBER, STRING)) return new Expression.Literal(previous().literal());

        if (match(LEFT_PAREN)) {
            var expression = expression();
            consume(RIGHT_PAREN, "expect ')' after expression.");
            return new Expression.Grouping(expression);
        }

        if (match(THIS)) return new Expression.This(previous());

        if (match(SUPER)) {
            Token keyword = previous();
            consume(DOT, "expect '.' after 'super'.");
            Token method = consume(IDENTIFIER, "expect superclass method name.");
            return new Expression.Super(keyword, method);
        }

        if (match(IDENTIFIER)) return new Expression.Variable(previous());

        throw error(peek(), "expect expression.");
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
            if (token.type() == EOF) {
                errorReporter.handle(new SyntaxError(token.line(), "end", message));
            } else {
                errorReporter.handle(new SyntaxError(token.line(), token.lexeme(), message));
            }
        }
    }

    private void synchronize() {
        advance();

        while (!isAtEnd()) {
            if (previous().type() == SEMICOLON) return;

            switch (peek().type()) {
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
        return peek().type() == type;
    }

    private Token peek() {
        return tokens.get(current);
    }

    private boolean isAtEnd() {
        return peek().type() == EOF;
    }

    private Token advance() {
        if (!isAtEnd()) current++;
        return previous();
    }

    private Token previous() {
        return tokens.get(current - 1);
    }
}
