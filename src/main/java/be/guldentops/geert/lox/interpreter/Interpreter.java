package be.guldentops.geert.lox.interpreter;

import be.guldentops.geert.lox.error.CanReportErrors;
import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.grammar.Statement;
import be.guldentops.geert.lox.semantic.analysis.ResolutionAnalyzer;

import java.util.List;

public interface Interpreter extends ResolutionAnalyzer, CanReportErrors {

    static Interpreter createDefault() {
        var globals = Environment.createGlobal();
        globals.defineNativeMethod("clock", new LoxCallable() {

            @Override
            public int arity() {
                return 0;
            }

            @Override
            public Object call(Interpreter interpreter, List<Object> arguments) {
                return (double) System.currentTimeMillis() / 1_000.0;
            }

            @Override
            public String toString() {
                return "<native fn>";
            }
        });

        return new PostOrderTraversalInterpreter(globals);
    }

    Object interpret(Expression expression);

    void interpret(List<Statement> statements);

    void executeBlock(List<Statement> body, Environment environment);
}
