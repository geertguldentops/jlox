package be.guldentops.geert.lox.interpreter.api;

import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.lexer.api.CanReportErrors;

public interface Interpreter extends CanReportErrors {

    Object interpret(Expression expression);
}
