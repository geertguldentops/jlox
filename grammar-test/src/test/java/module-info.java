module be.guldentops.geert.lox.grammar.test {

    requires be.guldentops.geert.lox.grammar;
    requires be.guldentops.geert.lox.lexer;
    requires be.guldentops.geert.lox.lexer.test;

    requires org.junit.jupiter.api;

    requires org.assertj.core;

    /**
     * IntelliJ gives a false positive error message here.
     */
    exports be.guldentops.geert.lox.grammar.test;
}