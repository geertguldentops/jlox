package be.guldentops.geert.lox.semantic.analysis.api;

import be.guldentops.geert.lox.error.api.CanReportErrors;
import be.guldentops.geert.lox.grammar.Statement;
import be.guldentops.geert.lox.semantic.analysis.impl.VariableResolver;

import java.util.List;

public interface Resolver extends CanReportErrors {

    static Resolver createDefault(ResolutionAnalyzer resolutionAnalyzer) {
        return new VariableResolver(resolutionAnalyzer);
    }

    void resolve(List<Statement> statements);
}
