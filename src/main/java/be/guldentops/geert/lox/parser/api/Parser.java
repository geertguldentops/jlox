package be.guldentops.geert.lox.parser.api;

import be.guldentops.geert.lox.grammar.Statement;
import be.guldentops.geert.lox.lexer.api.CanReportErrors;

import java.util.List;

public interface Parser extends CanReportErrors {

    List<Statement> parse();
}
