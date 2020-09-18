package be.guldentops.geert.lox.semantic.analysis;

import be.guldentops.geert.lox.error.ErrorReporter;
import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.grammar.Statement;
import be.guldentops.geert.lox.lexer.Token;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

class VariableResolver implements Resolver, Expression.Visitor<Void>, Statement.Visitor<Void> {

    private final Stack<Map<String, Boolean>> scopes = new Stack<>();

    private FunctionType currentFunction = FunctionType.NONE;
    private ClassType currentClass = ClassType.NONE;

    private final ResolutionAnalyzer resolutionAnalyzer;

    private final List<ErrorReporter> errorReporters = new ArrayList<>();

    VariableResolver(ResolutionAnalyzer resolutionAnalyzer) {
        this.resolutionAnalyzer = resolutionAnalyzer;
    }

    @Override
    public void addErrorReporter(ErrorReporter errorReporter) {
        this.errorReporters.add(errorReporter);
    }

    @Override
    public void resolve(List<Statement> statements) {
        if (statements == null) throw new IllegalArgumentException("Can not resolve null statements");

        for (var statement : statements) {
            resolve(statement);
        }
    }

    @Override
    public Void visitAssignExpression(Expression.Assign expression) {
        resolve(expression.value());
        resolveLocal(expression, expression.name());
        return null;
    }

    @Override
    public Void visitBinaryExpression(Expression.Binary expression) {
        resolve(expression.left());
        resolve(expression.right());
        return null;
    }

    @Override
    public Void visitCallExpression(Expression.Call expression) {
        resolve(expression.callee());

        for (var argument : expression.arguments()) {
            resolve(argument);
        }

        return null;
    }

    @Override
    public Void visitGetExpression(Expression.Get expression) {
        resolve(expression.object());
        return null;
    }

    @Override
    public Void visitGroupingExpression(Expression.Grouping expression) {
        resolve(expression.expression());
        return null;
    }

    @Override
    public Void visitLiteralExpression(Expression.Literal expression) {
        return null;
    }

    @Override
    public Void visitLogicalExpression(Expression.Logical expression) {
        resolve(expression.left());
        resolve(expression.right());
        return null;
    }

    @Override
    public Void visitSetExpression(Expression.Set expression) {
        resolve(expression.value());
        resolve(expression.object());

        return null;
    }

    @Override
    public Void visitSuperExpression(Expression.Super expression) {
        if (currentClass == ClassType.NONE) {
            reportError(expression.keyword(), "cannot use 'super' outside of a class.");
        } else if (currentClass != ClassType.SUBCLASS) {
            reportError(expression.keyword(), "cannot use 'super' in a class with no superclass.");
        }

        resolveLocal(expression, expression.keyword());
        return null;
    }

    @Override
    public Void visitThisExpression(Expression.This expression) {
        if (currentClass == ClassType.NONE) {
            reportError(expression.keyword(), "cannot use 'this' outside of a class.");
        } else {
            resolveLocal(expression, expression.keyword());
        }

        return null;
    }

    @Override
    public Void visitUnaryExpression(Expression.Unary expression) {
        resolve(expression.right());
        return null;
    }

    @Override
    public Void visitVariableExpression(Expression.Variable expression) {
        if (!scopes.isEmpty() && scopes.peek().get(expression.name().lexeme()) == Boolean.FALSE) {
            reportError(expression.name(), "cannot read local variable in its own initializer.");
        }

        resolveLocal(expression, expression.name());
        return null;
    }

    private void reportError(Token token, String message) {
        for (var errorReporter : errorReporters) {
            errorReporter.handle(new SemanticError(token, message));
        }
    }

    private void resolveLocal(Expression expression, Token name) {
        for (var i = scopes.size() - 1; i >= 0; i--) {
            if (scopes.get(i).containsKey(name.lexeme())) {
                resolutionAnalyzer.resolve(expression, scopes.size() - 1 - i);
                return;
            }
        }

        // Not found. Assume it is global.
    }

