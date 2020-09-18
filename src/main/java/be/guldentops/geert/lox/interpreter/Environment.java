package be.guldentops.geert.lox.interpreter;

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
        if (values.containsKey(name.lexeme()))
            throw new RuntimeError(name, "variable is already defined.");

        define(name.lexeme(), value);
    }

    void define(String name, Object value) {
        values.put(name, value);
    }

    void defineNativeMethod(String name, LoxCallable loxCallable) {
        define(name, loxCallable);
    }

    void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme())) {
            values.put(name.lexeme(), value);
        } else {
            throw new RuntimeError(name, "undefined variable.");
        }
    }

    Object get(Token name) {
        if (values.containsKey(name.lexeme())) return values.get(name.lexeme());
        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "undefined variable.");
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
        ancestor(distance).values.put(name.lexeme(), value);
    }
}
