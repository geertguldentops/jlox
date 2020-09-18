package be.guldentops.geert.lox.interpreter;

import be.guldentops.geert.lox.grammar.Statement;

import java.util.List;

class LoxFunction implements LoxCallable {

    private final Statement.Function declaration;
    private final Environment closure;
    private final boolean isInitializer;

    static LoxFunction createInitFunction(Statement.Function method, Environment environment) {
        return new LoxFunction(method, environment, true);
    }

    static LoxFunction createFunction(Statement.Function method, Environment environment) {
        return new LoxFunction(method, environment, false);
    }

    private LoxFunction(Statement.Function declaration, Environment closure, boolean isInitializer) {
        this.declaration = declaration;
        this.closure = closure;
        this.isInitializer = isInitializer;
    }

    LoxFunction bind(LoxInstance instance) {
        var environment = Environment.createLocal(closure);
        environment.define("this", instance);
        return LoxFunction.createFunction(declaration, environment);
    }

    @Override
    public int arity() {
        return declaration.parameters().size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        var environment = Environment.createLocal(closure);

        for (int i = 0; i < declaration.parameters().size(); i++) {
            environment.define(declaration.parameters().get(i), arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body(), environment);
        } catch (Return r) {
            if (isInitializer) return closure.getAt(0, "this");

            return r.value;
        }

        if (isInitializer) return closure.getAt(0, "this");
        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name().lexeme() + ">";
    }
}
