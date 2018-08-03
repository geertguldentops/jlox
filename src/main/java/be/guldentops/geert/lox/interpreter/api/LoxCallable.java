package be.guldentops.geert.lox.interpreter.api;

import java.util.List;

public interface LoxCallable {

    int arity();

    Object call(Interpreter interpreter, List<Object> arguments);
}
