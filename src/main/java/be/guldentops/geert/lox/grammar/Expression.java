package be.guldentops.geert.lox.grammar;

import be.guldentops.geert.lox.lexer.Token;

import java.util.List;

public sealed interface Expression {

    <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {

        R visitAssignExpression(Assign expression);

        R visitBinaryExpression(Binary expression);

        R visitCallExpression(Call expression);

        R visitGetExpression(Get expression);

        R visitGroupingExpression(Grouping expression);

        R visitLiteralExpression(Literal expression);

        R visitLogicalExpression(Logical expression);

        R visitSetExpression(Set expression);

        R visitSuperExpression(Super expression);

        R visitThisExpression(This expression);

        R visitUnaryExpression(Unary expression);

        R visitVariableExpression(Variable expression);
    }

    record Assign(Token name, Expression value) implements Expression {

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpression(this);
        }
    }

    record Binary(Expression left, Token operator, Expression right) implements Expression {

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpression(this);
        }
    }

    record Call(Expression callee, Token paren, List<Expression> arguments) implements Expression {

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpression(this);
        }
    }

    record Get(Expression object, Token name) implements Expression {

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpression(this);
        }
    }

    record Grouping(Expression expression) implements Expression {

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpression(this);
        }
    }

    record Literal(Object value) implements Expression {

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpression(this);
        }
    }

    record Logical(Expression left, Token operator, Expression right) implements Expression {

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpression(this);
        }
    }

    record Set(Expression object, Token name, Expression value) implements Expression {

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpression(this);
        }
    }

    record Super(Token keyword, Token method) implements Expression {

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSuperExpression(this);
        }
    }

    record This(Token keyword) implements Expression {

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitThisExpression(this);
        }
    }

    record Unary(Token operator, Expression right) implements Expression {

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpression(this);
        }
    }

    record Variable(Token name) implements Expression {

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpression(this);
        }
    }
}
