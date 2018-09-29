module be.guldentops.geert.lox.tools.test {

    requires be.guldentops.geert.lox.lexer;
    requires be.guldentops.geert.lox.lexer.test;
    requires be.guldentops.geert.lox.grammar;
    requires be.guldentops.geert.lox.grammar.test;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;

    requires org.assertj.core;
    requires be.guldentops.geert.lox.tools;
}