package be.guldentops.geert.lox.grammar;

import be.guldentops.geert.lox.lexer.api.Token;

public class ExpressionTestFactory {

    private ExpressionTestFactory() {
    }

    public static Expression.Literal literal(Object value) {
        return new Expression.Literal(value);
    }

    public static Expression.Grouping grouping(Expression expression) {
        return new Expression.Grouping(expression);
    }

    public static Expression.Unary unary(Token operator, Expression right) {
        return new Expression.Unary(operator, right);
    }

    public static Expression.Binary binary(Expression left, Token operator, Expression right) {
        return new Expression.Binary(left, operator, right);
    }

    public static Expression.Variable variable(Token token) {
        return new Expression.Variable(token);
    }

    public static Expression.Assign assign(Token name, Expression value) {
        return new Expression.Assign(name, value);
    }
}