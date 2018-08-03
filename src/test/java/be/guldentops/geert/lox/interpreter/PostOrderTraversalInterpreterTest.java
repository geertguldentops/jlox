package be.guldentops.geert.lox.interpreter;

import be.guldentops.geert.lox.error.FakeErrorReporter;
import be.guldentops.geert.lox.error.RuntimeError;
import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.grammar.Statement;
import be.guldentops.geert.lox.lexer.Token;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.assign;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.binary;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.call;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.grouping;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.literal;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.logical;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.unary;
import static be.guldentops.geert.lox.grammar.ExpressionTestFactory.variable;
import static be.guldentops.geert.lox.grammar.StatementTestFactory._if;
import static be.guldentops.geert.lox.grammar.StatementTestFactory._return;
import static be.guldentops.geert.lox.grammar.StatementTestFactory._while;
import static be.guldentops.geert.lox.grammar.StatementTestFactory.blockStatement;
import static be.guldentops.geert.lox.grammar.StatementTestFactory.expressionStatement;
import static be.guldentops.geert.lox.grammar.StatementTestFactory.function;
import static be.guldentops.geert.lox.grammar.StatementTestFactory.print;
import static be.guldentops.geert.lox.grammar.StatementTestFactory.variableDeclaration;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.and;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.bang;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.bangEqual;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.equalEqual;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.greater;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.greaterEqual;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.identifier;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.less;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.lessEqual;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.minus;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.nil;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.or;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.plus;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.slash;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.star;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class PostOrderTraversalInterpreterTest {

    private Environment environment;
    private FakeErrorReporter fakeErrorReporter;

    private Interpreter interpreter;

    @BeforeEach
    void setUp() {
        environment = Environment.createGlobal();
        fakeErrorReporter = new FakeErrorReporter();

        interpreter = new PostOrderTraversalInterpreter(environment);
        interpreter.addErrorReporter(fakeErrorReporter);
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

        @Test
        void nonInitializedVariableExpression() {
            environment.define(identifier("a"), null);

            assertThat(interpreter.interpret(variable("a"))).isNull();
        }

        @Test
        void initializedVariableExpression() {
            environment.define(identifier("a"), "2.1");

            assertThat(interpreter.interpret(variable("a"))).isEqualTo("2.1");
        }

        @Test
        void assignmentExpression() {
            environment.define(identifier("a"), null);

            assertThat(interpreter.interpret(assign("a", literal(1.0)))).isEqualTo(1.0);

            assertThat(environment.get(identifier("a"))).isEqualTo(1.0);
        }

        @Test
        void reassignExpression() {
            environment.define(identifier("a"), 2.0);

            assertThat(interpreter.interpret(assign("a", literal(3.0)))).isEqualTo(3.0);

            assertThat(environment.get(identifier("a"))).isEqualTo(3.0);
        }

        @Test
        void orLogicalExpression() {
            assertThat(interpreter.interpret(logical(literal(true), or(), literal(false)))).isEqualTo(true);
            assertThat(interpreter.interpret(logical(literal(false), or(), literal(true)))).isEqualTo(true);
            assertThat(interpreter.interpret(logical(literal(true), or(), literal(true)))).isEqualTo(true);
            assertThat(interpreter.interpret(logical(literal(false), or(), literal(false)))).isEqualTo(false);
            assertThat(interpreter.interpret(logical(literal(1.0), or(), literal(false)))).isEqualTo(1.0);
            assertThat(interpreter.interpret(logical(literal(3.14), or(), literal(false)))).isEqualTo(3.14);
            assertThat(interpreter.interpret(logical(literal("a"), or(), literal(false)))).isEqualTo("a");
            assertThat(interpreter.interpret(logical(literal(null), or(), literal(false)))).isEqualTo(false);
            assertThat(interpreter.interpret(logical(literal(false), or(), literal(1.0)))).isEqualTo(1.0);
            assertThat(interpreter.interpret(logical(literal(false), or(), literal(3.14)))).isEqualTo(3.14);
            assertThat(interpreter.interpret(logical(literal(false), or(), literal("a")))).isEqualTo("a");
            assertThat(interpreter.interpret(logical(literal(false), or(), literal(null)))).isNull();
            assertThat(interpreter.interpret(logical(literal(1.0), or(), literal(3.14)))).isEqualTo(1.0);
            assertThat(interpreter.interpret(logical(literal(3.14), or(), literal("a")))).isEqualTo(3.14);
            assertThat(interpreter.interpret(logical(literal("a"), or(), literal(null)))).isEqualTo("a");
            assertThat(interpreter.interpret(logical(literal(null), or(), literal(null)))).isNull();
        }

        @Test
        void andLogicalExpression() {
            assertThat(interpreter.interpret(logical(literal(true), and(), literal(false)))).isEqualTo(false);
            assertThat(interpreter.interpret(logical(literal(false), and(), literal(true)))).isEqualTo(false);
            assertThat(interpreter.interpret(logical(literal(true), and(), literal(true)))).isEqualTo(true);
            assertThat(interpreter.interpret(logical(literal(false), and(), literal(false)))).isEqualTo(false);
            assertThat(interpreter.interpret(logical(literal(1.0), and(), literal(false)))).isEqualTo(false);
            assertThat(interpreter.interpret(logical(literal(3.14), and(), literal(false)))).isEqualTo(false);
            assertThat(interpreter.interpret(logical(literal("a"), and(), literal(false)))).isEqualTo(false);
            assertThat(interpreter.interpret(logical(literal(null), and(), literal(false)))).isNull();
            assertThat(interpreter.interpret(logical(literal(false), and(), literal(1.0)))).isEqualTo(false);
            assertThat(interpreter.interpret(logical(literal(false), and(), literal(3.14)))).isEqualTo(false);
            assertThat(interpreter.interpret(logical(literal(false), and(), literal("a")))).isEqualTo(false);
            assertThat(interpreter.interpret(logical(literal(false), and(), literal(null)))).isEqualTo(false);
            assertThat(interpreter.interpret(logical(literal(true), and(), literal(1.0)))).isEqualTo(1.0);
            assertThat(interpreter.interpret(logical(literal(1.0), and(), literal(3.14)))).isEqualTo(3.14);
            assertThat(interpreter.interpret(logical(literal(3.14), and(), literal("a")))).isEqualTo("a");
            assertThat(interpreter.interpret(logical(literal("a"), and(), literal(null)))).isNull();
            assertThat(interpreter.interpret(logical(literal(null), and(), literal(null)))).isNull();
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

        @Test
        void runtimeErrorWhenPrinting() {
            interpret(print(binary(literal(5.0), slash(), literal(0.0))));

            assertThat(fakeErrorReporter.receivedRuntimeError()).isTrue();
            assertThat(fakeErrorReporter.getRuntimeError())
                    .isInstanceOf(RuntimeError.class)
                    .hasMessage("Can not divide by zero!");

        }

        @Test
        void variableExpressionWithoutVariableDefined() {
            assertThat(interpreter.interpret(variable("a"))).isNull();

            assertThat(fakeErrorReporter.receivedRuntimeError()).isTrue();
            assertThat(fakeErrorReporter.getRuntimeError())
                    .isInstanceOf(RuntimeError.class)
                    .hasMessage("Undefined variable 'a'.");
        }

        @Test
        void variableStatementWithVariableAlreadyDefined() {
            environment.define(identifier("a"), 1.0);

            interpret(variableDeclaration("a", literal(2.0)));

            assertThat(environment.get(identifier("a"))).isEqualTo(1.0);

            assertThat(fakeErrorReporter.receivedRuntimeError()).isTrue();
            assertThat(fakeErrorReporter.getRuntimeError())
                    .isInstanceOf(RuntimeError.class)
                    .hasMessage("Variable 'a' is already defined.");
        }

        @Test
        void assignmentExpressionWithoutVariableDefined() {
            assertThat(interpreter.interpret(assign("a", literal(1.0)))).isNull();

            assertThat(fakeErrorReporter.receivedRuntimeError()).isTrue();
            assertThat(fakeErrorReporter.getRuntimeError())
                    .isInstanceOf(RuntimeError.class)
                    .hasMessage("Undefined variable 'a'.");
        }

        @Test
        void cannotAssignToNonExistingVariable() {
            interpret(
                    blockStatement(
                            expressionStatement(assign("a", literal(1.0)))
                    )
            );

            assertThat(fakeErrorReporter.receivedRuntimeError()).isTrue();
            assertThat(fakeErrorReporter.getRuntimeError())
                    .isInstanceOf(RuntimeError.class)
                    .hasMessage("Undefined variable 'a'.");
        }

        @Test
        void cannotCallNonExistingFunction() {
            interpret(
                    expressionStatement(call("function that does not exist"))
            );

            assertThat(fakeErrorReporter.receivedRuntimeError()).isTrue();
            assertThat(fakeErrorReporter.getRuntimeError())
                    .isInstanceOf(RuntimeError.class)
                    .hasMessage("Undefined variable 'function that does not exist'.");
        }

        @Test
        void cannotCallNonFunction() {
            interpret(
                    variableDeclaration("totally not a function", literal(true)),
                    expressionStatement(call("totally not a function"))
            );

            assertThat(fakeErrorReporter.receivedRuntimeError()).isTrue();
            assertThat(fakeErrorReporter.getRuntimeError())
                    .isInstanceOf(RuntimeError.class)
                    .hasMessage("Can only call functions and classes.");
        }

        @Test
        void cannotCallFunctionWithNotEnoughArguments() {
            interpret(
                    function("twoArgumentsFunction", List.of(identifier("a"), identifier("b")),
                            List.of(
                                    print(literal("Hello world"))
                            )
                    ),
                    expressionStatement(call("twoArgumentsFunction", literal(1.0)))
            );

            assertThat(fakeErrorReporter.receivedRuntimeError()).isTrue();
            assertThat(fakeErrorReporter.getRuntimeError())
                    .isInstanceOf(RuntimeError.class)
                    .hasMessage("Expected 2 argument(s) but got 1.");
        }

        @Test
        void cannotCallFunctionWithTooManyArguments() {
            interpret(
                    function("singleArgumentsFunction", List.of(identifier("a")),
                            List.of(
                                    print(literal("Hello world"))
                            )
                    ),
                    expressionStatement(call("singleArgumentsFunction", literal(1.0), literal(2.0)))
            );

            assertThat(fakeErrorReporter.receivedRuntimeError()).isTrue();
            assertThat(fakeErrorReporter.getRuntimeError())
                    .isInstanceOf(RuntimeError.class)
                    .hasMessage("Expected 1 argument(s) but got 2.");
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

    /**
     * To test block statements we would need to get hold of the block statement's local scope. By observing it correctly
     * manipulates this scope and its outer scopes during variable look-up we can verify that it behaves correctly.
     * <p>
     * However, by definition, local scope is local to the block and is not observable from outside!
     * <p>
     * The work-around used here is to print variables at strategic times so we can <strong>infer</strong> that the correct scope rules were followed!
     */
    @Nested
    class BlockStatementCases {

        private PrintStream originalOut;
        private ByteArrayOutputStream outContent;

        @BeforeEach
        void setUp() {
            originalOut = System.out;
            outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
        }

        @AfterEach
        void tearDown() {
            System.setOut(originalOut);
        }

        @Test
        void resolveVariableInLocalScope() {
            interpret(
                    blockStatement(
                            variableDeclaration("a", literal(1.0)),
                            print(variable("a"))
                    )
            );

            assertThat(outContent.toString()).isEqualTo("1\n");
        }

        @Test
        void canAssignVariableFromGlobalScope() {
            interpret(
                    variableDeclaration("a"),
                    blockStatement(
                            expressionStatement(assign("a", literal(1.0))),
                            print(variable("a"))
                    )
            );

            assertThat(outContent.toString()).isEqualTo("1\n");
        }

        @Test
        void canAssignVariableFromOuterScope() {
            interpret(
                    blockStatement(
                            variableDeclaration("a"),
                            blockStatement(
                                    expressionStatement(assign("a", literal(1.0))),
                                    print(variable("a"))
                            )
                    )
            );

            assertThat(outContent.toString()).isEqualTo("1\n");
        }

        @Test
        void resolveVariableInGlobalScope() {
            interpret(
                    variableDeclaration("a", literal(1.0)),
                    blockStatement(
                            print(variable("a"))
                    )
            );

            assertThat(outContent.toString()).isEqualTo("1\n");
        }

        @Test
        void resolveVariableInOuterBlockScope() {
            interpret(
                    blockStatement(
                            variableDeclaration("a", literal(1.0)),
                            blockStatement(
                                    print(variable("a"))
                            )
                    )
            );

            assertThat(outContent.toString()).isEqualTo("1\n");
        }

        @Test
        void localVariableShadowsOuterVariable() {
            interpret(
                    variableDeclaration("a", literal(2.0)),
                    blockStatement(
                            variableDeclaration("a", literal(1.0)),
                            print(variable("a"))
                    )
            );

            assertThat(outContent.toString()).isEqualTo("1\n");
        }

        @Test
        void ignoreVariableFromOutOfScopeBlock() {
            interpret(
                    variableDeclaration("a", literal(2.0)),
                    blockStatement(
                            variableDeclaration("a", literal(1.0))
                    ),
                    print(variable("a"))
            );

            assertThat(outContent.toString()).isEqualTo("2\n");
        }
    }

    @Nested
    class PrintStatementCases {

        private PrintStream originalOut;
        private ByteArrayOutputStream outContent;

        @BeforeEach
        void setUp() {
            originalOut = System.out;
            outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
        }

        @AfterEach
        void tearDown() {
            System.setOut(originalOut);
        }

        @Test
        void printInteger() {
            interpret(print(literal(1.0)));

            assertThat(outContent.toString()).isEqualTo("1\n");
        }

        @Test
        void printFloatingPointNumber() {
            interpret(print(literal(3.14)));

            assertThat(outContent.toString()).isEqualTo("3.14\n");
        }

        @Test
        void printString() {
            interpret(print(literal("Hello, world!")));

            assertThat(outContent.toString()).isEqualTo("Hello, world!\n");
        }

        @Test
        void printTrue() {
            interpret(print(literal(true)));

            assertThat(outContent.toString()).isEqualTo("true\n");
        }

        @Test
        void printFalse() {
            interpret(print(literal(false)));

            assertThat(outContent.toString()).isEqualTo("false\n");
        }

        @Test
        void printNil() {
            interpret(print(literal(null)));

            assertThat(outContent.toString()).isEqualTo("nil\n");
        }

        @Test
        void printSmallProgram() {
            interpret(
                    print(literal(1.0)),
                    print(grouping(binary(literal(2.0), plus(), literal(3.0)))),
                    print(unary(minus(), literal(3.14)))
            );

            assertThat(outContent.toString()).isEqualTo("1\n5\n-3.14\n");
        }
    }

    @Nested
    class VariableStatementCases {

        @Test
        void defineVariableWithoutInitializer() {
            interpret(variableDeclaration("a"));

            assertThat(environment.get(identifier("a"))).isNull();
        }

        @Test
        void defineVariableWithInitializer() {
            interpret(variableDeclaration("a", literal(1.0)));

            assertThat(environment.get(identifier("a"))).isEqualTo(1.0);
        }
    }

    @Nested
    class IfStatementCases {

        private PrintStream originalOut;
        private ByteArrayOutputStream outContent;

        @BeforeEach
        void setUp() {
            originalOut = System.out;
            outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
        }

        @AfterEach
        void tearDown() {
            System.setOut(originalOut);
        }

        @Test
        void ifTruthyThenExecuteThenBranch() {
            interpret(_if(literal(true), print(literal(1.0)), null));

            assertThat(outContent.toString()).isEqualTo("1\n");
        }

        @Test
        void ifFalseyThenDoNothing() {
            interpret(_if(literal(false), print(literal(1.0)), null));

            assertThat(outContent.toString()).isBlank();
        }

        @Test
        void ifElseTruthyThenExecuteThenBranch() {
            interpret(_if(literal(true), print(literal(1.0)), print(literal(2.0))));

            assertThat(outContent.toString()).isEqualTo("1\n");
        }

        @Test
        void ifElseFalseyThenExecuteElseBranch() {
            interpret(_if(literal(false), print(literal(1.0)), print(literal(2.0))));

            assertThat(outContent.toString()).isEqualTo("2\n");
        }
    }

    @Nested
    class WhileStatementCases {

        private PrintStream originalOut;
        private ByteArrayOutputStream outContent;

        @BeforeEach
        void setUp() {
            originalOut = System.out;
            outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
        }

        @AfterEach
        void tearDown() {
            System.setOut(originalOut);
        }

        @Test
        void whileTruthyThenExecuteBody() {
            interpret(
                    variableDeclaration("a", literal(1.0)),
                    _while(binary(variable("a"), equalEqual(), literal(1.0)),
                            blockStatement(
                                    print(variable("a")),
                                    expressionStatement(assign("a", literal(2.0)))
                            )
                    )
            );

            assertThat(outContent.toString()).isEqualTo("1\n");
        }

        @Test
        void whileFalseyThenDoNothing() {
            interpret(_while(literal(false), print(literal(1.0))));

            assertThat(outContent.toString()).isBlank();
        }
    }

    @Nested
    class CallFunctionCases {

        private PrintStream originalOut;
        private ByteArrayOutputStream outContent;

        @BeforeEach
        void setUp() {
            originalOut = System.out;
            outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
        }

        @AfterEach
        void tearDown() {
            System.setOut(originalOut);
        }

        @Test
        void canCallDefinedFunctionWithNoArguments() {
            interpret(
                    function("printNoArgs", emptyList(),
                            List.of(
                                    print(literal(2.0))
                            )
                    ),
                    expressionStatement(call("printNoArgs"))
            );

            assertThat(outContent.toString()).isEqualTo("2\n");
        }

        @Test
        void canCallFunctionWithOneArgument() {
            interpret(
                    function("printSingleArgument", List.of(identifier("a")),
                            List.of(
                                    print(variable("a"))
                            )
                    ),
                    expressionStatement(call("printSingleArgument", literal(5.0)))
            );

            assertThat(outContent.toString()).isEqualTo("5\n");
        }

        @Test
        void canCallFunctionWithMultipleArguments() {
            interpret(
                    function("printSummedArguments", List.of(identifier("a"), identifier("b")),
                            List.of(
                                    print(binary(variable("a"), plus(), variable("b")))
                            )
                    ),
                    expressionStatement(call("printSummedArguments", literal(2.0), literal(4.0)))
            );

            assertThat(outContent.toString()).isEqualTo("6\n");
        }

        @Test
        void canCallFunctionThatClosesOverVariableInOuterBlockScope() {
            interpret(
                    function("makeCounter", emptyList(),
                            List.of(
                                    variableDeclaration("i", literal(0.0)),
                                    function("count", emptyList(),
                                            List.of(
                                                    expressionStatement(assign("i", binary(variable("i"), plus(), literal(1.0)))),
                                                    print(variable("i"))
                                            )
                                    ),
                                    _return(variable("count"))
                            )
                    ),
                    variableDeclaration("counter", call("makeCounter")),
                    expressionStatement(call("counter")),
                    expressionStatement(call("counter"))
            );

            assertThat(outContent.toString()).isEqualTo("1\n2\n");
        }
    }

    @Nested
    class ReturnStatementCases {

        private PrintStream originalOut;
        private ByteArrayOutputStream outContent;

        @BeforeEach
        void setUp() {
            originalOut = System.out;
            outContent = new ByteArrayOutputStream();
            System.setOut(new PrintStream(outContent));
        }

        @AfterEach
        void tearDown() {
            System.setOut(originalOut);
        }

        @Test
        void canCallFunctionThatReturnsValue() {
            interpret(
                    function("returnTwo", emptyList(),
                            List.of(
                                    _return(literal(2.0))
                            )
                    ),
                    print(call("returnTwo"))
            );

            assertThat(outContent.toString()).isEqualTo("2\n");
        }

        @Test
        void canCallFunctionThatReturnsNothing() {
            interpret(
                    function("returnNothing", emptyList(),
                            List.of(
                                    _return(null)
                            )
                    ),
                    print(call("returnNothing"))
            );

            assertThat(outContent.toString()).isEqualTo("nil\n");
        }

        @Test
        void canCallFunctionThatDoesNotReturn() {
            interpret(
                    function("noReturn", emptyList(),
                            emptyList()
                    ),
                    print(call("noReturn"))
            );

            assertThat(outContent.toString()).isEqualTo("nil\n");
        }

        @Test
        void canReturnFromNestedBlockWithinFunction() {
            interpret(
                    function("aFunction", emptyList(),
                            List.of(
                                    variableDeclaration("a", literal(1.0)),
                                    _while(binary(variable("a"), equalEqual(), literal(1.0)),
                                            blockStatement(
                                                    _if(literal(true), _return(literal(42.0)), null),
                                                    expressionStatement(assign("a", literal(2.0)))
                                            )
                                    )
                            )
                    ),
                    print(call("aFunction"))
            );

            assertThat(outContent.toString()).isEqualTo("42\n");
        }
    }

    private void interpret(Statement... statements) {
        interpreter.interpret(List.of(statements));
    }

}