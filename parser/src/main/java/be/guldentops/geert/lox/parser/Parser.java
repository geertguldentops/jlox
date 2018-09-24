package be.guldentops.geert.lox.parser;

import be.guldentops.geert.lox.error.CanReportErrors;
import be.guldentops.geert.lox.grammar.Statement;
import be.guldentops.geert.lox.lexer.Token;

import java.util.List;

public interface Parser extends CanReportErrors {

    static Parser createDefault(List<Token> tokens) {
        return new RecursiveDescentParser(tokens);
    }

    List<Statement> parse();
}
