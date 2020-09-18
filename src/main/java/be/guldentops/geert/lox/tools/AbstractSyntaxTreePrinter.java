package be.guldentops.geert.lox.tools;

import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.grammar.Statement;
import be.guldentops.geert.lox.lexer.Token;

import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

class AbstractSyntaxTreePrinter implements Expression.Visitor<String>, Statement.Visitor<String> {

    String print(Expression expression) {
        return expression.accept(this);
    }

    String print(Statement statement) {
        return statement.accept(this);
    }

    @Override
    public String visitAssignExpression(Expression.Assign expression) {
        return parenthesize(" = " + expression.name().lexeme(), expression.value());
    }

    @Override
    public String visitBinaryExpression(Expression.Binary expression) {
        return parenthesize(expression.operator().lexeme(), expression.left(), expression.right());
    }

    @Override
    public String visitCallExpression(Expression.Call expression) {
        return "(call " + expression.callee().accept(this) + " (" + toSpaceSeparatedText(expression.arguments().toArray(new Expression[]{})) + "))";
    }

    @Override
    public String visitGetExpression(Expression.Get expression) {
        return "(. " + print(expression.object()) + " " + expression.name().lexeme() + ")";
    }

    @Override
    public String visitGroupingExpression(Expression.Grouping expression) {
        return parenthesize("group", expression.expression());
    }

    @Override
    public String visitLiteralExpression(Expression.Literal expression) {
        if (expression.value() == null) return "nil";
        return expression.value().toString();
    }

    @Override
    public String visitLogicalExpression(Expression.Logical expression) {
        return parenthesize(expression.operator().lexeme(), expression.left(), expression.right());
    }

    @Override
    public String visitSetExpression(Expression.Set expression) {
        return "(. " + print(expression.object()) + " " + expression.name().lexeme() + " " + print(expression.value()) + ")";
    }

    @Override
    public String visitSuperExpression(Expression.Super expression) {
        return "(" + expression.keyword().lexeme() + " " + expression.method().lexeme() + ")";
    }

    @Override
    public String visitThisExpression(Expression.This expression) {
        return expression.keyword().lexeme();
    }

    @Override
    public String visitUnaryExpression(Expression.Unary expression) {
        return parenthesize(expression.operator().lexeme(), expression.right());
    }

    @Override
    public String visitVariableExpression(Expression.Variable expression) {
        return expression.name().lexeme();
    }

    @Override
    public String visitBlockStatement(Statement.Block block) {
        var sb = new StringBuilder("(block ");

        for (var statement : block.statements()) {
            sb.append(print(statement));
        }

        sb.append(")");

        return sb.toString();
    }

    @Override
    public String visitClassStatement(Statement.Class statement) {
        var sb = new StringBuilder();
        sb.append("(class ").append(statement.name().lexeme());
        appendSuperClass(sb, statement);

        for (Statement.Function method : statement.methods()) {
            sb.append(" ").append(print(method));
        }

        sb.append(")");
        return sb.toString();
    }

    private void appendSuperClass(StringBuilder sb, Statement.Class statement) {
        if (statement.superclass() != null)
            sb.append("(superClass ").append(print(statement.superclass())).append(")");
    }

    @Override
    public String visitExpressionStatement(Statement.Expression statement) {
        return parenthesize(";", statement.expression());
    }

    @Override
    public String visitFunctionStatement(Statement.Function statement) {
        return "(fun " + statement.name().lexeme() + " (" + toSpaceSeparatedText(statement.parameters().toArray(new Token[]{})) + ") "
                + toSpaceSeparatedText(statement.body().toArray(new Statement[]{}))
                + ")";
    }

    @Override
    public String visitIfStatement(Statement.If statement) {
        if (statement.elseBranch() == null) {
            return "if " + print(statement.condition()) + " " + print(statement.thenBranch());
        } else {
            return "if-else " + print(statement.condition()) + " " + print(statement.thenBranch()) + " " + print(statement.elseBranch());
        }
    }

    @Override
    public String visitPrintStatement(Statement.Print statement) {
        return parenthesize("print", statement.expression());
    }

    @Override
    public String visitReturnStatement(Statement.Return statement) {
        if (statement.value() == null) return "(return)";
        return parenthesize("return", statement.value());
    }

    @Override
    public String visitVariableStatement(Statement.Variable statement) {
        if (statement.initializer() == null) return "var " + statement.name().lexeme();

        return "var " + statement.name().lexeme() + " = " + statement.initializer().accept(this);
    }

    @Override
    public String visitWhileStatement(Statement.While statement) {
        return "while " + print(statement.condition()) + " " + print(statement.body());
    }

    private String parenthesize(String name, Expression... expressions) {
        return "(" + name + " " + toSpaceSeparatedText(expressions) + ")";
    }

    private String toSpaceSeparatedText(Token... tokens) {
        return Stream.of(tokens)
                .map(token -> token.lexeme())
                .collect(joining(" "));
    }

    private String toSpaceSeparatedText(Expression... expressions) {
        return Stream.of(expressions)
                .map(argument -> argument.accept(this))
                .collect(joining(" "));
    }

    private String toSpaceSeparatedText(Statement... statements) {
        return Stream.of(statements)
                .map(statement -> statement.accept(this))
                .collect(joining(" "));
    }
}
