package be.guldentops.geert.lox.interpreter;

import be.guldentops.geert.lox.error.ErrorReporter;
import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.grammar.Statement;
import be.guldentops.geert.lox.lexer.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static be.guldentops.geert.lox.lexer.Token.Type.OR;
import static java.util.stream.Collectors.toList;

class PostOrderTraversalInterpreter implements Interpreter, Expression.Visitor<Object>, Statement.Visitor<Void> {

    private final Environment globals;
    private final Map<Expression, Integer> locals = new HashMap<>();
    private Environment environment;

    private final List<ErrorReporter> errorReporters = new ArrayList<>();

    PostOrderTraversalInterpreter(Environment globals) {
        this.globals = globals;
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
        executeBlock(statement.statements(), Environment.createLocal(environment));
        return null;
    }

    @Override
    public Void visitClassStatement(Statement.Class statement) {
        Object superclass = null;
        if (statement.superclass() != null) {
            superclass = evaluate(statement.superclass());

            if (!(superclass instanceof LoxClass)) {
                throw new RuntimeError(statement.superclass().name(), "superclass must be a class.");
            }
        }

        environment.define(statement.name(), null);

        if (superclass != null) {
            environment = Environment.createLocal(environment);
            environment.define("super", superclass);
        }

        var methods = new HashMap<String, LoxFunction>();
        for (var method : statement.methods()) {
            var function = createFunction(method);
            methods.put(method.name().lexeme(), function);
        }

        LoxClass clazz = new LoxClass(statement.name().lexeme(), (LoxClass) superclass, methods);

        if (superclass != null) {
            environment = environment.enclosing;
        }

        environment.assign(statement.name(), clazz);

        return null;
    }

