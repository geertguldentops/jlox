package be.guldentops.geert.lox.interpreter.impl;

import be.guldentops.geert.lox.error.api.RuntimeError;
import be.guldentops.geert.lox.error.impl.FakeErrorReporter;
import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.interpreter.api.Interpreter;
import be.guldentops.geert.lox.lexer.api.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.bang;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.bangEqual;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.equalEqual;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.greater;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.greaterEqual;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.less;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.lessEqual;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.minus;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.nil;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.plus;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.slash;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.star;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PostOrderTraversalInterpreterTest {

    private Interpreter interpreter;
    private FakeErrorReporter fakeErrorReporter;

    @BeforeEach
    void setUp() {
        fakeErrorReporter = new FakeErrorReporter();

        interpreter = new PostOrderTraversalInterpreter();
        interpreter.addErrorReporter(fakeErrorReporter);
    }

    @Nested
    class DegenerateCases {

        @Test
        void nullExpression() {
            assertThatThrownBy(() -> interpreter.interpret(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Expression should never be null!");
        }
    }

    @Nested
    class SimpleCases {

        @Test
        void literalExpression() {
            assertThat(interpreter.interpret(literal(1.0))).isEqualTo(1.0);
            assertThat(interpreter.interpret(literal(3.14))).isEqualTo(3.14);
            assertThat(interpreter.interpret(literal(null))).isNull();
            assertThat(interpreter.interpret(literal(true))).isEqualTo(true);
            assertThat(interpreter.interpret(literal(false))).isEqualTo(false);
            assertThat(interpreter.interpret(literal("hello world"))).isEqualTo("hello world");
        }

        @Test
        void groupExpression() {
            assertThat(interpreter.interpret(grouping(literal(1.0)))).isEqualTo(1.0);
        }

        @Test
        void minusUnaryExpression() {
            assertThat(interpreter.interpret(unary(minus(), literal(1.0)))).isEqualTo(-1.0);
            assertThat(interpreter.interpret(unary(minus(), literal(-1.0)))).isEqualTo(1.0);
        }

        @Test
        void bangUnaryExpressionWithFalseyValues() {
            assertThat(interpreter.interpret(unary(bang(), literal(false)))).isEqualTo(true);
            assertThat(interpreter.interpret(unary(bang(), literal(null)))).isEqualTo(true);
        }

        @Test
        void bangUnaryExpressionWithTruthyValues() {
            assertThat(interpreter.interpret(unary(bang(), literal(true)))).isEqualTo(false);
            assertThat(interpreter.interpret(unary(bang(), literal(1.0)))).isEqualTo(false);
            assertThat(interpreter.interpret(unary(bang(), literal("Hello")))).isEqualTo(false);
        }

        @Test
        void minusBinaryExpressionWith2Numbers() {
            assertThat(interpreter.interpret(binary(literal(3.0), minus(), literal(1.0)))).isEqualTo(2.0);
            assertThat(interpreter.interpret(binary(literal(12.34), minus(), literal(9.76)))).isEqualTo(2.58);
        }

        @Test
        void slashBinaryExpressionWith2Numbers() {
            assertThat(interpreter.interpret(binary(literal(15.0), slash(), literal(3.0)))).isEqualTo(5.0);
            assertThat(interpreter.interpret(binary(literal(8.27), slash(), literal(2.69)))).isEqualTo(3.074349442379182);
        }

        @Test
        void starBinaryExpressionWith2Numbers() {
            assertThat(interpreter.interpret(binary(literal(3.0), star(), literal(2.0)))).isEqualTo(6.0);
            assertThat(interpreter.interpret(binary(literal(11.33), star(), literal(1.02)))).isEqualTo(11.5566);
        }

        @Test
        void plusBinaryExpressionWith2Numbers() {
            assertThat(interpreter.interpret(binary(literal(8.0), plus(), literal(12.0)))).isEqualTo(20.0);
            assertThat(interpreter.interpret(binary(literal(3.14), plus(), literal(6.82)))).isEqualTo(9.96);
        }

        @Test
        void plusBinaryExpressionWith2Strings() {
            assertThat(interpreter.interpret(binary(literal("Hello "), plus(), literal("world!")))).isEqualTo("Hello world!");
        }

        @Test
        void greaterBinaryExpressionWith2Numbers() {
            assertThat(interpreter.interpret(binary(literal(3.0), greater(), literal(2.0)))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal(3.0), greater(), literal(3.0)))).isEqualTo(false);
            assertThat(interpreter.interpret(binary(literal(2.0), greater(), literal(3.0)))).isEqualTo(false);
        }

        @Test
        void greaterEqualBinaryExpressionWith2Numbers() {
            assertThat(interpreter.interpret(binary(literal(3.0), greaterEqual(), literal(2.0)))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal(3.0), greaterEqual(), literal(3.0)))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal(2.0), greaterEqual(), literal(3.0)))).isEqualTo(false);
        }

        @Test
        void lessBinaryExpressionWith2Numbers() {
            assertThat(interpreter.interpret(binary(literal(3.0), less(), literal(2.0)))).isEqualTo(false);
            assertThat(interpreter.interpret(binary(literal(3.0), less(), literal(3.0)))).isEqualTo(false);
            assertThat(interpreter.interpret(binary(literal(2.0), less(), literal(3.0)))).isEqualTo(true);
        }

        @Test
        void lessEqualBinaryExpressionWith2Numbers() {
            assertThat(interpreter.interpret(binary(literal(3.0), lessEqual(), literal(2.0)))).isEqualTo(false);
            assertThat(interpreter.interpret(binary(literal(3.0), lessEqual(), literal(3.0)))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal(2.0), lessEqual(), literal(3.0)))).isEqualTo(true);
        }

        @Test
        void equalEqualBinaryExpression() {
            assertThat(interpreter.interpret(binary(literal(1.0), equalEqual(), literal(1.0)))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal(3.14), equalEqual(), literal(3.14)))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal("Hello world"), equalEqual(), literal("Hello world")))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal(true), equalEqual(), literal(true)))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal(false), equalEqual(), literal(false)))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal(null), equalEqual(), literal(null)))).isEqualTo(true);
        }

        @Test
        void bangEqualBinaryExpression() {
            assertThat(interpreter.interpret(binary(literal(1.0), bangEqual(), literal(2.0)))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal(3.14), bangEqual(), literal(3.15)))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal("Hello world"), bangEqual(), literal("Hell world")))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal(true), bangEqual(), literal(false)))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal(false), bangEqual(), literal(true)))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal(null), bangEqual(), literal(1.0)))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal(null), bangEqual(), literal(3.14)))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal(null), bangEqual(), literal("Hello world")))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal(null), bangEqual(), literal(true)))).isEqualTo(true);
            assertThat(interpreter.interpret(binary(literal(null), bangEqual(), literal(false)))).isEqualTo(true);
        }
    }

    @Nested
    class ErrorCases {

        @Test
        void unaryExpressionWithInvalidRight() {
            assertThat(interpreter.interpret(unary(minus(), literal("hello world")))).isNull();

            assertThat(fakeErrorReporter.receivedRuntimeError()).isTrue();
            assertThat(fakeErrorReporter.getRuntimeError())
                    .isInstanceOf(RuntimeError.class)
                    .hasMessage("Operand must be a number.");
        }

        @Test
        void unaryExpressionWithInvalidOperator() {
            assertThat(interpreter.interpret(unary(nil(), literal(1.0)))).isNull();

            assertThat(fakeErrorReporter.receivedRuntimeError()).isFalse();
        }

        @Test
        void binaryExpressionWithInvalidOperator() {
            assertThat(interpreter.interpret(binary(literal(2.0), bang(), literal(1.0)))).isNull();

            assertThat(fakeErrorReporter.receivedRuntimeError()).isFalse();
        }

        @Test
        void minusBinaryExpressionWithNumberAndNonNumber() {
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), minus(), literal("hello world"));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), minus(), literal(null));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), minus(), literal(true));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), minus(), literal(false));
        }

        @Test
        void slashBinaryExpressionWithNumberAndNonNumber() {
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), slash(), literal("hello world"));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), slash(), literal(null));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), slash(), literal(true));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), slash(), literal(false));
        }

        @Test
        void starBinaryExpressionWithNumberAndNonNumber() {
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), star(), literal("hello world"));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), star(), literal(null));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), star(), literal(true));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), star(), literal(false));
        }

        @Test
        void plusBinaryExpressionWithoutNumbersOrStrings() {
            assertBinaryExpressionCanOnlyOperateOnNumbersOrStrings(literal(1.0), plus(), literal(null));
            assertBinaryExpressionCanOnlyOperateOnNumbersOrStrings(literal(null), plus(), literal(1.0));

            assertBinaryExpressionCanOnlyOperateOnNumbersOrStrings(literal(1.0), plus(), literal(true));
            assertBinaryExpressionCanOnlyOperateOnNumbersOrStrings(literal(true), plus(), literal(1.0));

            assertBinaryExpressionCanOnlyOperateOnNumbersOrStrings(literal(1.0), plus(), literal(false));
            assertBinaryExpressionCanOnlyOperateOnNumbersOrStrings(literal(false), plus(), literal(1.0));

            assertBinaryExpressionCanOnlyOperateOnNumbersOrStrings(literal("hello world"), plus(), literal(null));
            assertBinaryExpressionCanOnlyOperateOnNumbersOrStrings(literal(null), plus(), literal("hello world"));

            assertBinaryExpressionCanOnlyOperateOnNumbersOrStrings(literal("hello world"), plus(), literal(true));
            assertBinaryExpressionCanOnlyOperateOnNumbersOrStrings(literal(true), plus(), literal("hello world"));

            assertBinaryExpressionCanOnlyOperateOnNumbersOrStrings(literal("hello world"), plus(), literal(false));
            assertBinaryExpressionCanOnlyOperateOnNumbersOrStrings(literal(false), plus(), literal("hello world"));
        }

        @Test
        void greaterBinaryExpressionWithNumberAndNonNumber() {
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), greater(), literal("hello world"));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), greater(), literal(null));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), greater(), literal(true));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), greater(), literal(false));
        }

        @Test
        void greaterEqualBinaryExpressionWithNumberAndNonNumber() {
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), greaterEqual(), literal("hello world"));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), greaterEqual(), literal(null));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), greaterEqual(), literal(true));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), greaterEqual(), literal(false));
        }

        @Test
        void lessBinaryExpressionWithNumberAndNonNumber() {
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), less(), literal("hello world"));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), less(), literal(null));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), less(), literal(true));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), less(), literal(false));
        }

        @Test
        void lessEqualBinaryExpressionWithNumberAndNonNumber() {
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), lessEqual(), literal("hello world"));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), lessEqual(), literal(null));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), lessEqual(), literal(true));
            assertBinaryExpressionCanOnlyOperateOnNumbers(literal(1.0), lessEqual(), literal(false));
        }

        @Test
        void divideByZero() {
            assertThat(interpreter.interpret(binary(literal(5.0), slash(), literal(0.0)))).isNull();

            assertThat(fakeErrorReporter.receivedRuntimeError()).isTrue();
            assertThat(fakeErrorReporter.getRuntimeError())
                    .isInstanceOf(RuntimeError.class)
                    .hasMessage("Can not divide by zero!");
        }

        private void assertBinaryExpressionCanOnlyOperateOnNumbersOrStrings(Expression expression1, Token operator, Expression expression2) {
            assertBinaryExpressionThrowsRuntimeErrorWithMessage(expression1, operator, expression2, "Operands must be two numbers or two strings.");
        }

        private void assertBinaryExpressionCanOnlyOperateOnNumbers(Expression expression1, Token operator, Expression expression2) {
            assertBinaryExpressionThrowsRuntimeErrorWithMessage(expression1, operator, expression2, "Operands must be numbers.");
        }

        private void assertBinaryExpressionThrowsRuntimeErrorWithMessage(Expression expression1, Token operator, Expression expression2, String message) {
            assertThat(interpreter.interpret(binary(expression1, operator, expression2))).isNull();
            assertThat(fakeErrorReporter.receivedRuntimeError()).isTrue();
            assertThat(fakeErrorReporter.getRuntimeError())
                    .isInstanceOf(RuntimeError.class)
                    .hasMessage(message);

            fakeErrorReporter.reset();

            assertThat(interpreter.interpret(binary(expression2, operator, expression1))).isNull();
            assertThat(fakeErrorReporter.receivedRuntimeError()).isTrue();
            assertThat(fakeErrorReporter.getRuntimeError())
                    .isInstanceOf(RuntimeError.class)
                    .hasMessage(message);
        }
    }

    private Expression.Literal literal(Object value) {
        return new Expression.Literal(value);
    }

    private Expression.Grouping grouping(Expression expression) {
        return new Expression.Grouping(expression);
    }

    private Expression.Unary unary(Token operator, Expression right) {
        return new Expression.Unary(operator, right);
    }

    private Expression.Binary binary(Expression left, Token operator, Expression right) {
        return new Expression.Binary(left, operator, right);
    }
}