package be.guldentops.geert.lox.tools;

import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.grammar.Statement;

public class AbstractSyntaxTreePrinter implements Expression.Visitor<String>, Statement.Visitor<String> {

    public String print(Expression expression) {
        return expression.accept(this);
    }

    public String print(Statement statement) {
        return statement.accept(this);
    }

    @Override
    public String visitAssignExpression(Expression.Assign expression) {
        return parenthesize(" = " + expression.name.lexeme, expression.value);
    }

    @Override
    public String visitBinaryExpression(Expression.Binary expression) {
        return parenthesize(expression.operator.lexeme, expression.left, expression.right);
    }

    @Override
    public String visitGroupingExpression(Expression.Grouping expression) {
        return parenthesize("group", expression.expression);
    }

    @Override
    public String visitLiteralExpression(Expression.Literal expression) {
        if (expression.value == null) return "nil";
        return expression.value.toString();
    }

    @Override
    public String visitUnaryExpression(Expression.Unary expression) {
        return parenthesize(expression.operator.lexeme, expression.right);
    }

    @Override
    public String visitVariableExpression(Expression.Variable expression) {
        return expression.name.lexeme;
    }

    @Override
    public String visitBlockStatement(Statement.Block block) {
        StringBuilder sb = new StringBuilder("(block ");

        for (Statement statement : block.statements) {
            sb.append(print(statement));
        }

        sb.append(")");

        return sb.toString();
    }

    @Override
    public String visitExpressionStatement(Statement.Expression statement) {
        return parenthesize(";", statement.expression);
    }

    @Override
    public String visitPrintStatement(Statement.Print statement) {
        return parenthesize("print", statement.expression);
    }

    @Override
    public String visitVariableStatement(Statement.Variable statement) {
        if (statement.initializer == null) return parenthesize("var " + statement.name.lexeme);

        return parenthesize("var " + statement.name.lexeme + " =", statement.initializer);
    }

    private String parenthesize(String name, Expression... expressions) {
        var builder = new StringBuilder();

        builder.append("(").append(name);
        for (var expression : expressions) {
            builder.append(" ");
            builder.append(expression.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }
}
