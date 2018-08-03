package be.guldentops.geert.lox.grammar;

import be.guldentops.geert.lox.lexer.Token;

import java.util.List;

public interface Expression {

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

        R visitThisExpression(This expression);

        R visitUnaryExpression(Unary expression);

        R visitVariableExpression(Variable expression);
    }

    class Assign implements Expression {

        public final Token name;
        public final Expression value;

        public Assign(Token name, Expression value) {
            this.name = name;
            this.value = value;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitAssignExpression(this);
        }
    }

    class Binary implements Expression {

        public final Expression left;
        public final Token operator;
        public final Expression right;

        public Binary(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBinaryExpression(this);
        }
    }

    class Call implements Expression {

        public final Expression callee;
        public final Token paren;
        public final List<Expression> arguments;

        public Call(Expression callee, Token paren, List<Expression> arguments) {
            this.callee = callee;
            this.paren = paren;
            this.arguments = arguments;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitCallExpression(this);
        }
    }

    class Get implements Expression {

        public final Expression object;
        public final Token name;

        public Get(Expression object, Token name) {
            this.object = object;
            this.name = name;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGetExpression(this);
        }
    }

    class Grouping implements Expression {

        public final Expression expression;

        public Grouping(Expression expression) {
            this.expression = expression;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitGroupingExpression(this);
        }
    }

    class Literal implements Expression {

        public final Object value;

        public Literal(Object value) {
            this.value = value;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLiteralExpression(this);
        }
    }

    class Logical implements Expression {

        public final Expression left;
        public final Token operator;
        public final Expression right;

        public Logical(Expression left, Token operator, Expression right) {
            this.left = left;
            this.operator = operator;
            this.right = right;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitLogicalExpression(this);
        }
    }

    class Set implements Expression {

        public final Expression object;
        public final Token name;
        public final Expression value;

        public Set(Expression object, Token name, Expression value) {
            this.object = object;
            this.name = name;
            this.value = value;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitSetExpression(this);
        }
    }

    class This implements Expression {

        public final Token keyword;

        public This(Token keyword) {
            this.keyword = keyword;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitThisExpression(this);
        }
    }

    class Unary implements Expression {

        public final Token operator;
        public final Expression right;

        public Unary(Token operator, Expression right) {
            this.operator = operator;
            this.right = right;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitUnaryExpression(this);
        }
    }

    class Variable implements Expression {

        public final Token name;

        public Variable(Token name) {
            this.name = name;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableExpression(this);
        }
    }
}
