package be.guldentops.geert.lox.lexer;

import be.guldentops.geert.lox.error.FakeErrorReporter;
import be.guldentops.geert.lox.lexer.Token.Type;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static be.guldentops.geert.lox.lexer.Token.Type.AND;
import static be.guldentops.geert.lox.lexer.Token.Type.BANG;
import static be.guldentops.geert.lox.lexer.Token.Type.BANG_EQUAL;
import static be.guldentops.geert.lox.lexer.Token.Type.CLASS;
import static be.guldentops.geert.lox.lexer.Token.Type.COMMA;
import static be.guldentops.geert.lox.lexer.Token.Type.DOT;
import static be.guldentops.geert.lox.lexer.Token.Type.ELSE;
import static be.guldentops.geert.lox.lexer.Token.Type.EOF;
import static be.guldentops.geert.lox.lexer.Token.Type.EQUAL;
import static be.guldentops.geert.lox.lexer.Token.Type.EQUAL_EQUAL;
import static be.guldentops.geert.lox.lexer.Token.Type.FALSE;
import static be.guldentops.geert.lox.lexer.Token.Type.FOR;
import static be.guldentops.geert.lox.lexer.Token.Type.FUN;
import static be.guldentops.geert.lox.lexer.Token.Type.GREATER;
import static be.guldentops.geert.lox.lexer.Token.Type.GREATER_EQUAL;
import static be.guldentops.geert.lox.lexer.Token.Type.IDENTIFIER;
import static be.guldentops.geert.lox.lexer.Token.Type.IF;
import static be.guldentops.geert.lox.lexer.Token.Type.LEFT_BRACE;
import static be.guldentops.geert.lox.lexer.Token.Type.LEFT_PAREN;
import static be.guldentops.geert.lox.lexer.Token.Type.LESS;
import static be.guldentops.geert.lox.lexer.Token.Type.LESS_EQUAL;
import static be.guldentops.geert.lox.lexer.Token.Type.MINUS;
import static be.guldentops.geert.lox.lexer.Token.Type.NIL;
import static be.guldentops.geert.lox.lexer.Token.Type.NUMBER;
import static be.guldentops.geert.lox.lexer.Token.Type.OR;
import static be.guldentops.geert.lox.lexer.Token.Type.PLUS;
import static be.guldentops.geert.lox.lexer.Token.Type.PRINT;
import static be.guldentops.geert.lox.lexer.Token.Type.RETURN;
import static be.guldentops.geert.lox.lexer.Token.Type.RIGHT_BRACE;
import static be.guldentops.geert.lox.lexer.Token.Type.RIGHT_PAREN;
import static be.guldentops.geert.lox.lexer.Token.Type.SEMICOLON;
import static be.guldentops.geert.lox.lexer.Token.Type.SLASH;
import static be.guldentops.geert.lox.lexer.Token.Type.STAR;
import static be.guldentops.geert.lox.lexer.Token.Type.STRING;
import static be.guldentops.geert.lox.lexer.Token.Type.SUPER;
import static be.guldentops.geert.lox.lexer.Token.Type.THIS;
import static be.guldentops.geert.lox.lexer.Token.Type.TRUE;
import static be.guldentops.geert.lox.lexer.Token.Type.VAR;
import static be.guldentops.geert.lox.lexer.Token.Type.WHILE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SimpleScannerTest {

    private FakeErrorReporter fakeErrorReporter;

    @BeforeEach
    void setUp() {
        fakeErrorReporter = new FakeErrorReporter();
    }

    @Nested
    class DegenerateCases {

        @Test
        void scanNullSourceCode() {
            assertThatThrownBy(() -> createScanner(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("source code should not be null!");
        }
    }

    @Nested
    class SingleTokens {

        @Test
        void scanLeftParen() {
            assertSingleTokenScanned("(", LEFT_PAREN);
        }

        @Test
        void scanRightParen() {
            assertSingleTokenScanned(")", RIGHT_PAREN);
        }

        @Test
        void scanLeftBrace() {
            assertSingleTokenScanned("{", LEFT_BRACE);
        }

        @Test
        void scanRightBrace() {
            assertSingleTokenScanned("}", RIGHT_BRACE);
        }

        @Test
        void scanComma() {
            assertSingleTokenScanned(",", COMMA);
        }

        @Test
        void scanDot() {
            assertSingleTokenScanned(".", DOT);
        }

        @Test
        void scanMinus() {
            assertSingleTokenScanned("-", MINUS);
        }

        @Test
        void scanPlus() {
            assertSingleTokenScanned("+", PLUS);
        }

        @Test
        void scanSemiColon() {
            assertSingleTokenScanned(";", SEMICOLON);
        }

        @Test
        void scanStar() {
            assertSingleTokenScanned("*", STAR);
        }

        @Test
        void scanBang() {
            assertSingleTokenScanned("!", BANG);
        }

        @Test
        void scanInequalityOperator() {
            assertSingleTokenScanned("!=", BANG_EQUAL);
        }

        @Test
        void scanEqual() {
            assertSingleTokenScanned("=", EQUAL);
        }

        @Test
        void scanEqualityOperator() {
            assertSingleTokenScanned("==", EQUAL_EQUAL);
        }

        @Test
        void scanLessThan() {
            assertSingleTokenScanned("<", LESS);
        }

        @Test
        void scanLessThanOrEqualOperator() {
            assertSingleTokenScanned("<=", LESS_EQUAL);
        }

        @Test
        void scanGreaterThan() {
            assertSingleTokenScanned(">", GREATER);
        }

        @Test
        void scanGreaterThanOrEqualOperator() {
            assertSingleTokenScanned(">=", GREATER_EQUAL);
        }

        @Test
        void scanSlash() {
            assertSingleTokenScanned("/", SLASH);
        }
    }

    @Nested
    class MeaninglessCharacters {

        @Test
        void scanEmptySourceCode() {
            assertNoTokensScanned("");
        }

        @Test
        void ignoresComment() {
            assertNoTokensScanned("// this is a comment!");
        }

        @Test
        void ignoreSpace() {
            assertNoTokensScanned(" ");
        }

        @Test
        void ignoreReturn() {
            assertNoTokensScanned("\r");
        }

        @Test
        void ignoreTab() {
            assertNoTokensScanned("\t");
        }

        @Test
        void scanNewLineIncreasesLineNumber() {
            var scanner = createScanner("\n");

            var tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(1);
            assertThat(tokens.get(0).type()).isEqualTo(EOF);
            assertThat(tokens.get(0).lexeme()).isEqualTo("");
            assertThat(tokens.get(0).literal()).isNull();
            assertThat(tokens.get(0).line()).isEqualTo(2);

            assertThat(fakeErrorReporter.receivedError()).isFalse();
        }

        private void assertNoTokensScanned(String sourceCode) {
            var scanner = createScanner(sourceCode);

            var tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(1);
            assertTokenIsEndOfLine(tokens.get(0));

            assertThat(fakeErrorReporter.receivedError()).isFalse();
        }
    }

    @Nested
    class StringLiterals {

        @Test
        void singleCharacterString() {
            var scanner = createScanner("\"A\"");

            var tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(2);
            assertThat(tokens.get(0).type()).isEqualTo(STRING);
            assertThat(tokens.get(0).lexeme()).isEqualTo("\"A\"");
            assertThat(tokens.get(0).literal()).isEqualTo("A");
            assertThat(tokens.get(0).line()).isEqualTo(1);
            assertTokenIsEndOfLine(tokens.get(1));

            assertThat(fakeErrorReporter.receivedError()).isFalse();
        }

        @Test
        void multipleCharactersString() {
            var scanner = createScanner("\"Hello\"");

            var tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(2);
            assertThat(tokens.get(0).type()).isEqualTo(STRING);
            assertThat(tokens.get(0).lexeme()).isEqualTo("\"Hello\"");
            assertThat(tokens.get(0).literal()).isEqualTo("Hello");
            assertThat(tokens.get(0).line()).isEqualTo(1);
            assertTokenIsEndOfLine(tokens.get(1));

            assertThat(fakeErrorReporter.receivedError()).isFalse();
        }

        @Test
        void multiLineString() {
            var scanner = createScanner("\"Hello\nWorld\"");

            var tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(2);
            assertThat(tokens.get(0).type()).isEqualTo(STRING);
            assertThat(tokens.get(0).lexeme()).isEqualTo("\"Hello\nWorld\"");
            assertThat(tokens.get(0).literal()).isEqualTo("Hello\nWorld");
            assertThat(tokens.get(0).line()).isEqualTo(2);
            assertThat(tokens.get(1).type()).isEqualTo(EOF);
            assertThat(tokens.get(1).lexeme()).isEqualTo("");
            assertThat(tokens.get(1).literal()).isNull();
            assertThat(tokens.get(1).line()).isEqualTo(2);

            assertThat(fakeErrorReporter.receivedError()).isFalse();
        }
    }

    @Nested
    class NumberLiterals {

        @Test
        void singleDigitNumber() {
            var scanner = createScanner("9");

            var tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(2);
            assertThat(tokens.get(0).type()).isEqualTo(NUMBER);
            assertThat(tokens.get(0).lexeme()).isEqualTo("9");
            assertThat(tokens.get(0).literal()).isEqualTo(9.0);
            assertThat(tokens.get(0).line()).isEqualTo(1);
            assertTokenIsEndOfLine(tokens.get(1));

            assertThat(fakeErrorReporter.receivedError()).isFalse();
        }

        @Test
        void multipleDigitNumber() {
            var scanner = createScanner("12");

            var tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(2);
            assertThat(tokens.get(0).type()).isEqualTo(NUMBER);
            assertThat(tokens.get(0).lexeme()).isEqualTo("12");
            assertThat(tokens.get(0).literal()).isEqualTo(12.0);
            assertThat(tokens.get(0).line()).isEqualTo(1);
            assertTokenIsEndOfLine(tokens.get(1));

            assertThat(fakeErrorReporter.receivedError()).isFalse();
        }

        @Test
        void fractionalNumber() {
            var scanner = createScanner("1.2");

            var tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(2);
            assertThat(tokens.get(0).type()).isEqualTo(NUMBER);
            assertThat(tokens.get(0).lexeme()).isEqualTo("1.2");
            assertThat(tokens.get(0).literal()).isEqualTo(1.2);
            assertThat(tokens.get(0).line()).isEqualTo(1);
            assertTokenIsEndOfLine(tokens.get(1));

            assertThat(fakeErrorReporter.receivedError()).isFalse();
        }
    }

    @Nested
    class Identifiers {

        @ParameterizedTest
        @ValueSource(strings = {"_",
                "a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z",
                "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"
        })
        void singleCharacterIdentifier(String identifier) {
            var scanner = createScanner(identifier);

            var tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(2);
            assertThat(tokens.get(0).type()).isEqualTo(IDENTIFIER);
            assertThat(tokens.get(0).lexeme()).isEqualTo(identifier);
            assertThat(tokens.get(0).literal()).isNull();
            assertThat(tokens.get(0).line()).isEqualTo(1);
            assertTokenIsEndOfLine(tokens.get(1));

            assertThat(fakeErrorReporter.receivedError()).isFalse();
        }

        @Test
        void multipleCharactersIdentifier() {
            var scanner = createScanner("myIdentifier");

            var tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(2);
            assertThat(tokens.get(0).type()).isEqualTo(IDENTIFIER);
            assertThat(tokens.get(0).lexeme()).isEqualTo("myIdentifier");
            assertThat(tokens.get(0).literal()).isNull();
            assertThat(tokens.get(0).line()).isEqualTo(1);
            assertTokenIsEndOfLine(tokens.get(1));

            assertThat(fakeErrorReporter.receivedError()).isFalse();
        }
    }

    @Nested
    class ReservedWords {

        @Test
        void scanAnd() {
            assertSingleTokenScanned("and", AND);
        }

        @Test
        void scanClass() {
            assertSingleTokenScanned("class", CLASS);
        }

        @Test
        void scanElse() {
            assertSingleTokenScanned("else", ELSE);
        }

        @Test
        void scanFalse() {
            assertSingleTokenScanned("false", FALSE);
        }

        @Test
        void scanFor() {
            assertSingleTokenScanned("for", FOR);
        }

        @Test
        void scanFun() {
            assertSingleTokenScanned("fun", FUN);
        }

        @Test
        void scanIf() {
            assertSingleTokenScanned("if", IF);
        }

        @Test
        void scanNil() {
            assertSingleTokenScanned("nil", NIL);
        }

        @Test
        void scanOr() {
            assertSingleTokenScanned("or", OR);
        }

        @Test
        void scanPrint() {
            assertSingleTokenScanned("print", PRINT);
        }

        @Test
        void scanReturn() {
            assertSingleTokenScanned("return", RETURN);
        }

        @Test
        void scanSuper() {
            assertSingleTokenScanned("super", SUPER);
        }

        @Test
        void scanThis() {
            assertSingleTokenScanned("this", THIS);
        }

        @Test
        void scanTrue() {
            assertSingleTokenScanned("true", TRUE);
        }

        @Test
        void scanVar() {
            assertSingleTokenScanned("var", VAR);
        }

        @Test
        void scanWhile() {
            assertSingleTokenScanned("while", WHILE);
        }

    }

    @Nested
    class ErrorCases {

        @Test
        void unknownCharacter() {
            var scanner = createScanner("@");

            var tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(1);
            assertTokenIsEndOfLine(tokens.get(0));

            assertThat(fakeErrorReporter.receivedError()).isTrue();
            assertThat(fakeErrorReporter.getError()).hasToString("[line 1] SyntaxError: at '@' unexpected character.");
        }

        @Test
        void unterminatedString() {
            var scanner = createScanner("\"H");

            var tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(1);
            assertTokenIsEndOfLine(tokens.get(0));

            assertThat(fakeErrorReporter.receivedError()).isTrue();
            assertThat(fakeErrorReporter.getError()).hasToString("[line 1] SyntaxError: Unterminated string.");
        }
    }

    private Scanner createScanner(String sourceCode) {
        var scanner = new SimpleScanner(sourceCode);
        scanner.addErrorReporter(fakeErrorReporter);

        return scanner;
    }

    private void assertSingleTokenScanned(String sourceCode, Type type) {
        var scanner = createScanner(sourceCode);

        var tokens = scanner.scanTokens();

        assertThat(tokens).hasSize(2);
        assertTokenHasTypeAndLexeme(tokens.get(0), type, sourceCode);
        assertTokenIsEndOfLine(tokens.get(1));

        assertThat(fakeErrorReporter.receivedError()).isFalse();
    }

    private void assertTokenHasTypeAndLexeme(Token token, Type type, String lexeme) {
        assertThat(token.type()).isEqualTo(type);
        assertThat(token.lexeme()).isEqualTo(lexeme);
        assertThat(token.literal()).isNull();
        assertThat(token.line()).isEqualTo(1);
    }

    private void assertTokenIsEndOfLine(Token token) {
        assertThat(token.type()).isEqualTo(EOF);
        assertThat(token.lexeme()).isEqualTo("");
        assertThat(token.literal()).isNull();
        assertThat(token.line()).isEqualTo(1);
    }
}
