package be.guldentops.geert.lox.lexer.api;

import be.guldentops.geert.lox.error.api.CanReportErrors;
import be.guldentops.geert.lox.lexer.impl.SimpleScanner;

import java.util.List;

public interface Scanner extends CanReportErrors {

    static Scanner createDefault(String sourceCode) {
        return new SimpleScanner(sourceCode);
    }

    List<Token> scanTokens();
}
