package be.guldentops.geert.lox.environment;

import be.guldentops.geert.lox.error.api.RuntimeError;
import be.guldentops.geert.lox.interpreter.api.LoxCallable;
import be.guldentops.geert.lox.lexer.api.Token;

import java.util.HashMap;
import java.util.Map;

import static be.guldentops.geert.lox.lexer.api.Token.Type.IDENTIFIER;

public class Environment {

    private final Environment enclosing;

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
            throw new RuntimeError(name, String.format("Variable '%s' is already defined.", name.lexeme));

        values.put(name.lexeme, value);
    }

    public void defineNativeMethod(String name, LoxCallable loxCallable) {
        define(new Token(IDENTIFIER, name, null, -1), loxCallable);
    }

    public void assign(Token name, Object value) {
        if (values.containsKey(name.lexeme)) {
            values.put(name.lexeme, value);
        } else if (enclosing != null) {
            enclosing.assign(name, value);
        } else {
            throw new RuntimeError(name, String.format("Undefined variable '%s'.", name.lexeme));
        }
    }

    public Object get(Token name) {
        if (values.containsKey(name.lexeme)) return values.get(name.lexeme);
        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, String.format("Undefined variable '%s'.", name.lexeme));
    }
}
