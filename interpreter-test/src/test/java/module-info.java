module be.guldentops.geert.lox.interpreter.test {

    requires be.guldentops.geert.lox.error;
    requires be.guldentops.geert.lox.error.test;
    requires be.guldentops.geert.lox.lexer;
    requires be.guldentops.geert.lox.lexer.test;
    requires be.guldentops.geert.lox.grammar;
    requires be.guldentops.geert.lox.grammar.test;
    requires be.guldentops.geert.lox.semantic.analyser;
    requires be.guldentops.geert.lox.interpreter;

    requires org.junit.jupiter.api;

    requires org.assertj.core;
}