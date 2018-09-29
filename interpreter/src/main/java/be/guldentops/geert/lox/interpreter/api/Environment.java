package be.guldentops.geert.lox.interpreter.api;

import be.guldentops.geert.lox.interpreter.impl.LoxCallable;
import be.guldentops.geert.lox.lexer.api.Token;

import java.util.HashMap;
import java.util.Map;

public class Environment {

    public final Environment enclosing;

    private final Map<String, Object> values = new HashMap<>();

    public static Environment createGlobal() {
        return new Environment(null);
    }

    public static Environment createLocal(Environment enclosing) {
        return new Environment(enclosing);
    }

    private Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(Token name, Object value) {
        if (values.containsKey(name.lexeme))
            throw new RuntimeError(name, "variable is already defined.");

        define(name.lexeme, value);
    }

    public void define(String name, Object value) {
        values.put(name, value);
    }

    public void defineNativeMethod(String name, LoxCallable loxCallable) {
        define(name, loxCallable);
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
        } else {
            throw new RuntimeError(name, "undefined variable.");
        }
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) return values.get(name.lexeme);
        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "undefined variable.");
    }

    public Object getAt(Integer distance, String name) {
        return ancestor(distance).values.get(name);
    }

    private Environment ancestor(Integer distance) {
        var environment = this;
        for (var i = 0; i < distance; i++) {
            environment = environment.enclosing;
        }

        return environment;
    }

    public void assignAt(Integer distance, Token name, Object value) {
        ancestor(distance).values.put(name.lexeme, value);
    }
}
