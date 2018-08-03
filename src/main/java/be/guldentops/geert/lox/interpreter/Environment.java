package be.guldentops.geert.lox.interpreter;

import be.guldentops.geert.lox.error.RuntimeError;
import be.guldentops.geert.lox.lexer.Token;

import java.util.HashMap;
import java.util.Map;

class Environment {

    final Environment enclosing;

    private final Map<String, Object> values = new HashMap<>();

    static Environment createGlobal() {
        return new Environment(null);
    }

    static Environment createLocal(Environment enclosing) {
        return new Environment(enclosing);
    }

    private Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    void define(Token name, Object value) {
        if (values.containsKey(name.lexeme))
            throw new RuntimeError(name, String.format("Variable '%s' is already defined.", name.lexeme));

        values.put(name.lexeme, value);
    }

    void define(String name, Object value) {
        if (values.containsKey(name))
            throw new RuntimeError(null, String.format("Variable '%s' is already defined.", name));

        values.put(name, value);
    }

    void defineNativeMethod(String name, LoxCallable loxCallable) {
        define(name, loxCallable);
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
        } else if (enclosing != null) {
            enclosing.assign(name, value);
        } else {
            throw new RuntimeError(name, String.format("Undefined variable '%s'.", name.lexeme));
        }
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme)) return values.get(name.lexeme);
        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, String.format("Undefined variable '%s'.", name.lexeme));
    }

    Object getAt(Integer distance, String name) {
        return ancestor(distance).values.get(name);
    }

    private Environment ancestor(Integer distance) {
        var environment = this;
        for (var i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }

        return environment;
    }

    void assignAt(Integer distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }
}
