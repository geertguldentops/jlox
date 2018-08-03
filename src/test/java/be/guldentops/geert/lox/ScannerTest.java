package be.guldentops.geert.lox;

import be.guldentops.geert.lox.error.api.Error;
import be.guldentops.geert.lox.error.api.ErrorReporter;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ScannerTest {

    class FakeErrorReporter implements ErrorReporter {

        Error error;

        @Override
        public void handle(Error error) {
            this.error = error;
        }
    }

    @Nested
    class Degenerate {

        @Test
        void scanNullSourceCode() {
            assertThatThrownBy(() -> new Scanner(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("source code should not be null!");
        }
    }

    @Nested
    class SingleTokens {

        @Test
        void scanLeftParen() {
            assertSingleTokenScanned("(", Token.Type.LEFT_PAREN);
        }

        @Test
        void scanRightParen() {
            assertSingleTokenScanned(")", Token.Type.RIGHT_PAREN);
        }

        @Test
        void scanLeftBrace() {
            assertSingleTokenScanned("{", Token.Type.LEFT_BRACE);
        }

        @Test
        void scanRightBrace() {
            assertSingleTokenScanned("}", Token.Type.RIGHT_BRACE);
        }

        @Test
        void scanComma() {
            assertSingleTokenScanned(",", Token.Type.COMMA);
        }

        @Test
        void scanDot() {
            assertSingleTokenScanned(".", Token.Type.DOT);
        }

        @Test
        void scanMinus() {
            assertSingleTokenScanned("-", Token.Type.MINUS);
        }

        @Test
        void scanPlus() {
            assertSingleTokenScanned("+", Token.Type.PLUS);
        }

        @Test
        void scanSemiColon() {
            assertSingleTokenScanned(";", Token.Type.SEMICOLON);
        }

        @Test
        void scanStar() {
            assertSingleTokenScanned("*", Token.Type.STAR);
        }

        @Test
        void scanBang() {
            assertSingleTokenScanned("!", Token.Type.BANG);
        }

        @Test
        void scanInequalityOperator() {
            assertSingleTokenScanned("!=", Token.Type.BANG_EQUAL);
        }

        @Test
        void scanEqual() {
            assertSingleTokenScanned("=", Token.Type.EQUAL);
        }

        @Test
        void scanEqualityOperator() {
            assertSingleTokenScanned("==", Token.Type.EQUAL_EQUAL);
        }

        @Test
        void scanLessThan() {
            assertSingleTokenScanned("<", Token.Type.LESS);
        }

        @Test
        void scanLessThanOrEqualOperator() {
            assertSingleTokenScanned("<=", Token.Type.LESS_EQUAL);
        }

        @Test
        void scanGreaterThan() {
            assertSingleTokenScanned(">", Token.Type.GREATER);
        }

        @Test
        void scanGreaterThanOrEqualOperator() {
            assertSingleTokenScanned(">=", Token.Type.GREATER_EQUAL);
        }

        @Test
        void scanSlash() {
            assertSingleTokenScanned("/", Token.Type.SLASH);
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
            FakeErrorReporter fakeErrorReporter = new FakeErrorReporter();

            Scanner scanner = new Scanner("\n");
            scanner.addErrorReporter(fakeErrorReporter);

            List<Token> tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(1);
            assertThat(tokens.get(0).getType()).isEqualTo(Token.Type.EOF);
            assertThat(tokens.get(0).getLexeme()).isEqualTo("");
            assertThat(tokens.get(0).getLiteral()).isNull();
            assertThat(tokens.get(0).getLine()).isEqualTo(2);

            assertThat(fakeErrorReporter.error).isNull();
        }

        private void assertNoTokensScanned(String sourceCode) {
            FakeErrorReporter fakeErrorReporter = new FakeErrorReporter();

            Scanner scanner = new Scanner(sourceCode);
            scanner.addErrorReporter(fakeErrorReporter);

            List<Token> tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(1);
            assertTokenIsEndOfLine(tokens.get(0));

            assertThat(fakeErrorReporter.error).isNull();
        }
    }

    @Nested
    class StringLiterals {

        @Test
        void singleCharacterString() {
            FakeErrorReporter fakeErrorReporter = new FakeErrorReporter();

            Scanner scanner = new Scanner("\"A\"");
            scanner.addErrorReporter(fakeErrorReporter);

            List<Token> tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(2);
            assertThat(tokens.get(0).getType()).isEqualTo(Token.Type.STRING);
            assertThat(tokens.get(0).getLexeme()).isEqualTo("\"A\"");
            assertThat(tokens.get(0).getLiteral()).isEqualTo("A");
            assertThat(tokens.get(0).getLine()).isEqualTo(1);
            assertTokenIsEndOfLine(tokens.get(1));

            assertThat(fakeErrorReporter.error).isNull();
        }

        @Test
        void multipleCharactersString() {
            FakeErrorReporter fakeErrorReporter = new FakeErrorReporter();

            Scanner scanner = new Scanner("\"Hello\"");
            scanner.addErrorReporter(fakeErrorReporter);

            List<Token> tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(2);
            assertThat(tokens.get(0).getType()).isEqualTo(Token.Type.STRING);
            assertThat(tokens.get(0).getLexeme()).isEqualTo("\"Hello\"");
            assertThat(tokens.get(0).getLiteral()).isEqualTo("Hello");
            assertThat(tokens.get(0).getLine()).isEqualTo(1);
            assertTokenIsEndOfLine(tokens.get(1));

            assertThat(fakeErrorReporter.error).isNull();
        }

        @Test
        void multiLineString() {
            FakeErrorReporter fakeErrorReporter = new FakeErrorReporter();

            Scanner scanner = new Scanner("\"Hello\nWorld\"");
            scanner.addErrorReporter(fakeErrorReporter);

            List<Token> tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(2);
            assertThat(tokens.get(0).getType()).isEqualTo(Token.Type.STRING);
            assertThat(tokens.get(0).getLexeme()).isEqualTo("\"Hello\nWorld\"");
            assertThat(tokens.get(0).getLiteral()).isEqualTo("Hello\nWorld");
            assertThat(tokens.get(0).getLine()).isEqualTo(2);
            assertThat(tokens.get(1).getType()).isEqualTo(Token.Type.EOF);
            assertThat(tokens.get(1).getLexeme()).isEqualTo("");
            assertThat(tokens.get(1).getLiteral()).isNull();
            assertThat(tokens.get(1).getLine()).isEqualTo(2);

            assertThat(fakeErrorReporter.error).isNull();
        }
    }

    @Nested
    class NumberLiterals {

        @Test
        void singleDigitNumber() {
            FakeErrorReporter fakeErrorReporter = new FakeErrorReporter();

            Scanner scanner = new Scanner("9");
            scanner.addErrorReporter(fakeErrorReporter);

            List<Token> tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(2);
            assertThat(tokens.get(0).getType()).isEqualTo(Token.Type.NUMBER);
            assertThat(tokens.get(0).getLexeme()).isEqualTo("9");
            assertThat(tokens.get(0).getLiteral()).isEqualTo(9.0);
            assertThat(tokens.get(0).getLine()).isEqualTo(1);
            assertTokenIsEndOfLine(tokens.get(1));

            assertThat(fakeErrorReporter.error).isNull();
        }

        @Test
        void multipleDigitNumber() {
            FakeErrorReporter fakeErrorReporter = new FakeErrorReporter();

            Scanner scanner = new Scanner("12");
            scanner.addErrorReporter(fakeErrorReporter);

            List<Token> tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(2);
            assertThat(tokens.get(0).getType()).isEqualTo(Token.Type.NUMBER);
            assertThat(tokens.get(0).getLexeme()).isEqualTo("12");
            assertThat(tokens.get(0).getLiteral()).isEqualTo(12.0);
            assertThat(tokens.get(0).getLine()).isEqualTo(1);
            assertTokenIsEndOfLine(tokens.get(1));

            assertThat(fakeErrorReporter.error).isNull();
        }

        @Test
        void fractionalNumber() {
            FakeErrorReporter fakeErrorReporter = new FakeErrorReporter();

            Scanner scanner = new Scanner("1.2");
            scanner.addErrorReporter(fakeErrorReporter);

            List<Token> tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(2);
            assertThat(tokens.get(0).getType()).isEqualTo(Token.Type.NUMBER);
            assertThat(tokens.get(0).getLexeme()).isEqualTo("1.2");
            assertThat(tokens.get(0).getLiteral()).isEqualTo(1.2);
            assertThat(tokens.get(0).getLine()).isEqualTo(1);
            assertTokenIsEndOfLine(tokens.get(1));

            assertThat(fakeErrorReporter.error).isNull();
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
            FakeErrorReporter fakeErrorReporter = new FakeErrorReporter();

            Scanner scanner = new Scanner(identifier);
            scanner.addErrorReporter(fakeErrorReporter);

            List<Token> tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(2);
            assertThat(tokens.get(0).getType()).isEqualTo(Token.Type.IDENTIFIER);
            assertThat(tokens.get(0).getLexeme()).isEqualTo(identifier);
            assertThat(tokens.get(0).getLiteral()).isNull();
            assertThat(tokens.get(0).getLine()).isEqualTo(1);
            assertTokenIsEndOfLine(tokens.get(1));

            assertThat(fakeErrorReporter.error).isNull();
        }

        @Test
        void multipleCharactersIdentifier() {
            FakeErrorReporter fakeErrorReporter = new FakeErrorReporter();

            Scanner scanner = new Scanner("myIdentifier");
            scanner.addErrorReporter(fakeErrorReporter);

            List<Token> tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(2);
            assertThat(tokens.get(0).getType()).isEqualTo(Token.Type.IDENTIFIER);
            assertThat(tokens.get(0).getLexeme()).isEqualTo("myIdentifier");
            assertThat(tokens.get(0).getLiteral()).isNull();
            assertThat(tokens.get(0).getLine()).isEqualTo(1);
            assertTokenIsEndOfLine(tokens.get(1));

            assertThat(fakeErrorReporter.error).isNull();
        }
    }

    @Nested
    class ReservedWords {

        @Test
        void scanAnd() {
            assertSingleTokenScanned("and", Token.Type.AND);
        }

        @Test
        void scanClass() {
            assertSingleTokenScanned("class", Token.Type.CLASS);
        }

        @Test
        void scanElse() {
            assertSingleTokenScanned("else", Token.Type.ELSE);
        }

        @Test
        void scanFalse() {
            assertSingleTokenScanned("false", Token.Type.FALSE);
        }

        @Test
        void scanFor() {
            assertSingleTokenScanned("for", Token.Type.FOR);
        }

        @Test
        void scanFun() {
            assertSingleTokenScanned("fun", Token.Type.FUN);
        }

        @Test
        void scanIf() {
            assertSingleTokenScanned("if", Token.Type.IF);
        }

        @Test
        void scanNil() {
            assertSingleTokenScanned("nil", Token.Type.NIL);
        }

        @Test
        void scanOr() {
            assertSingleTokenScanned("or", Token.Type.OR);
        }

        @Test
        void scanPrint() {
            assertSingleTokenScanned("print", Token.Type.PRINT);
        }

        @Test
        void scanReturn() {
            assertSingleTokenScanned("return", Token.Type.RETURN);
        }

        @Test
        void scanSuper() {
            assertSingleTokenScanned("super", Token.Type.SUPER);
        }

        @Test
        void scanThis() {
            assertSingleTokenScanned("this", Token.Type.THIS);
        }

        @Test
        void scanTrue() {
            assertSingleTokenScanned("true", Token.Type.TRUE);
        }

        @Test
        void scanVar() {
            assertSingleTokenScanned("var", Token.Type.VAR);
        }

        @Test
        void scanWhile() {
            assertSingleTokenScanned("while", Token.Type.WHILE);
        }

    }

    @Nested
    class ErrorCases {

        @Test
        void unknownCharacter() {
            FakeErrorReporter fakeErrorReporter = new FakeErrorReporter();

            Scanner scanner = new Scanner("@");
            scanner.addErrorReporter(fakeErrorReporter);

            List<Token> tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(1);
            assertTokenIsEndOfLine(tokens.get(0));

            assertThat(fakeErrorReporter.error).isNotNull();
            assertThat(fakeErrorReporter.error.getLine()).isEqualTo(1);
            assertThat(fakeErrorReporter.error.getLocation()).isNull();
            assertThat(fakeErrorReporter.error.getMessage()).isEqualTo("Unexpected character.");
        }

        @Test
        void unterminatedString() {
            FakeErrorReporter fakeErrorReporter = new FakeErrorReporter();

            Scanner scanner = new Scanner("\"H");
            scanner.addErrorReporter(fakeErrorReporter);

            List<Token> tokens = scanner.scanTokens();

            assertThat(tokens).hasSize(1);
            assertTokenIsEndOfLine(tokens.get(0));

            assertThat(fakeErrorReporter.error).isNotNull();
            assertThat(fakeErrorReporter.error.getLine()).isEqualTo(1);
            assertThat(fakeErrorReporter.error.getLocation()).isNull();
            assertThat(fakeErrorReporter.error.getMessage()).isEqualTo("Unterminated string.");
        }
    }

    private void assertSingleTokenScanned(String sourceCode, Token.Type type) {
        FakeErrorReporter fakeErrorReporter = new FakeErrorReporter();

        Scanner scanner = new Scanner(sourceCode);
        scanner.addErrorReporter(fakeErrorReporter);

        List<Token> tokens = scanner.scanTokens();

        assertThat(tokens).hasSize(2);
        assertTokenHasTypeAndLexeme(tokens.get(0), type, sourceCode);
        assertTokenIsEndOfLine(tokens.get(1));

        assertThat(fakeErrorReporter.error).isNull();
    }

    private void assertTokenHasTypeAndLexeme(Token token, Token.Type type, String lexeme) {
        assertThat(token.getType()).isEqualTo(type);
        assertThat(token.getLexeme()).isEqualTo(lexeme);
        assertThat(token.getLiteral()).isNull();
        assertThat(token.getLine()).isEqualTo(1);
    }

    private void assertTokenIsEndOfLine(Token token) {
        assertThat(token.getType()).isEqualTo(Token.Type.EOF);
        assertThat(token.getLexeme()).isEqualTo("");
        assertThat(token.getLiteral()).isNull();
        assertThat(token.getLine()).isEqualTo(1);
    }
}
