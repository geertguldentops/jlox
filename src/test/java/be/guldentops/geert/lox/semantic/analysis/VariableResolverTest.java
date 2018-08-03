package be.guldentops.geert.lox.semantic.analysis;

import be.guldentops.geert.lox.error.FakeErrorReporter;
import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.grammar.Statement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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
import static be.guldentops.geert.lox.lexer.TokenObjectMother.identifier;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.minus;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.or;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.plus;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.entry;

class VariableResolverTest {

    private FakeResolutionAnalyzer fakeResolutionAnalyzer;
    private FakeErrorReporter fakeErrorReporter;

    private Resolver resolver;

    @BeforeEach
    void setUp() {
        fakeResolutionAnalyzer = new FakeResolutionAnalyzer();
        fakeErrorReporter = new FakeErrorReporter();
        resolver = new VariableResolver(fakeResolutionAnalyzer);
        resolver.addErrorReporter(fakeErrorReporter);
    }

    @Nested
    class DegenerateCases {

        @Test
        void resolveNull() {
            assertThatThrownBy(() -> resolver.resolve(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Can not resolve null statements");

            assertNoErrors();
        }

        @Test
        void resolveEmptyStatements() {
            resolver.resolve(emptyList());

            assertThat(fakeResolutionAnalyzer.depthPerExpression).isEmpty();
            assertNoErrors();
        }
    }

    @Nested
    class ResolveToNothing {

        @Test
        void literalExpressionsResolve() {
            resolve(expressionStatement(literal(1.0)));

            assertResolvedToNothing();
        }

        @Test
        void groupExpressionsResolve() {
            resolve(expressionStatement(grouping(literal(1.0))));

            assertResolvedToNothing();
        }

        @Test
        void logicalExpressionsResolve() {
            resolve(expressionStatement(logical(literal(true), or(), literal(false))));

            assertResolvedToNothing();
        }

        @Test
        void binaryExpressionsResolve() {
            resolve(expressionStatement(binary(literal(8.0), plus(), literal(12.0))));

            assertResolvedToNothing();
        }

        @Test
        void unaryExpressionsResolve() {
            resolve(expressionStatement(unary(minus(), literal(1.0))));

            assertResolvedToNothing();
        }

        @Test
        void ifStatementsResolve() {
            resolve(_if(literal(true), print(literal(1.0)), null));

            assertResolvedToNothing();
        }

        @Test
        void printStatementsResolve() {
            resolve(print(literal(1.0)));

            assertResolvedToNothing();
        }

        @Test
        void whileStatementsResolve() {
            resolve(_while(literal(true), print(literal(1.0))));

            assertResolvedToNothing();
        }
    }

    @Nested
    class VariableExpression {

        @Test
        void variableExpressionResolveToGlobalScope() {
            resolve(
                    variableDeclaration("a"),
                    expressionStatement(variable("a"))
            );

            assertResolvedToNothing();
        }

        @Test
        void variableExpressionResolveToLocalScope() {
            var variable = variable("a");

            resolve(
                    blockStatement(
                            variableDeclaration("a"),
                            expressionStatement(variable)
                    )
            );

            assertResolvedToDepth(variable, 0);
        }

        @Test
        void variableExpressionResolveToOuterNonGlobalScope() {
            var variable = variable("a");

            resolve(
                    blockStatement(
                            variableDeclaration("a"),
                            blockStatement(
                                    expressionStatement(variable)
                            )
                    )
            );

            assertResolvedToDepth(variable, 1);
        }

        @Test
        void variableExpressionResolveToDoubleOuterNonGlobalScope() {
            var variable = variable("a");

            resolve(
                    blockStatement(
                            variableDeclaration("a"),
                            blockStatement(
                                    blockStatement(
                                            expressionStatement(variable)
                                    )
                            )
                    )
            );

            assertResolvedToDepth(variable, 2);
        }

        @Test
        void nestedScopeVariableExpressionResolveToGlobalScope() {
            var variable = variable("a");

            resolve(
                    variableDeclaration("a"),
                    blockStatement(
                            blockStatement(
                                    blockStatement(
                                            expressionStatement(variable)
                                    )
                            )
                    )
            );

            assertResolvedToNothing();
        }

        @Test
        void canUseVariableInItsOwnInitializerInGlobalScope() {
            resolve(
                    variableDeclaration("a", variable("a"))
            );

            assertResolvedToNothing();
        }

        @Test
        void canNotUseVariableInItsOwnInitializerInGlobalScope() {
            var variable = variable("a");

            resolve(
                    blockStatement(
                            variableDeclaration("a", variable)
                    )
            );

            assertThat(fakeErrorReporter.receivedError()).isTrue();
            assertThat(fakeErrorReporter.getError().line).isEqualTo(1);
            assertThat(fakeErrorReporter.getError().location).isEqualTo("a");
            assertThat(fakeErrorReporter.getError().message).isEqualTo("Cannot read local variable in its own initializer.");
            assertThat(fakeErrorReporter.receivedRuntimeError()).isFalse();
            assertThat(fakeResolutionAnalyzer.depthPerExpression)
                    .hasSize(1)
                    .contains(entry(variable, 0));
        }
    }

    @Nested
    class AssignExpression {

        @Test
        void assignExpressionResolveToGlobalScope() {
            resolve(
                    variableDeclaration("a"),
                    expressionStatement(assign("a", literal(1.0)))
            );

            assertResolvedToNothing();
        }

        @Test
        void assignExpressionResolveToLocalScope() {
            var assign = assign("a", literal(1.0));

            resolve(
                    blockStatement(
                            variableDeclaration("a"),
                            expressionStatement(assign)
                    )
            );

            assertResolvedToDepth(assign, 0);
        }

        @Test
        void assignExpressionResolveToOuterNonGlobalScope() {
            var assign = assign("a", literal(1.0));

            resolve(
                    blockStatement(
                            variableDeclaration("a"),
                            blockStatement(
                                    expressionStatement(assign)
                            )
                    )
            );

            assertResolvedToDepth(assign, 1);
        }

        @Test
        void assignExpressionResolveToDoubleOuterNonGlobalScope() {
            var assign = assign("a", literal(1.0));

            resolve(
                    blockStatement(
                            variableDeclaration("a"),
                            blockStatement(
                                    blockStatement(
                                            expressionStatement(assign)
                                    )
                            )
                    )
            );

            assertResolvedToDepth(assign, 2);
        }

        @Test
        void nestedScopeAssignExpressionResolveToGlobalScope() {
            var assign = assign("a", literal(1.0));

            resolve(
                    variableDeclaration("a"),
                    blockStatement(
                            blockStatement(
                                    blockStatement(
                                            expressionStatement(assign)
                                    )
                            )
                    )
            );

            assertResolvedToNothing();
        }
    }

    @Nested
    class CallFunctionStatement {

        @Test
        void callFunctionWithoutParametersResolveToGlobalScope() {
            resolve(
                    function("printNoArgs", emptyList(),
                            List.of(
                                    print(literal(2.0))
                            )
                    ),
                    expressionStatement(call("printNoArgs"))
            );

            assertResolvedToNothing();
        }

        @Test
        void callFunctionWithParametersResolveToGlobalScope() {
            resolve(
                    function("printSingleArgument", List.of(identifier("a")),
                            List.of(
                                    print(literal(1.0))
                            )
                    ),
                    expressionStatement(call("printSingleArgument", literal(2.0)))
            );

            assertResolvedToNothing();
        }

        @Test
        void callFunctionWithoutParametersResolveToLocalScope() {
            var printNoArgs = variable("printNoArgs");

            resolve(
                    blockStatement(
                            function("printNoArgs", emptyList(),
                                    List.of(
                                            print(literal(2.0))
                                    )
                            ),
                            expressionStatement(call(printNoArgs))
                    )
            );

            assertNoErrors();
            assertThat(fakeResolutionAnalyzer.depthPerExpression)
                    .hasSize(1)
                    .contains(entry(printNoArgs, 0));
        }

        @Test
        void callFunctionWithParametersResolveToLocalScope() {
            var printSingleArgument = variable("printSingleArgument");

            resolve(
                    blockStatement(
                            function("printSingleArgument", List.of(identifier("a")),
                                    List.of(
                                            print(literal(1.0))
                                    )
                            ),
                            expressionStatement(call(printSingleArgument, literal(2.0)))
                    )
            );

            assertNoErrors();
            assertThat(fakeResolutionAnalyzer.depthPerExpression)
                    .hasSize(1)
                    .contains(entry(printSingleArgument, 0));
        }

        @Test
        void callFunctionWithoutParametersResolveToOuterLocalScope() {
            var printNoArgs = variable("printNoArgs");

            resolve(
                    blockStatement(
                            function("printNoArgs", emptyList(),
                                    List.of(
                                            print(literal(2.0))
                                    )
                            ),
                            blockStatement(
                                    expressionStatement(call(printNoArgs))
                            )
                    )
            );

            assertNoErrors();
            assertThat(fakeResolutionAnalyzer.depthPerExpression)
                    .hasSize(1)
                    .contains(entry(printNoArgs, 1));
        }

        @Test
        void callFunctionWithParametersResolveToOuterLocalScope() {
            var printSingleArgument = variable("printSingleArgument");

            resolve(
                    blockStatement(
                            function("printSingleArgument", List.of(identifier("a")),
                                    List.of(
                                            print(literal(1.0))
                                    )
                            ),
                            blockStatement(
                                    expressionStatement(call(printSingleArgument, literal(2.0)))
                            )
                    )
            );

            assertNoErrors();
            assertThat(fakeResolutionAnalyzer.depthPerExpression)
                    .hasSize(1)
                    .contains(entry(printSingleArgument, 1));
        }

        @Test
        void callFunctionWithoutParametersResolveToOuterGlobalScope() {
            var printNoArgs = variable("printNoArgs");

            resolve(
                    function("printNoArgs", emptyList(),
                            List.of(
                                    print(literal(2.0))
                            )
                    ),
                    blockStatement(
                            expressionStatement(call(printNoArgs))
                    )
            );

            assertNoErrors();
        }

        @Test
        void callFunctionWithParametersResolveToOuterGlobalScope() {
            var printSingleArgument = variable("printSingleArgument");

            resolve(
                    function("printSingleArgument", List.of(identifier("a")),
                            List.of(
                                    print(literal(1.0))
                            )
                    ),
                    blockStatement(
                            expressionStatement(call(printSingleArgument, literal(2.0)))
                    )
            );

            assertNoErrors();
        }

        @Test
        void callFunctionWithoutParametersResolveToNestedOuterLocalScope() {
            var printNoArgs = variable("printNoArgs");

            resolve(
                    blockStatement(
                            function("printNoArgs", emptyList(),
                                    List.of(
                                            print(literal(2.0))
                                    )
                            ),
                            blockStatement(
                                    blockStatement(
                                            expressionStatement(call(printNoArgs))
                                    )
                            )
                    )
            );

            assertNoErrors();
            assertThat(fakeResolutionAnalyzer.depthPerExpression)
                    .hasSize(1)
                    .contains(entry(printNoArgs, 2));
        }

        @Test
        void callFunctionWithParametersResolveToNestedOuterLocalScope() {
            var printSingleArgument = variable("printSingleArgument");

            resolve(
                    blockStatement(
                            function("printSingleArgument", List.of(identifier("a")),
                                    List.of(
                                            print(literal(1.0))
                                    )
                            ),
                            blockStatement(
                                    blockStatement(
                                            expressionStatement(call(printSingleArgument, literal(2.0)))
                                    )
                            )
                    )
            );

            assertNoErrors();
            assertThat(fakeResolutionAnalyzer.depthPerExpression)
                    .hasSize(1)
                    .contains(entry(printSingleArgument, 2));
        }
    }

    // visitBlockStatement and visitExpressionStatement are tested indirectly by variable and assign expression!

    @Nested
    class ReturnStatement {

        @Test
        void returnNothingResolveToNothing() {
            resolve(
                    function("returnNothing", emptyList(),
                            List.of(
                                    _return(null)
                            )
                    ),
                    expressionStatement(call("returnNothing"))
            );

            assertResolvedToNothing();
        }

        @Test
        void returnSomethingResolveToNothing() {
            resolve(
                    function("returnSomething", emptyList(),
                            List.of(
                                    _return(literal("something"))
                            )
                    ),
                    expressionStatement(call("returnSomething"))
            );

            assertResolvedToNothing();
        }

        @Test
        void canNotReturnFromGlobalScope() {
            resolve(
                    _return(literal("at top level"))
            );

            assertThat(fakeErrorReporter.receivedError()).isTrue();
            assertThat(fakeErrorReporter.getError().line).isEqualTo(1);
            assertThat(fakeErrorReporter.getError().location).isEqualTo("return");
            assertThat(fakeErrorReporter.getError().message).isEqualTo("Cannot return from top-level code.");
            assertThat(fakeErrorReporter.receivedRuntimeError()).isFalse();
            assertThat(fakeResolutionAnalyzer.depthPerExpression).isEmpty();
        }
    }

    @Nested
    class VariableStatement {

        // visitVariableStatement is largely tested indirectly by variable and assign expression!

        @Test
        void canNotDeclareLocalVariableTwice() {
            resolve(
                    blockStatement(
                            variableDeclaration("a", literal("first")),
                            variableDeclaration("a", literal("second"))
                    )
            );

            assertThat(fakeErrorReporter.receivedError()).isTrue();
            assertThat(fakeErrorReporter.getError().line).isEqualTo(1);
            assertThat(fakeErrorReporter.getError().location).isEqualTo("a");
            assertThat(fakeErrorReporter.getError().message).isEqualTo("Variable with this name already declared in this scope.");
            assertThat(fakeErrorReporter.receivedRuntimeError()).isFalse();
            assertThat(fakeResolutionAnalyzer.depthPerExpression).isEmpty();
        }

        @Test
        void canDeclareGlobalVariableTwice() {
            resolve(
                    variableDeclaration("a", literal("first")),
                    variableDeclaration("a", literal("second"))
            );

            assertResolvedToNothing();
        }
    }

    // visitFunctionStatement is indirectly tested by call!

    private void resolve(Statement... statements) {
        resolver.resolve(List.of(statements));
    }

    private void assertResolvedToNothing() {
        assertNoErrors();

        assertThat(fakeResolutionAnalyzer.depthPerExpression).isEmpty();
    }

    private void assertResolvedToDepth(Expression expression, int depth) {
        assertNoErrors();

        assertThat(fakeResolutionAnalyzer.depthPerExpression)
                .hasSize(1)
                .contains(entry(expression, depth));
    }

    private void assertNoErrors() {
        assertThat(fakeErrorReporter.receivedError()).isFalse();
        assertThat(fakeErrorReporter.receivedRuntimeError()).isFalse();
    }
}