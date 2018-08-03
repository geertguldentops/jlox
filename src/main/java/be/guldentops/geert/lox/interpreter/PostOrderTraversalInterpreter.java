package be.guldentops.geert.lox.interpreter;

import be.guldentops.geert.lox.error.ErrorReporter;
import be.guldentops.geert.lox.error.RuntimeError;
import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.grammar.Statement;
import be.guldentops.geert.lox.lexer.Token;

import java.util.ArrayList;
import java.util.List;

import static be.guldentops.geert.lox.lexer.Token.Type.OR;
import static java.util.stream.Collectors.toList;

class PostOrderTraversalInterpreter implements Interpreter, Expression.Visitor<Object>, Statement.Visitor<Void> {

    private Environment environment;

    private final List<ErrorReporter> errorReporters = new ArrayList<>();

    PostOrderTraversalInterpreter(Environment globals) {
        this.environment = globals;
    }

    @Override
    public void addErrorReporter(ErrorReporter errorReporter) {
        this.errorReporters.add(errorReporter);
    }

    @Override
    public Object interpret(Expression expression) {
        try {
            return evaluate(expression);
        } catch (RuntimeError e) {
            for (var errorReporter : errorReporters) {
                errorReporter.handle(e);
            }

            return null;
        }
    }

    @Override
    public void interpret(List<Statement> statements) {
        try {
            for (var statement : statements) {
                execute(statement);
            }
        } catch (RuntimeError e) {
            for (var errorReporter : errorReporters) {
                errorReporter.handle(e);
            }
        }
    }

    private void execute(Statement statement) {
        statement.accept(this);
    }

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        executeBlock(statement.statements, Environment.createLocal(environment));
        return null;
    }

    @Override
    public void executeBlock(List<Statement> statements, Environment localEnvironment) {
        Environment previous = this.environment;

        try {
            this.environment = localEnvironment;

            for (var statement : statements) {
                execute(statement);
            }
        } finally {
            this.environment = previous;
        }
    }

    @Override
    public Void visitExpressionStatement(Statement.Expression statement) {
        evaluate(statement.expression);
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.Function statement) {
        var function = new LoxFunction(statement, environment);
        environment.define(statement.name, function);
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
        if (isTruthy(evaluate(statement.condition))) {
            execute(statement.thenBranch);
        } else if (statement.elseBranch != null) {
            execute(statement.elseBranch);
        }

        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        var value = evaluate(statement.expression);
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        Object value = null;
        if (statement.value != null) value = evaluate(statement.value);

        throw new Return(value);
    }

    @Override
    public Void visitVariableStatement(Statement.Variable statement) {
        Object value = null;
        if (statement.initializer != null) {
            value = evaluate(statement.initializer);
        }

        environment.define(statement.name, value);
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        while (isTruthy(evaluate(statement.condition))) {
            execute(statement.body);
        }

        return null;
    }

    private String stringify(Object value) {
        if (value == null) return "nil";

        // Hack. Work around Java adding ".0" to integer-valued doubles.
        if (value instanceof Double) {
            var text = value.toString();
            if (text.endsWith(".0")) {
                text = text.substring(0, text.length() - 2);
            }
            return text;
        }

        return value.toString();
    }

    @Override
    public Object visitAssignExpression(Expression.Assign expression) {
        Object value = evaluate(expression.value);

        environment.assign(expression.name, value);
        return value;
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression) {
        var left = evaluate(expression.left);
        var right = evaluate(expression.right);

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
    public Object visitCallExpression(Expression.Call expression) {
        var callee = evaluate(expression.callee);

        if (!(callee instanceof LoxCallable)) {
            throw new RuntimeError(expression.paren, "Can only call functions and classes.");
        }

        var arguments = expression.arguments.stream()
                .map(this::evaluate)
                .collect(toList());

        LoxCallable function = (LoxCallable) callee;

        if (arguments.size() != function.arity()) {
            throw new RuntimeError(
                    expression.paren,
                    String.format("Expected %d argument(s) but got %d.", function.arity(), arguments.size())
            );
        }

        return function.call(this, arguments);
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
    public Object visitLogicalExpression(Expression.Logical expression) {
        var left = evaluate(expression.left);

        if (expression.operator.type == OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expression.right);
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression) {
        var right = evaluate(expression.right);

        switch (expression.operator.type) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expression.operator, right);
                return -(double) right;
        }

        return null;
    }

    @Override
    public Object visitVariableExpression(Expression.Variable expression) {
        return environment.get(expression.name);
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
        if (d == 0) throw new RuntimeError(operator, "Can not divide by zero!");
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }
}
