package be.guldentops.geert.lox.grammar;

import be.guldentops.geert.lox.lexer.api.Token;

import java.util.List;

public interface Statement {

    <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {

        R visitBlockStatement(Block statement);

        R visitExpressionStatement(Expression statement);

        R visitPrintStatement(Print statement);

        R visitVariableStatement(Variable statement);
    }

    class Block implements Statement {

        public final List<Statement> statements;

        public Block(List<Statement> statements) {
            this.statements = statements;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitBlockStatement(this);
        }
    }

    class Expression implements Statement {

        public final be.guldentops.geert.lox.grammar.Expression expression;

        public Expression(be.guldentops.geert.lox.grammar.Expression expression) {
            this.expression = expression;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitExpressionStatement(this);
        }
    }

    class Print implements Statement {

        public final be.guldentops.geert.lox.grammar.Expression expression;

        public Print(be.guldentops.geert.lox.grammar.Expression expression) {
            this.expression = expression;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitPrintStatement(this);
        }
    }

    class Variable implements Statement {

        public final Token name;
        public final be.guldentops.geert.lox.grammar.Expression initializer;

        public Variable(Token name, be.guldentops.geert.lox.grammar.Expression initializer) {
            this.name = name;
            this.initializer = initializer;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitVariableStatement(this);
        }
    }
}