    private LoxFunction createFunction(Statement.Function method) {
        if (method.name().lexeme().equals("init")) {
            return LoxFunction.createInitFunction(method, environment);
        } else {
            return LoxFunction.createFunction(method, environment);
        }
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
    public void resolve(Expression expression, int depth) {
        locals.put(expression, depth);
    }

    @Override
    public Void visitExpressionStatement(Statement.Expression statement) {
        evaluate(statement.expression());
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.Function statement) {
        var function = LoxFunction.createFunction(statement, environment);
        environment.define(statement.name(), function);
        return null;
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
        if (isTruthy(evaluate(statement.condition()))) {
            execute(statement.thenBranch());
        } else if (statement.elseBranch() != null) {
            execute(statement.elseBranch());
        }

        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        var value = evaluate(statement.expression());
        System.out.println(stringify(value));
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        Object value = null;
        if (statement.value() != null) value = evaluate(statement.value());

        throw new Return(value);
    }

    @Override
    public Void visitVariableStatement(Statement.Variable statement) {
        Object value = null;
        if (statement.initializer() != null) {
            value = evaluate(statement.initializer());
        }

        environment.define(statement.name(), value);
        return null;
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        while (isTruthy(evaluate(statement.condition()))) {
            execute(statement.body());
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
        Object value = evaluate(expression.value());

        Integer distance = locals.get(expression);
        if (distance != null) {
            environment.assignAt(distance, expression.name(), value);
        } else {
            globals.assign(expression.name(), value);
        }

        return value;
    }

    @Override
    public Object visitBinaryExpression(Expression.Binary expression) {
        var left = evaluate(expression.left());
        var right = evaluate(expression.right());

        switch (expression.operator().type()) {
            case GREATER:
                checkNumberOperands(expression.operator(), left, right);
                return (double) left > (double) right;
            case GREATER_EQUAL:
                checkNumberOperands(expression.operator(), left, right);
                return (double) left >= (double) right;
            case LESS:
                checkNumberOperands(expression.operator(), left, right);
                return (double) left < (double) right;
            case LESS_EQUAL:
                checkNumberOperands(expression.operator(), left, right);
                return (double) left <= (double) right;
            case MINUS:
                checkNumberOperands(expression.operator(), left, right);
                return (double) left - (double) right;
            case PLUS:
                if (left instanceof Double l && right instanceof Double r) {
                    return l + r;
                }

                if (left instanceof String l && right instanceof String r) {
                    return l + r;
                }

                throw new RuntimeError(expression.operator(), "operands must be two numbers or two strings.");
            case SLASH:
                checkNumberOperands(expression.operator(), left, right);
                checkNull(expression.operator(), (double) right);
                return (double) left / (double) right;
            case STAR:
                checkNumberOperands(expression.operator(), left, right);
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
        var callee = evaluate(expression.callee());

        if (callee instanceof LoxCallable function) {
            var arguments = expression.arguments().stream()
                    .map(this::evaluate)
                    .collect(toList());

            if (arguments.size() != function.arity()) {
                throw new RuntimeError(
                        expression.paren(),
                        String.format("expected %d argument(s) but got %d.", function.arity(), arguments.size())
                );
            }

            return function.call(this, arguments);
        } else {
            throw new RuntimeError(expression.paren(), "can only call functions and classes.");
        }
    }

    @Override
    public Object visitGetExpression(Expression.Get expression) {
        Object object = evaluate(expression.object());
        if (object instanceof LoxInstance instance) {
            return instance.get(expression.name());
        }

        throw new RuntimeError(expression.name(), "only instances have properties.");
    }

    @Override
    public Object visitGroupingExpression(Expression.Grouping expression) {
        return evaluate(expression.expression());
    }

    @Override
    public Object visitLiteralExpression(Expression.Literal expression) {
        return expression.value();
    }

    @Override
    public Object visitLogicalExpression(Expression.Logical expression) {
        var left = evaluate(expression.left());

        if (expression.operator().type() == OR) {
            if (isTruthy(left)) return left;
        } else {
            if (!isTruthy(left)) return left;
        }

        return evaluate(expression.right());
    }

    @Override
    public Object visitSetExpression(Expression.Set expression) {
        Object object = evaluate(expression.object());

        if (object instanceof LoxInstance instance) {
            Object value = evaluate(expression.value());
            instance.set(expression.name(), value);
            return value;
        }

        throw new RuntimeError(expression.name(), "only instances have fields.");
    }

    @Override
    public Object visitSuperExpression(Expression.Super expression) {
        int distance = locals.get(expression);

        var superclass = (LoxClass) environment.getAt(distance, "super");

        // "this" is always one level nearer than "super"'s environment.
        var object = (LoxInstance) environment.getAt(distance - 1, "this");

        LoxFunction method = superclass.findMethod(object, expression.method().lexeme());

        if (method == null) {
            throw new RuntimeError(expression.method(), "undefined property.");
        }

        return method;
    }

    @Override
    public Object visitThisExpression(Expression.This expression) {
        return lookUpVariable(expression.keyword(), expression);
    }

    @Override
    public Object visitUnaryExpression(Expression.Unary expression) {
        var right = evaluate(expression.right());

        switch (expression.operator().type()) {
            case BANG:
                return !isTruthy(right);
            case MINUS:
                checkNumberOperand(expression.operator(), right);
                return -(double) right;
        }

        return null;
    }

    @Override
    public Object visitVariableExpression(Expression.Variable expression) {
        return lookUpVariable(expression.name(), expression);
    }

    private Object lookUpVariable(Token name, Expression expression) {
        var distance = locals.get(expression);

        if (distance != null) {
            return environment.getAt(distance, name.lexeme());
        } else {
            return globals.get(name);
        }
    }

    private Object evaluate(Expression expression) {
        return expression.accept(this);
    }

    private boolean isTruthy(Object object) {
        if (object == null) return false;
        if (object instanceof Boolean bool) return bool;

        return true;
    }

    private void checkNumberOperand(Token operator, Object operand) {
        if (operand instanceof Double) return;

        throw new RuntimeError(operator, "operand must be a number.");
    }

    private void checkNumberOperands(Token operator, Object left, Object right) {
        if (left instanceof Double && right instanceof Double) return;

        throw new RuntimeError(operator, "operands must be numbers.");
    }

    private void checkNull(Token operator, double d) {
        if (d == 0) throw new RuntimeError(operator, "can not divide by zero!");
    }

    private boolean isEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null) return false;

        return a.equals(b);
    }
}
