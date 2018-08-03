package be.guldentops.geert.lox.interpreter.impl;

import be.guldentops.geert.lox.error.api.ErrorReporter;
import be.guldentops.geert.lox.error.api.RuntimeError;
import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.interpreter.api.Interpreter;
import be.guldentops.geert.lox.lexer.api.Token;

import java.util.ArrayList;
import java.util.List;

public class PostOrderTraversalInterpreter implements Interpreter, Expression.Visitor<Object> {

    private final List<ErrorReporter> errorReporters = new ArrayList<>();

    @Override
    public void addErrorReporter(ErrorReporter errorReporter) {
        this.errorReporters.add(errorReporter);
    }

    @Override
    public Object interpret(Expression expression) {
        if (expression == null) throw new IllegalArgumentException("Expression should never be null!");

        try {
            return evaluate(expression);
        } catch (RuntimeError e) {
            for (ErrorReporter errorReporter : errorReporters) {
                errorReporter.handle(e);
            }

            return null;
        }
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression) {
        Object left = evaluate(expression.left);
        Object right = evaluate(expression.right);

        switch (expression.operator.type) {
            case GREATER:
                checkNumberOperands(expression.operator, left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expression.operator, left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expression.operator, left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expression.operator, left, right);
                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperands(expression.operator, left, right);
                return (double) left - (double) right;
            case PLUS:
                if (left instanceof Double && right instanceof Double) {
                    return (double) left + (double) right;
                }

                if (left instanceof String && right instanceof String) {
                    return (String) left + (String) right;
                }

                throw new RuntimeError(expression.operator, "Operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expression.operator, left, right);
                checkNull(expression.operator, (double) right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expression.operator, left, right);
                return (double) left * (double) right;
            case BANG_EQUAL:
                return !isEqual(left, right);
            case EQUAL_EQUAL:
                return isEqual(left, right);
        }

        return null;
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expression) {
        return evaluate(expression.expression);
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        return expression.value;
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression) {
        Object right = evaluate(expression.right);

        switch (expression.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expression.operator, right);
                return -(double) right;
        }

        return null;
    }

    private Object evaluate(Expression expression) {
        return expression.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean) return (boolean) object;

        return true;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;

        throw new RuntimeError(operator, "Operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "Operands must be numbers.");
    }

    private void checkNull(Token operator, double d) {
        if (d != 0) return;

        throw new RuntimeError(operator, "Can not divide by zero!");
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }
}
