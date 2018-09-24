package be.guldentops.geert.lox.semantic.analysis;

import be.guldentops.geert.lox.error.CanReportErrors;
import be.guldentops.geert.lox.grammar.Statement;

import java.util.List;

public interface Resolver extends CanReportErrors {

    static Resolver createDefault(ResolutionAnalyzer resolutionAnalyzer) {
        return new VariableResolver(resolutionAnalyzer);
    }

    void resolve(List<Statement> statements);
}