    @Override
    public Void visitBlockStatement(Statement.Block statement) {
        beginScope();
        resolve(statement.statements());
        endScope();
        return null;
    }

    @Override
    public Void visitClassStatement(Statement.Class statement) {
        var enclosingClass = currentClass;
        currentClass = ClassType.CLASS;

        declare(statement.name());

        if (statement.superclass() != null) {
            currentClass = ClassType.SUBCLASS;
            resolve(statement.superclass());
        }

        define(statement.name());

        if (statement.superclass() != null) {
            beginScope();
            scopes.peek().put("super", true);
        }

        beginScope();
        scopes.peek().put("this", true);

        for (Statement.Function method : statement.methods()) {
            var declaration = analyseDeclaration(method);
            resolveFunction(method, declaration);
        }

        endScope();

        if (statement.superclass() != null) {
            endScope();
        }

        currentClass = enclosingClass;
        return null;
    }

    private FunctionType analyseDeclaration(Statement.Function method) {
        if (method.name().lexeme().equals("init")) {
            return FunctionType.INITIALIZER;
        } else {
            return FunctionType.METHOD;
        }
    }

    private void beginScope() {
        scopes.push(new HashMap<>());
    }

    private void resolve(Statement statement) {
        statement.accept(this);
    }

    private void endScope() {
        scopes.pop();
    }

    @Override
    public Void visitExpressionStatement(Statement.Expression statement) {
        resolve(statement.expression());
        return null;
    }

    @Override
    public Void visitFunctionStatement(Statement.Function statement) {
        declare(statement.name());
        define(statement.name());

        resolveFunction(statement, FunctionType.FUNCTION);
        return null;
    }

    private void resolveFunction(Statement.Function function, FunctionType type) {
        var enclosingFunction = currentFunction;
        currentFunction = type;

        beginScope();
        for (var parameter : function.parameters()) {
            declare(parameter);
            define(parameter);
        }
        resolve(function.body());
        endScope();

        currentFunction = enclosingFunction;
    }

    @Override
    public Void visitIfStatement(Statement.If statement) {
        resolve(statement.condition());
        resolve(statement.thenBranch());
        if (statement.elseBranch() != null) resolve(statement.elseBranch());
        return null;
    }

    @Override
    public Void visitPrintStatement(Statement.Print statement) {
        resolve(statement.expression());
        return null;
    }

    @Override
    public Void visitReturnStatement(Statement.Return statement) {
        if (currentFunction == FunctionType.NONE) {
            reportError(statement.keyword(), "cannot return from top-level code.");
        }
        if (currentFunction == FunctionType.INITIALIZER) {
            reportError(statement.keyword(), "cannot return a value from an initializer.");
        }

        if (statement.value() != null) {
            resolve(statement.value());
        }

        return null;
    }

    @Override
    public Void visitVariableStatement(Statement.Variable statement) {
        declare(statement.name());
        if (statement.initializer() != null) {
            resolve(statement.initializer());
        }
        define(statement.name());
        return null;
    }

    private void declare(Token name) {
        if (scopes.isEmpty()) return;

        Map<String, Boolean> scope = scopes.peek();
        if (scope.containsKey(name.lexeme())) {
            reportError(name, "variable with this name already declared in this scope.");
        }
        scope.put(name.lexeme(), false);
    }

    private void resolve(Expression expression) {
        expression.accept(this);
    }

    private void define(Token name) {
        if (scopes.isEmpty()) return;
        scopes.peek().put(name.lexeme(), true);
    }

    @Override
    public Void visitWhileStatement(Statement.While statement) {
        resolve(statement.condition());
        resolve(statement.body());
        return null;
    }

    private enum FunctionType {
        NONE,
        FUNCTION,
        METHOD,
        INITIALIZER,
    }

    private enum ClassType {
        NONE,
        CLASS,
        SUBCLASS
    }
}
