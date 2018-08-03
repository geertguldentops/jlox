package be.guldentops.geert.lox.parser.api;

import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.lexer.api.CanReportErrors;

public interface Parser extends CanReportErrors {

    Expression parse();
}
