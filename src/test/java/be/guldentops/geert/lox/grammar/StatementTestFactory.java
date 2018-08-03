package be.guldentops.geert.lox.grammar;

import be.guldentops.geert.lox.lexer.api.Token;

import java.util.List;

public class StatementTestFactory {

    private StatementTestFactory() {
    }

    public static Statement.Block blockStatement(Statement... statements) {
        return new Statement.Block(List.of(statements));
    }

    public static Statement.Expression expressionStatement(Expression expression) {
        return new Statement.Expression(expression);
    }

    public static Statement.If _if(Expression condition, Statement thenBranch, Statement elseBranch) {
        return new Statement.If(condition, thenBranch, elseBranch);
    }

    public static Statement.Print print(Expression expression) {
        return new Statement.Print(expression);
    }

    public static Statement.Variable uninitializedVariableDeclaration(Token name) {
        return new Statement.Variable(name, null);
    }

    public static Statement.Variable variableDeclaration(Token name, Expression initializer) {
        return new Statement.Variable(name, initializer);
    }

    public static Statement.While _while(Expression condition, Statement body) {
        return new Statement.While(condition, body);
    }
}
