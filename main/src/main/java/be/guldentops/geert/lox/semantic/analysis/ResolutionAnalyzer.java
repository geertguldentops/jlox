package be.guldentops.geert.lox.semantic.analysis;

import be.guldentops.geert.lox.grammar.Expression;

public interface ResolutionAnalyzer {

    void resolve(Expression expression, int depth);

}
