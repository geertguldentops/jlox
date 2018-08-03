package be.guldentops.geert.lox.grammar;

import be.guldentops.geert.lox.lexer.api.Token;

import java.util.List;

public interface Statement {

    <R> R accept(Visitor<R> visitor);

    interface Visitor<R> {

        R visitBlockStatement(Block statement);

        R visitExpressionStatement(Expression statement);

        R visitIfStatement(If statement);

        R visitPrintStatement(Print statement);

        R visitVariableStatement(Variable statement);

        R visitWhileStatement(While statement);
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

    class If implements Statement {

        public final be.guldentops.geert.lox.grammar.Expression condition;
        public final Statement thenBranch;
        public final Statement elseBranch;

        public If(be.guldentops.geert.lox.grammar.Expression condition, Statement thenBranch, Statement elseBranch) {
            this.condition = condition;
            this.thenBranch = thenBranch;
            this.elseBranch = elseBranch;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitIfStatement(this);
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

    class While implements Statement {

        public final be.guldentops.geert.lox.grammar.Expression condition;
        public final Statement body;

        public While(be.guldentops.geert.lox.grammar.Expression condition, Statement body) {
            this.condition = condition;
            this.body = body;
        }

        public <R> R accept(Visitor<R> visitor) {
            return visitor.visitWhileStatement(this);
        }
    }
}
