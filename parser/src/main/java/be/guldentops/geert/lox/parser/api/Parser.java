package be.guldentops.geert.lox.parser.api;

import be.guldentops.geert.lox.error.api.CanReportErrors;
import be.guldentops.geert.lox.grammar.Statement;
import be.guldentops.geert.lox.lexer.api.Token;
import be.guldentops.geert.lox.parser.impl.RecursiveDescentParser;

import java.util.List;

public interface Parser extends CanReportErrors {

    static Parser createDefault(List<Token> tokens) {
        return new RecursiveDescentParser(tokens);
    }

    List<Statement> parse();
}
