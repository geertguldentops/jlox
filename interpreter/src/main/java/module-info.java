module be.guldentops.geert.lox.interpreter {

    requires be.guldentops.geert.lox.error;
    requires be.guldentops.geert.lox.lexer;
    requires be.guldentops.geert.lox.grammar;
    requires be.guldentops.geert.lox.semantic.analyser;

    exports be.guldentops.geert.lox.interpreter.api;
}