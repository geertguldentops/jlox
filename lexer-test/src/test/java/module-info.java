module be.guldentops.geert.lox.lexer.test {

    requires be.guldentops.geert.lox.lexer;
    requires be.guldentops.geert.lox.error.test;

    requires org.junit.jupiter.api;
    requires org.junit.jupiter.params;
    requires org.assertj.core;

    /**
     * IntelliJ gives a false positive error message here.
     */
    exports be.guldentops.geert.lox.lexer.test.api;
}