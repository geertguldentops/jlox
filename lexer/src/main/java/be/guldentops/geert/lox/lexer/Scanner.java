package be.guldentops.geert.lox.lexer;

import be.guldentops.geert.lox.error.api.CanReportErrors;

import java.util.List;

public interface Scanner extends CanReportErrors {

    static Scanner createDefault(String sourceCode) {
        return new SimpleScanner(sourceCode);
    }

    List<Token> scanTokens();
}
