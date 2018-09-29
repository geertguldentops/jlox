package be.guldentops.geert.lox.interpreter.impl;

import be.guldentops.geert.lox.interpreter.api.Interpreter;

import java.util.List;

public interface LoxCallable {

    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}
