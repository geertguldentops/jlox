package be.guldentops.geert.lox.lexer.api;

import org.junit.jupiter.api.Test;

import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.bang;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.bangEqual;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.one;
import static org.assertj.core.api.Assertions.assertThat;

class TokenTest {

    @Test
    void printsNicely() {
        assertThat(bang().toString()).isEqualTo("BANG ! null");
        assertThat(bangEqual().toString()).isEqualTo("BANG_EQUAL != null");
        assertThat(one().toString()).isEqualTo("NUMBER 1 1.0");
    }

}