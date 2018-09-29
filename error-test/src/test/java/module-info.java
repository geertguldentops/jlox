module be.guldentops.geert.lox.error.test {

    requires be.guldentops.geert.lox.error;

    requires org.junit.jupiter.api;

    requires org.assertj.core;

    /**
     * IntelliJ gives a false positive error message here.
     */
    exports be.guldentops.geert.lox.error.test.api;
}