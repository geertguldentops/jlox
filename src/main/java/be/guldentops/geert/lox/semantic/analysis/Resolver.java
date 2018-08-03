package be.guldentops.geert.lox.semantic.analysis;

import be.guldentops.geert.lox.error.CanReportErrors;
import be.guldentops.geert.lox.grammar.Statement;
import be.guldentops.geert.lox.interpreter.Interpreter;

import java.util.List;

public interface Resolver extends CanReportErrors {

    static Resolver createDefault(Interpreter interpreter) {
        return new VariableResolver(interpreter);
    }

    void resolve(List<Statement> statements);
}
