package be.guldentops.geert.lox.interpreter.api;

import be.guldentops.geert.lox.environment.Environment;
import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.grammar.Statement;
import be.guldentops.geert.lox.lexer.api.CanReportErrors;

import java.util.List;

public interface Interpreter extends CanReportErrors {

    Object interpret(Expression expression);

    void interpret(List<Statement> statements);

    void executeBlock(List<Statement> body, Environment environment);
}
