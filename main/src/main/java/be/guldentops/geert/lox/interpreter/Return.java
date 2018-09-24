package be.guldentops.geert.lox.interpreter;


/**
 * Exceptions are, in general, NOT a good strategy for control flow!
 * But inside this heavily recursive interpreter the exception confirms the rule.
 */
class Return extends RuntimeException {

    final Object value;

    Return(Object value) {
        // Disable JVM exception handling machinery (would generate unnecessary overhead).
        super(null, null, false, false);

        this.value = value;
    }
}
