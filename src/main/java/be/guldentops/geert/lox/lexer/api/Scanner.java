package be.guldentops.geert.lox.lexer.api;

import java.util.List;

public interface Scanner extends CanReportErrors {

    List<Token> scanTokens();
}
