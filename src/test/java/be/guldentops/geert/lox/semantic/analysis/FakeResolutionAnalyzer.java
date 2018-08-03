package be.guldentops.geert.lox.semantic.analysis;

import be.guldentops.geert.lox.grammar.Expression;

import java.util.HashMap;
import java.util.Map;

class FakeResolutionAnalyzer implements ResolutionAnalyzer {

    final Map<Expression, Integer> depthPerExpression = new HashMap<>();

    @Override
    public void resolve(Expression expression, int depth) {
        depthPerExpression.put(expression, depth);
    }
}
