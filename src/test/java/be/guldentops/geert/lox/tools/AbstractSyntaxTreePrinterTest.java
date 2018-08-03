package be.guldentops.geert.lox.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.assign;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.binary;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.grouping;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.literal;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.logical;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.unary;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.variable;
import static be.guldentops.geert.lox.grammar.StatementTestFactory._if;
import static be.guldentops.geert.lox.grammar.StatementTestFactory._while;
import static be.guldentops.geert.lox.grammar.StatementTestFactory.blockStatement;
import static be.guldentops.geert.lox.grammar.StatementTestFactory.expressionStatement;
import static be.guldentops.geert.lox.grammar.StatementTestFactory.print;
import static be.guldentops.geert.lox.grammar.StatementTestFactory.uninitializedVariableDeclaration;
import static be.guldentops.geert.lox.grammar.StatementTestFactory.variableDeclaration;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.and;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.identifier;
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

    @Nested
    class Expressions {

        @Test
        void printIntegerExpression() {
            assertThat(astPrinter.print(literal(1.0))).isEqualTo("1.0");
        }

        @Test
        void printFloatingPointNumberExpression() {
            assertThat(astPrinter.print(literal(3.14))).isEqualTo("3.14");
        }

        @Test
        void printStringExpression() {
            assertThat(astPrinter.print(literal("Hello world"))).isEqualTo("Hello world");
        }

        @Test
        void printBooleanExpression() {
            assertThat(astPrinter.print(literal(true))).isEqualTo("true");
            assertThat(astPrinter.print(literal(false))).isEqualTo("false");
        }

        @Test
        void printNullExpression() {
            assertThat(astPrinter.print(literal(null))).isEqualTo("nil");
        }

        @Test
        void printBinaryExpressionWithLeftAndRightLiterals() {
            assertThat(astPrinter.print(binary(literal(1.0), plus(), literal(2.0)))).isEqualTo("(+ 1.0 2.0)");
        }

        @Test
        void printUnaryExpression() {
            assertThat(astPrinter.print(unary(minus(), literal(1.0)))).isEqualTo("(- 1.0)");
        }

        @Test
        void printGroupExpression() {
            assertThat(astPrinter.print(grouping(literal(12.34)))).isEqualTo("(group 12.34)");
        }

        @Test
        void printComplexExpression() {
            var complexBinaryExpression = binary(
                    unary(
                            minus(),
                            literal(123.0)),
                    star(),
                    grouping(
                            literal(45.67)));

            assertThat(astPrinter.print(complexBinaryExpression)).isEqualTo("(* (- 123.0) (group 45.67))");
        }

        @Test
        void printVariableExpression() {
            assertThat(astPrinter.print(variable(identifier("a")))).isEqualTo("a");
        }

        @Test
        void printAssignExpression() {
            assertThat(astPrinter.print(assign(identifier("a"), literal(1.0)))).isEqualTo("( = a 1.0)");
        }

        @Test
        void printLogicalExpression() {
            assertThat(astPrinter.print(logical(literal(true), and(), literal(false)))).isEqualTo("(and true false)");
        }
    }

    @Nested
    class Statements {

        @Test
        void printBlockStatement() {
            assertThat(astPrinter.print(blockStatement(print(literal(1.0))))).isEqualTo("(block (print 1.0))");
        }

        @Test
        void printExpressionStatement() {
            assertThat(astPrinter.print(expressionStatement(literal(1.0)))).isEqualTo("(; 1.0)");
        }

        @Test
        void printIfStatement() {
            assertThat(astPrinter.print(_if(literal(true), print(literal(1.0)), null))).isEqualTo("if true (print 1.0)");
        }

        @Test
        void printIfElseStatement() {
            assertThat(astPrinter.print(_if(literal(true), print(literal(1.0)), print(literal(2.0))))).isEqualTo("if-else true (print 1.0) (print 2.0)");
        }

        @Test
        void printPrintStatement() {
            assertThat(astPrinter.print(print(literal(1.0)))).isEqualTo("(print 1.0)");
        }

        @Test
        void printUninitializedVariableStatement() {
            assertThat(astPrinter.print(uninitializedVariableDeclaration(identifier("a")))).isEqualTo("(var a)");
        }

        @Test
        void printInitializedVariableStatement() {
            assertThat(astPrinter.print(variableDeclaration(identifier("a"), literal(1.0)))).isEqualTo("(var a = 1.0)");
        }

        @Test
        void printWhile() {
            assertThat(astPrinter.print(_while(literal(true), print(literal(1.0))))).isEqualTo("while true (print 1.0)");
        }
    }
}