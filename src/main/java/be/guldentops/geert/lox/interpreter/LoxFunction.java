package be.guldentops.geert.lox.interpreter;

import be.guldentops.geert.lox.grammar.Statement;

import java.util.List;

class LoxFunction implements LoxCallable {

    private final Statement.Function declaration;
    private final Environment closure;

    LoxFunction(Statement.Function declaration, Environment closure) {
        this.declaration = declaration;
        this.closure = closure;
    }

    @Override
    public int arity() {
        return declaration.parameters.size();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        var environment = Environment.createLocal(closure);

        for (int i = 0; i < declaration.parameters.size(); i++) {
            environment.define(declaration.parameters.get(i), arguments.get(i));
        }

        try {
            interpreter.executeBlock(declaration.body, environment);
        } catch (Return r) {
            return r.value;
        }

        return null;
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }
}
