package be.guldentops.geert.lox.parser.impl;

import be.guldentops.geert.lox.error.impl.FakeErrorReporter;
import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.lexer.api.Token;
import be.guldentops.geert.lox.parser.api.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static be.guldentops.geert.lox.lexer.api.TokenObjectMother._false;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother._true;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.bang;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.bangEqual;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.eof;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.equalEqual;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.greater;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.greaterEqual;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.leftParen;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.less;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.lessEqual;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.minus;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.nil;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.one;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.pi;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.plus;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.rightParen;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.slash;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.star;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.two;
import static org.assertj.core.api.Assertions.assertThat;

class RecursiveDecentParserTest {

    private FakeErrorReporter fakeErrorReporter;

    @BeforeEach
    void setUp() {
        fakeErrorReporter = new FakeErrorReporter();
    }

    @Nested
    class DegenerateCases {

        @Test
        void noTokens() {
            Parser parser = createParser((List) null);

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertThat(fakeErrorReporter.receivedError()).isFalse();
        }

        @Test
        void emptyListOfTokens() {
            Parser parser = createParser(Collections.emptyList());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertThat(fakeErrorReporter.receivedError()).isFalse();
        }
    }

    @Nested
    class LiteralExpressions {

        @Test
        void falseToken() {
            Parser parser = createParser(_false(), eof());

            Expression ast = parser.parse();

            assertLiteralExpression(ast, false);
        }

        @Test
        void trueToken() {
            Parser parser = createParser(_true(), eof());

            Expression ast = parser.parse();

            assertLiteralExpression(ast, true);
        }

        @Test
        void nilToken() {
            Parser parser = createParser(nil(), eof());

            Expression ast = parser.parse();

            assertLiteralExpression(ast, null);
        }

        @Test
        void integerNumberToken() {
            Parser parser = createParser(one(), eof());

            Expression ast = parser.parse();

            assertLiteralExpression(ast, 1.0);
        }

        @Test
        void floatingPointNumberToken() {
            Parser parser = createParser(pi(), eof());

            Expression ast = parser.parse();

            assertLiteralExpression(ast, 3.14);
        }

        @Test
        void stringToken() {
            Parser parser = createParser(new Token(Token.Type.STRING, "\"Hello\"", "Hello", 1), eof());

            Expression ast = parser.parse();

            assertLiteralExpression(ast, "Hello");
        }
    }

    @Nested
    class GroupingExpressions {

