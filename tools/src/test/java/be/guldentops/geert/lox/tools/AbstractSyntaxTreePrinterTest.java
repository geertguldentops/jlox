package be.guldentops.geert.lox.tools;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static be.guldentops.geert.lox.grammar.ExpressionTestFactory._super;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory._this;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.assign;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.binary;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.call;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.get;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.grouping;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.literal;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.logical;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.set;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.unary;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.variable;
import static be.guldentops.geert.lox.grammar.StatementTestFactory._class;
import static be.guldentops.geert.lox.grammar.StatementTestFactory._if;
import static be.guldentops.geert.lox.grammar.StatementTestFactory._return;
import static be.guldentops.geert.lox.grammar.StatementTestFactory._while;
import static be.guldentops.geert.lox.grammar.StatementTestFactory.blockStatement;
import static be.guldentops.geert.lox.grammar.StatementTestFactory.expressionStatement;
import static be.guldentops.geert.lox.grammar.StatementTestFactory.function;
import static be.guldentops.geert.lox.grammar.StatementTestFactory.print;
import static be.guldentops.geert.lox.grammar.StatementTestFactory.variableDeclaration;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.and;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.identifier;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.minus;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.plus;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.star;
import static java.util.Collections.emptyList;
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
            assertThat(astPrinter.print(variable("a"))).isEqualTo("a");
        }

        @Test
        void printAssignExpression() {
            assertThat(astPrinter.print(assign("a", literal(1.0)))).isEqualTo("( = a 1.0)");
        }

        @Test
        void printLogicalExpression() {
            assertThat(astPrinter.print(logical(literal(true), and(), literal(false)))).isEqualTo("(and true false)");
        }

        @Test
        void printCallExpression() {
            assertThat(astPrinter.print(call("sum", literal(1.0), literal(2.0)))).isEqualTo("(call sum (1.0 2.0))");
        }

        @Test
        void printReturnExpression() {
            assertThat(astPrinter.print(_return(literal(1.0)))).isEqualTo("(return 1.0)");
        }


        @Test
        void printGetExpression() {
            assertThat(astPrinter.print(get(literal("point"), identifier("x")))).isEqualTo("(. point x)");
        }

        @Test
        void printSetExpression() {
            assertThat(astPrinter.print(set(literal("point"), identifier("x"), literal(1.0)))).isEqualTo("(. point x 1.0)");
        }

        @Test
        void printThisExpression() {
            assertThat(astPrinter.print(_this())).isEqualTo("this");
        }

        @Test
        void printSuperExpression() {
            assertThat(astPrinter.print(_super(identifier("method")))).isEqualTo("(super method)");
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
            assertThat(astPrinter.print(variableDeclaration("a"))).isEqualTo("var a");
        }

        @Test
        void printInitializedVariableStatement() {
            assertThat(astPrinter.print(variableDeclaration("a", literal(1.0)))).isEqualTo("var a = 1.0");
        }

        @Test
        void printWhile() {
            assertThat(astPrinter.print(_while(literal(true), print(literal(1.0))))).isEqualTo("while true (print 1.0)");
        }

        @Test
        void printFunction() {
            assertThat(astPrinter.print(
                    function("printTwoArgs", List.of(identifier("a"), identifier("b")),
                            List.of(
                                    print(literal("a")),
                                    print(literal("b"))
                            )
                    )
            )).isEqualTo("(fun printTwoArgs (a b) (print a) (print b))");
        }

        @Test
        void printReturnNothing() {
            assertThat(astPrinter.print(_return(null))).isEqualTo("(return)");
        }

        @Test
        void printClassStatement() {
            assertThat(astPrinter.print(
                    _class("Provider", List.of(
                            function("provide", emptyList(), emptyList())
                    ))
            )).isEqualTo("(class Provider (fun provide () ))");
        }

        @Test
        void printClassWithSuperClassStatement() {
            assertThat(astPrinter.print(
                    _class("Rectangle", variable("Shape"), List.of(
                            function("calculateCircumference", emptyList(), emptyList())
                    ))
            )).isEqualTo("(class Rectangle(superClass Shape) (fun calculateCircumference () ))");
        }
    }
}