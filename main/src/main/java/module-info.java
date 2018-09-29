module be.guldentops.geert.lox.main {

    requires be.guldentops.geert.lox.error;
    requires be.guldentops.geert.lox.lexer;
    requires be.guldentops.geert.lox.parser;
    requires be.guldentops.geert.lox.semantic.analyser;
    requires be.guldentops.geert.lox.interpreter;

    exports be.guldentops.geert.lox.main;
}