        @Test
        void leftAndRightParenWithLiteralSubExpression() {
            Parser parser = createParser(leftParen(), one(), rightParen(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isInstanceOf(Expression.Grouping.class);
            Expression.Grouping groupingExpression = (Expression.Grouping) ast;

            assertThat(groupingExpression.expression).isNotNull();
            assertLiteralExpression(groupingExpression.expression, 1.0);
        }
    }

    @Nested
    class UnaryExpressions {

        @Test
        void bangTokenFollowedByLiteralToken() {
            Parser parser = createParser(bang(), _false(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isInstanceOf(Expression.Unary.class);
            Expression.Unary unaryExpression = (Expression.Unary) ast;

            assertThat(unaryExpression.operator).isEqualToComparingFieldByField(bang());
            assertLiteralExpression(unaryExpression.right, false);
        }

        @Test
        void minusTokenFollowedByLiteralToken() {
            Parser parser = createParser(minus(), one(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isInstanceOf(Expression.Unary.class);
            Expression.Unary unaryExpression = (Expression.Unary) ast;

            assertThat(unaryExpression.operator).isEqualToComparingFieldByField(minus());
            assertLiteralExpression(unaryExpression.right, 1.0);
        }

        @Test
        void multipleUnaryOperations() {
            Parser parser = createParser(bang(), bang(), bang(), _false(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isInstanceOf(Expression.Unary.class);
            Expression.Unary farLeftUnaryExpression = (Expression.Unary) ast;
            assertThat(farLeftUnaryExpression.operator).isEqualToComparingFieldByField(bang());

            assertThat(farLeftUnaryExpression.right).isInstanceOf(Expression.Unary.class);
            Expression.Unary leftUnaryExpression = (Expression.Unary) farLeftUnaryExpression.right;
            assertThat(leftUnaryExpression.operator).isEqualToComparingFieldByField(bang());

            assertThat(leftUnaryExpression).isInstanceOf(Expression.Unary.class);
            Expression.Unary unaryExpression = (Expression.Unary) leftUnaryExpression.right;

            assertThat(unaryExpression.operator).isEqualToComparingFieldByField(bang());
            assertLiteralExpression(unaryExpression.right, false);
        }
    }

    @Nested
    class BinaryExpressions {

        @Test
        void slashTokenWithLeftAndRightOperands() {
            Parser parser = createParser(one(), slash(), two(), eof());

            Expression ast = parser.parse();

            assertBinaryExpression(ast, 1.0, slash(), 2.0);
        }

        @Test
        void starTokenWithLeftAndRightOperands() {
            Parser parser = createParser(one(), star(), two(), eof());

            Expression ast = parser.parse();

            assertBinaryExpression(ast, 1.0, star(), 2.0);
        }

        @Test
        void minusTokenWithLeftAndRightOperands() {
            Parser parser = createParser(one(), minus(), two(), eof());

            Expression ast = parser.parse();

            assertBinaryExpression(ast, 1.0, minus(), 2.0);
        }

        @Test
        void plusTokenWithLeftAndRightOperands() {
            Parser parser = createParser(one(), plus(), two(), eof());

            Expression ast = parser.parse();

            assertBinaryExpression(ast, 1.0, plus(), 2.0);
        }

        @Test
        void greaterTokenWithLeftAndRightOperands() {
            Parser parser = createParser(one(), greater(), two(), eof());

            Expression ast = parser.parse();

            assertBinaryExpression(ast, 1.0, greater(), 2.0);
        }

        @Test
        void greaterEqualTokenWithLeftAndRightOperands() {
            Parser parser = createParser(one(), greaterEqual(), two(), eof());

            Expression ast = parser.parse();

            assertBinaryExpression(ast, 1.0, greaterEqual(), 2.0);
        }

        @Test
        void lessTokenWithLeftAndRightOperands() {
            Parser parser = createParser(one(), less(), two(), eof());

            Expression ast = parser.parse();

            assertBinaryExpression(ast, 1.0, less(), 2.0);
        }

        @Test
        void lessEqualTokenWithLeftAndRightOperands() {
            Parser parser = createParser(one(), lessEqual(), two(), eof());

            Expression ast = parser.parse();

            assertBinaryExpression(ast, 1.0, lessEqual(), 2.0);
        }

        @Test
        void bangEqualTokenWithLeftAndRightOperands() {
            Parser parser = createParser(one(), bangEqual(), two(), eof());

            Expression ast = parser.parse();

            assertBinaryExpression(ast, 1.0, bangEqual(), 2.0);
        }

        @Test
        void equalEqualTokenWithLeftAndRightOperands() {
            Parser parser = createParser(one(), equalEqual(), two(), eof());

            Expression ast = parser.parse();

            assertBinaryExpression(ast, 1.0, equalEqual(), 2.0);
        }

        @Test
        void multipleBinaryOperand() {
            Parser parser = createParser(one(), star(), two(), plus(), pi(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isInstanceOf(Expression.Binary.class);
            Expression.Binary firstBinaryExpression = (Expression.Binary) ast;

            assertThat(firstBinaryExpression.left).isInstanceOf(Expression.Binary.class);
            Expression.Binary secondBinaryExpression = (Expression.Binary) firstBinaryExpression.left;

            assertLiteralExpression(secondBinaryExpression.left, 1.0);
            assertThat(secondBinaryExpression.operator).isEqualToComparingFieldByField(star());
            assertLiteralExpression(secondBinaryExpression.right, 2.0);

            assertThat(firstBinaryExpression.operator).isEqualToComparingFieldByField(plus());
            assertLiteralExpression(firstBinaryExpression.right, 3.14);
        }

        private void assertBinaryExpression(Expression expression, Object left, Token operator, Object right) {
            assertThat(expression).isInstanceOf(Expression.Binary.class);
            Expression.Binary binaryExpression = (Expression.Binary) expression;

            assertLiteralExpression(binaryExpression.left, left);
            assertThat(binaryExpression.operator).isEqualToComparingFieldByField(operator);
            assertLiteralExpression(binaryExpression.right, right);
        }
    }

    @Nested
    class ErrorCases {

        @Test
        void onlyEOFToken() {
            Parser parser = createParser(eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtEnd();
        }

        @Test
        void bangEqualWithoutLeftOperand() {
            Parser parser = createParser(bangEqual(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtLexeme("!=");
        }

        @Test
        void equalEqualWithoutLeftOperand() {
            Parser parser = createParser(equalEqual(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtLexeme("==");
        }

        @Test
        void leftParenWithoutRightParen() {
            Parser parser = createParser(leftParen(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtEnd();
        }

        @Test
        void leftParenAndLiteralButNoRightParen() {
            Parser parser = createParser(leftParen(), one(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertRightParenMissing();
        }

        @Test
        void leftAndRightParenWithoutSubExpression() {
            Parser parser = createParser(leftParen(), rightParen(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtLexeme(")");
        }

        @Test
        void rightParenWithoutLeftParen() {
            Parser parser = createParser(rightParen(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtLexeme(")");
        }

        @Test
        void rightParenAndLiteralButNoLeftParen() {
            Parser parser = createParser(rightParen(), one(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtLexeme(")");
        }

        @Test
        void bangTokenWithoutRightOperand() {
            Parser parser = createParser(bang(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtEnd();
        }

        @Test
        void minusTokenWithoutRightOperand() {
            Parser parser = createParser(minus(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtEnd();
        }

        @Test
        void slashTokenWithoutLeftOperand() {
            Parser parser = createParser(slash(), one(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtLexeme("/");
        }

        @Test
        void slashTokenWithoutRightOperand() {
            Parser parser = createParser(one(), slash(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtEnd();
        }

        @Test
        void starTokenWithoutLeftOperand() {
            Parser parser = createParser(star(), one(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtLexeme("*");
        }

        @Test
        void starTokenWithoutRightOperand() {
            Parser parser = createParser(one(), star(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtEnd();
        }

        @Test
        void plusTokenWithoutLeftOperand() {
            Parser parser = createParser(plus(), one(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtLexeme("+");
        }

        @Test
        void plusTokenWithoutRightOperand() {
            Parser parser = createParser(one(), plus(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtEnd();
        }

        @Test
        void greaterTokenWithoutLeftOperand() {
            Parser parser = createParser(greater(), one(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtLexeme(">");
        }

        @Test
        void greaterTokenWithoutRightOperand() {
            Parser parser = createParser(one(), greater(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtEnd();
        }

        @Test
        void greaterEqualTokenWithoutLeftOperand() {
            Parser parser = createParser(greaterEqual(), one(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtLexeme(">=");
        }

        @Test
        void greaterEqualTokenWithoutRightOperand() {
            Parser parser = createParser(one(), greaterEqual(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtEnd();
        }

        @Test
        void lessTokenWithoutLeftOperand() {
            Parser parser = createParser(less(), one(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtLexeme("<");
        }

        @Test
        void lessTokenWithoutRightOperand() {
            Parser parser = createParser(one(), less(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtEnd();
        }

        @Test
        void lessEqualTokenWithoutLeftOperand() {
            Parser parser = createParser(lessEqual(), one(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtLexeme("<=");
        }

        @Test
        void lessEqualTokenWithoutRightOperand() {
            Parser parser = createParser(one(), lessEqual(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtEnd();
        }

        @Test
        void bangEqualTokenWithoutLeftOperand() {
            Parser parser = createParser(bangEqual(), one(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtLexeme("!=");
        }

        @Test
        void bangEqualTokenWithoutRightOperand() {
            Parser parser = createParser(one(), bangEqual(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtEnd();
        }

        @Test
        void equalEqualTokenWithoutLeftOperand() {
            Parser parser = createParser(equalEqual(), one(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtLexeme("==");
        }

        @Test
        void eqalEqualTokenWithoutRightOperand() {
            Parser parser = createParser(one(), equalEqual(), eof());

            Expression ast = parser.parse();

            assertThat(ast).isNull();

            assertErrorAtEnd();
        }
    }

    private Parser createParser(Token... tokens) {
        return createParser(List.of(tokens));
    }

    private Parser createParser(List<Token> tokens) {
        Parser parser = new RecursiveDecentParser(tokens);
        parser.addErrorReporter(fakeErrorReporter);

        return parser;
    }

    private void assertLiteralExpression(Expression expression, Object expected) {
        assertThat(expression).isInstanceOf(Expression.Literal.class);

        assertThat(((Expression.Literal) expression).value).isEqualTo(expected);
    }

    private void assertErrorAtLexeme(String lexeme) {
        assertThat(fakeErrorReporter.receivedError()).isTrue();
        assertThat(fakeErrorReporter.getError().getLine()).isEqualTo(1);
        assertThat(fakeErrorReporter.getError().getLocation()).isEqualTo(String.format(" at '%s'", lexeme));
        assertThat(fakeErrorReporter.getError().getMessage()).isEqualTo("Expect expression.");
    }

    private void assertErrorAtEnd() {
        assertThat(fakeErrorReporter.receivedError()).isTrue();
        assertThat(fakeErrorReporter.getError().getLine()).isEqualTo(1);
        assertThat(fakeErrorReporter.getError().getLocation()).isEqualTo(" at end");
        assertThat(fakeErrorReporter.getError().getMessage()).isEqualTo("Expect expression.");
    }

    private void assertRightParenMissing() {
        assertThat(fakeErrorReporter.receivedError()).isTrue();
        assertThat(fakeErrorReporter.getError().getLine()).isEqualTo(1);
        assertThat(fakeErrorReporter.getError().getLocation()).isEqualTo(" at end");
        assertThat(fakeErrorReporter.getError().getMessage()).isEqualTo("Expect ')' after expression.");
    }
}