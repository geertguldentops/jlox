package be.guldentops.geert.lox.interpreter;

import java.util.List;
import java.util.Map;

class LoxClass implements LoxCallable {

    private final String name;
    private final Map<String, LoxFunction> methods;

    LoxClass(String name, Map<String, LoxFunction> methods) {
        this.name = name;
        this.methods = methods;
    }

    LoxFunction findMethod(LoxInstance instance, String name) {
        if (methods.containsKey(name)) {
            return methods.get(name).bind(instance);
        }

        return null;
    }

    @Override
    public int arity() {
        var initializer = methods.get("init");
        if (initializer == null) return 0;

        return initializer.arity();
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        var instance = new LoxInstance(this);
        var initializer = methods.get("init");
        if (initializer != null) {
            initializer.bind(instance).call(interpreter, arguments);
        }

        return instance;
    }

    @Override
    public String toString() {
        return name;
    }
}