package be.guldentops.geert.lox.grammar;

import be.guldentops.geert.lox.lexer.Token;
import be.guldentops.geert.lox.lexer.TokenObjectMother;

import java.util.List;

import static be.guldentops.geert.lox.lexer.TokenObjectMother.identifier;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.rightParen;

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

    public static Expression.Variable variable(String name) {
        return new Expression.Variable(identifier(name));
    }

    public static Expression.Assign assign(String name, Expression value) {
        return new Expression.Assign(identifier(name), value);
    }

    public static Expression.Logical logical(Expression left, Token operator, Expression right) {
        return new Expression.Logical(left, operator, right);
    }

    public static Expression.Call call(String functionName, Expression... arguments) {
        return call(variable(functionName), arguments);
    }

    public static Expression.Call call(Expression callee, Expression... arguments) {
        return new Expression.Call(callee, rightParen(), List.of(arguments));
    }

    public static Expression.Get get(Expression object, Token name) {
        return new Expression.Get(object, name);
    }

    public static Expression.Set set(Expression object, Token name, Expression value) {
        return new Expression.Set(object, name, value);
    }

    public static Expression.This _this() {
        return new Expression.This(TokenObjectMother._this());
    }

    public static Expression.Super _super(Token method) {
        return new Expression.Super(TokenObjectMother._super(), method);
    }
}