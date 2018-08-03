package be.guldentops.geert.lox.lexer.api;

import be.guldentops.geert.lox.error.api.ErrorReporter;

public interface CanReportErrors {

    void addErrorReporter(ErrorReporter errorReporter);
}
