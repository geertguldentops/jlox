package be.guldentops.geert.lox.tools;

import be.guldentops.geert.lox.grammar.Expression;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.minus;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.plus;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.star;
import static org.assertj.core.api.Assertions.assertThat;

class AbstractSyntaxTreePrinterTest {

    private AbstractSyntaxTreePrinter astPrinter;

    @BeforeEach
    void setUp() {
        astPrinter = new AbstractSyntaxTreePrinter();
    }

    @Test
    void printLiteralExpression() {
        assertThat(astPrinter.print(new Expression.Literal("1"))).isEqualTo("1");
    }

    @Test
    void printNullLiteralExpression() {
        assertThat(astPrinter.print(new Expression.Literal(null))).isEqualTo("nil");
    }

    @Test
    void printBinaryExpressionWithLeftAndRightLiterals() {
        Expression.Literal one = new Expression.Literal("1");
        Expression.Literal two = new Expression.Literal("2");

        assertThat(astPrinter.print(new Expression.Binary(one, plus(), two))).isEqualTo("(+ 1 2)");
    }

    @Test
    void printUnaryExpression() {
        Expression.Literal one = new Expression.Literal("1");

        assertThat(astPrinter.print(new Expression.Unary(minus(), one))).isEqualTo("(- 1)");
    }

    @Test
    void printGroupExpression() {
        assertThat(astPrinter.print(new Expression.Grouping(new Expression.Literal(12.34)))).isEqualTo("(group 12.34)");
    }

    @Test
    void printComplexExpression() {
        Expression complexBinaryExpression = new Expression.Binary(
                new Expression.Unary(
                        minus(),
                        new Expression.Literal(123)),
                star(),
                new Expression.Grouping(
                        new Expression.Literal(45.67)));

        assertThat(astPrinter.print(complexBinaryExpression)).isEqualTo("(* (- 123) (group 45.67))");
    }

}