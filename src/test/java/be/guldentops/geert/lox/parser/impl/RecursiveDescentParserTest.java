package be.guldentops.geert.lox.parser.impl;

import be.guldentops.geert.lox.error.impl.FakeErrorReporter;
import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.grammar.Statement;
import be.guldentops.geert.lox.lexer.api.Token;
import be.guldentops.geert.lox.parser.api.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static be.guldentops.geert.lox.lexer.api.Token.Type.STRING;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother._false;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother._true;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.bang;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.bangEqual;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.eof;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.equal;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.equalEqual;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.greater;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.greaterEqual;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.identifier;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.leftBrace;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.leftParen;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.less;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.lessEqual;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.minus;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.nil;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.one;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.pi;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.plus;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.print;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.rightBrace;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.rightParen;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.semicolon;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.slash;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.star;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.two;
import static be.guldentops.geert.lox.lexer.api.TokenObjectMother.var;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;

class RecursiveDescentParserTest {

    private FakeErrorReporter fakeErrorReporter;

    @BeforeEach
    void setUp() {
        fakeErrorReporter = new FakeErrorReporter();
    }

    @Nested
    class DegenerateCases {

        @Test
        void noTokens() {
            var parser = createParser((List) null);

            var statements = parser.parse();

            assertThat(statements).isEmpty();

            assertThat(fakeErrorReporter.receivedError()).isFalse();
        }

        @Test
        void emptyListOfTokens() {
            var parser = createParser(emptyList());

            var statements = parser.parse();

            assertThat(statements).isEmpty();

            assertThat(fakeErrorReporter.receivedError()).isFalse();
        }
    }

    @Nested
    class LiteralExpressions {

        @Test
        void trueToken() {
            var parser = createParser(_true(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertLiteralExpression(expression, true);
        }

        @Test
        void falseToken() {
            var parser = createParser(_false(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertLiteralExpression(expression, false);
        }

        @Test
        void nilToken() {
            var parser = createParser(nil(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertLiteralExpression(expression, null);
        }

        @Test
        void integerNumberToken() {
            var parser = createParser(one(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertLiteralExpression(expression, 1.0);
        }

        @Test
        void floatingPointNumberToken() {
            var parser = createParser(pi(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertLiteralExpression(expression, 3.14);
        }

        @Test
        void stringToken() {
            var parser = createParser(new Token(STRING, "\"Hello\"", "Hello", 1), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertLiteralExpression(expression, "Hello");
        }
    }

    @Nested
    class GroupingExpressions {

        @Test
        void leftAndRightParenWithLiteralSubExpression() {
            var parser = createParser(leftParen(), one(), rightParen(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertThat(expression).isInstanceOf(Expression.Grouping.class);
            var groupingExpression = (Expression.Grouping) expression;

            assertThat(groupingExpression.expression).isNotNull();
            assertLiteralExpression(groupingExpression.expression, 1.0);
        }
    }

    @Nested
    class VariableExpressions {

        @Test
        void identifierToken() {
            var parser = createParser(identifier("a"), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertVariableExpression(expression, identifier("a"));
        }
    }

    @Nested
    class UnaryExpressions {

        @Test
        void bangTokenFollowedByLiteralToken() {
            var parser = createParser(bang(), _false(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertThat(expression).isInstanceOf(Expression.Unary.class);
            var unaryExpression = (Expression.Unary) expression;

            assertThat(unaryExpression.operator).isEqualToComparingFieldByField(bang());
            assertLiteralExpression(unaryExpression.right, false);
        }

        @Test
        void minusTokenFollowedByLiteralToken() {
            var parser = createParser(minus(), one(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertThat(expression).isInstanceOf(Expression.Unary.class);
            var unaryExpression = (Expression.Unary) expression;

            assertThat(unaryExpression.operator).isEqualToComparingFieldByField(minus());
            assertLiteralExpression(unaryExpression.right, 1.0);
        }

        @Test
        void multipleUnaryOperations() {
            var parser = createParser(bang(), bang(), bang(), _false(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertThat(expression).isInstanceOf(Expression.Unary.class);
            var farLeftUnaryExpression = (Expression.Unary) expression;
            assertThat(farLeftUnaryExpression.operator).isEqualToComparingFieldByField(bang());

            assertThat(farLeftUnaryExpression.right).isInstanceOf(Expression.Unary.class);
            var leftUnaryExpression = (Expression.Unary) farLeftUnaryExpression.right;
            assertThat(leftUnaryExpression.operator).isEqualToComparingFieldByField(bang());

            assertThat(leftUnaryExpression).isInstanceOf(Expression.Unary.class);
            var unaryExpression = (Expression.Unary) leftUnaryExpression.right;

            assertThat(unaryExpression.operator).isEqualToComparingFieldByField(bang());
            assertLiteralExpression(unaryExpression.right, false);
        }
    }

    @Nested
    class BinaryExpressions {

        @Test
        void slashTokenWithLeftAndRightOperands() {
            var parser = createParser(one(), slash(), two(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertBinaryExpression(expression, 1.0, slash(), 2.0);
        }

        @Test
        void starTokenWithLeftAndRightOperands() {
            var parser = createParser(one(), star(), two(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertBinaryExpression(expression, 1.0, star(), 2.0);
        }

        @Test
        void minusTokenWithLeftAndRightOperands() {
            var parser = createParser(one(), minus(), two(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertBinaryExpression(expression, 1.0, minus(), 2.0);
        }

        @Test
        void plusTokenWithLeftAndRightOperands() {
            var parser = createParser(one(), plus(), two(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertBinaryExpression(expression, 1.0, plus(), 2.0);
        }

        @Test
        void greaterTokenWithLeftAndRightOperands() {
            var parser = createParser(one(), greater(), two(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertBinaryExpression(expression, 1.0, greater(), 2.0);
        }

        @Test
        void greaterEqualTokenWithLeftAndRightOperands() {
            var parser = createParser(one(), greaterEqual(), two(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertBinaryExpression(expression, 1.0, greaterEqual(), 2.0);
        }

        @Test
        void lessTokenWithLeftAndRightOperands() {
            var parser = createParser(one(), less(), two(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertBinaryExpression(expression, 1.0, less(), 2.0);
        }

        @Test
        void lessEqualTokenWithLeftAndRightOperands() {
            var parser = createParser(one(), lessEqual(), two(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertBinaryExpression(expression, 1.0, lessEqual(), 2.0);
        }

        @Test
        void bangEqualTokenWithLeftAndRightOperands() {
            var parser = createParser(one(), bangEqual(), two(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertBinaryExpression(expression, 1.0, bangEqual(), 2.0);
        }

        @Test
        void equalEqualTokenWithLeftAndRightOperands() {
            var parser = createParser(one(), equalEqual(), two(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertBinaryExpression(expression, 1.0, equalEqual(), 2.0);
        }

        @Test
        void multipleBinaryOperand() {
            var parser = createParser(one(), star(), two(), plus(), pi(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertThat(expression).isInstanceOf(Expression.Binary.class);
            var firstBinaryExpression = (Expression.Binary) expression;

            assertThat(firstBinaryExpression.left).isInstanceOf(Expression.Binary.class);
            var secondBinaryExpression = (Expression.Binary) firstBinaryExpression.left;

            assertLiteralExpression(secondBinaryExpression.left, 1.0);
            assertThat(secondBinaryExpression.operator).isEqualToComparingFieldByField(star());
            assertLiteralExpression(secondBinaryExpression.right, 2.0);

            assertThat(firstBinaryExpression.operator).isEqualToComparingFieldByField(plus());
            assertLiteralExpression(firstBinaryExpression.right, 3.14);
        }
    }

    @Nested
    class PrintStatement {

        @Test
        void printLiteralExpressionBoolean() {
            var parser = createParser(print(), _false(), semicolon(), eof());

            var statement = extractOnlyStatementFrom(parser.parse());

            assertPrintStatement(statement, false);
        }

        @Test
        void printLiteralExpressionNumber() {
            var parser = createParser(print(), pi(), semicolon(), eof());

            var statement = extractOnlyStatementFrom(parser.parse());

            assertPrintStatement(statement, 3.14);
        }

        @Test
        void printLiteralExpressionString() {
            var parser = createParser(print(), new Token(STRING, "\"Hello\"", "Hello", 1), semicolon(), eof());

            var statement = extractOnlyStatementFrom(parser.parse());

            assertPrintStatement(statement, "Hello");
        }

    }

    @Nested
    class VariableDeclaration {

        @Test
        void variableWithoutInitializer() {
            var parser = createParser(var(), identifier("a"), semicolon(), eof());

            var statement = extractOnlyStatementFrom(parser.parse());

            assertUninitializedVariable(statement, identifier("a"));
        }

        @Test
        void variableWithInitializer() {
            var parser = createParser(var(), identifier("a"), equal(), one(), semicolon(), eof());

            var variableStatement = extractOnlyStatementFrom(parser.parse());

            assertVariableStatement(variableStatement, identifier("a"), 1.0);
        }

        private void assertUninitializedVariable(Statement statement, Token name) {
            assertThat(statement).isInstanceOf(Statement.Variable.class);
            var variableDeclaration = (Statement.Variable) statement;
            assertThat(variableDeclaration.name).isEqualToComparingFieldByField(name);
            assertThat(variableDeclaration.initializer).isNull();
        }
    }

    @Nested
    class AssignmentExpressions {

        @Test
        void assignUninitializedVariable() {
            var parser = createParser(
                    var(), identifier("a"), semicolon(),
                    identifier("a"), equal(), one(), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(2);
            // Ignore the first statement, it has been tested elsewhere.
            assertAssignedWithValue(statements.get(1), identifier("a"), 1.0);
        }

        @Test
        void reassignVariable() {
            var parser = createParser(
                    var(), identifier("a"), equal(), one(), semicolon(),
                    identifier("a"), equal(), two(), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(2);
            // Ignore the first statement, it has been tested elsewhere.
            assertAssignedWithValue(statements.get(1), identifier("a"), 2.0);
        }

        private void assertAssignedWithValue(Statement statement, Token name, Object expected) {
            var expression = extractExpressionFrom(statement);

            assertThat(expression).isInstanceOf(Expression.Assign.class);
            var assignExpression = (Expression.Assign) expression;

            assertThat(assignExpression.name).isEqualToComparingFieldByField(name);
            assertLiteralExpression(assignExpression.value, expected);
        }
    }

    @Nested
    class BlockStatement {

        @Test
        void blockWithOneStatement() {
            var parser = createParser(
                    leftBrace(),
                    var(), identifier("a"), equal(), one(), semicolon(),
                    rightBrace(),
                    eof()
            );

            var block = extractOnlyBlockStatementFrom(parser.parse());
            var innerBlockStatement = extractOnlyStatementFrom(block.statements);

            assertVariableStatement(innerBlockStatement, identifier("a"), 1.0);
        }

        @Test
        void blockWithMultipleStatements() {
            var parser = createParser(
                    leftBrace(),
                    var(), identifier("a"), equal(), one(), semicolon(),
                    print(), one(), semicolon(),
                    rightBrace(),
                    eof()
            );

            var block = extractOnlyBlockStatementFrom(parser.parse());
            assertThat(block.statements).hasSize(2);
            assertVariableStatement(block.statements.get(0), identifier("a"), 1.0);
            assertPrintStatement(block.statements.get(1), 1.0);
        }
    }

    @Nested
    class ErrorCases {

        @Test
        void onlyEOFToken() {
            var parser = createParser(semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtSemicolon();
        }

        @Test
        void bangEqualWithoutLeftOperand() {
            var parser = createParser(bangEqual(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtLexeme("!=");
        }

        @Test
        void equalEqualWithoutLeftOperand() {
            var parser = createParser(equalEqual(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtLexeme("==");
        }

        @Test
        void leftParenWithoutRightParen() {
            var parser = createParser(leftParen(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtSemicolon();
        }

        @Test
        void leftParenAndLiteralButNoRightParen() {
            var parser = createParser(leftParen(), one(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertRightParenMissing();
        }

        @Test
        void leftAndRightParenWithoutSubExpression() {
            var parser = createParser(leftParen(), rightParen(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtLexeme(")");
        }

        @Test
        void rightParenWithoutLeftParen() {
            var parser = createParser(rightParen(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtLexeme(")");
        }

        @Test
        void rightParenAndLiteralButNoLeftParen() {
            var parser = createParser(rightParen(), one(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtLexeme(")");
        }

        @Test
        void bangTokenWithoutRightOperand() {
            var parser = createParser(bang(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtSemicolon();
        }

        @Test
        void minusTokenWithoutRightOperand() {
            var parser = createParser(minus(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtSemicolon();
        }

        @Test
        void slashTokenWithoutLeftOperand() {
            var parser = createParser(slash(), one(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtLexeme("/");
        }

        @Test
        void slashTokenWithoutRightOperand() {
            var parser = createParser(one(), slash(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtSemicolon();
        }

        @Test
        void starTokenWithoutLeftOperand() {
            var parser = createParser(star(), one(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtLexeme("*");
        }

        @Test
        void starTokenWithoutRightOperand() {
            var parser = createParser(one(), star(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtSemicolon();
        }

        @Test
        void plusTokenWithoutLeftOperand() {
            var parser = createParser(plus(), one(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtLexeme("+");
        }

        @Test
        void plusTokenWithoutRightOperand() {
            var parser = createParser(one(), plus(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtSemicolon();
        }

        @Test
        void greaterTokenWithoutLeftOperand() {
            var parser = createParser(greater(), one(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtLexeme(">");
        }

        @Test
        void greaterTokenWithoutRightOperand() {
            var parser = createParser(one(), greater(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtSemicolon();
        }

        @Test
        void greaterEqualTokenWithoutLeftOperand() {
            var parser = createParser(greaterEqual(), one(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtLexeme(">=");
        }

        @Test
        void greaterEqualTokenWithoutRightOperand() {
            var parser = createParser(one(), greaterEqual(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtSemicolon();
        }

        @Test
        void lessTokenWithoutLeftOperand() {
            var parser = createParser(less(), one(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtLexeme("<");
        }

        @Test
        void lessTokenWithoutRightOperand() {
            var parser = createParser(one(), less(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtSemicolon();
        }

        @Test
        void lessEqualTokenWithoutLeftOperand() {
            var parser = createParser(lessEqual(), one(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtLexeme("<=");
        }

        @Test
        void lessEqualTokenWithoutRightOperand() {
            var parser = createParser(one(), lessEqual(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtSemicolon();
        }

        @Test
        void bangEqualTokenWithoutLeftOperand() {
            var parser = createParser(bangEqual(), one(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtLexeme("!=");
        }

        @Test
        void bangEqualTokenWithoutRightOperand() {
            var parser = createParser(one(), bangEqual(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtSemicolon();
        }

        @Test
        void equalEqualTokenWithoutLeftOperand() {
            var parser = createParser(equalEqual(), one(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtLexeme("==");
        }

        @Test
        void equalEqualTokenWithoutRightOperand() {
            var parser = createParser(one(), equalEqual(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtSemicolon();
        }

        @Test
        void bangTokenWithoutRightOperandNextTokenIsEOF() {
            var parser = createParser(bang(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();

            assertErrorAtEOF();
        }

        @Test
        void invalidAssignmentTarget() {
            var parser = createParser(
                    var(), identifier("a"), semicolon(),
                    var(), identifier("b"), semicolon(),
                    identifier("a"), plus(), identifier("b"), equal(), identifier("c"), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(3);
            // Ignore the first 2 statements.

            Statement statement = statements.get(2);
            assertThat(statement).isInstanceOf(Statement.Expression.class);
            var statementExpression = (Statement.Expression) statement;

            assertThat(statementExpression.expression).isInstanceOf(Expression.Binary.class);
            var binaryExpression = (Expression.Binary) statementExpression.expression;

            assertVariableExpression(binaryExpression.left, identifier("a"));
            assertThat(binaryExpression.operator).isEqualToComparingFieldByField(plus());
            assertVariableExpression(binaryExpression.right, identifier("b"));

            assertErrorIsInvalidAssignmentTarget("=");
        }

        @Test
        void leftBraceButRightBraceMissing() {
            var parser = createParser(
                    leftBrace(),
                    var(), identifier("a"), equal(), one(), semicolon(),
                    // Error: Missing right brace!
                    eof()
            );

            List<Statement> statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();
            assertErrorUnclosedBlock();
        }

        @Test
        void rightBraceButLeftBraceMissing() {
            var parser = createParser(
                    print(), one(), semicolon(),
                    rightBrace(),
                    eof()
            );

            List<Statement> statements = parser.parse();

            assertThat(statements).hasSize(2);
            assertPrintStatement(statements.get(0), 1.0);
            assertThat(statements.get(1)).isNull();
            assertErrorAtLexeme("}");
        }

        @Nested
        class SynchronizeCases {

            @Test
            void afterErrorRecoversToNextSemiColon() {
                var parser = createParser(
                        bangEqual(), two(), semicolon(),    // Error: bangEqual without left operand
                        one(), bangEqual(), two(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(2);

                assertThat(statements.get(0)).isNull();
                assertErrorAtLexeme("!=");

                assertBinaryExpression(extractExpressionFrom(statements.get(1)), 1.0, bangEqual(), 2.0);
            }

            @Test
            void afterErrorRecoversToNextVariableDeclarationEvenWhenSemiColonIsMissing() {
                var parser = createParser(
                        bangEqual(), two(), // Error: bangEqual without left operand
                        var(), identifier("a"), equal(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(2);

                assertThat(statements.get(0)).isNull();
                assertErrorAtLexeme("!=");

                assertVariableStatement(statements.get(1), identifier("a"), 1.0);
            }

            @Test
            void afterErrorRecoversToNextPrintDeclarationEvenWhenSemiColonIsMissing() {
                var parser = createParser(
                        bangEqual(), two(), // Error: bangEqual without left operand
                        print(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(2);

                assertThat(statements.get(0)).isNull();
                assertErrorAtLexeme("!=");

                assertPrintStatement(statements.get(1), 1.0);
            }
        }

        private void assertErrorIsInvalidAssignmentTarget(String lexeme) {
            assertThat(fakeErrorReporter.receivedError()).isTrue();
            assertThat(fakeErrorReporter.getError().line).isEqualTo(1);
            assertThat(fakeErrorReporter.getError().location).isEqualTo(String.format(" at '%s'", lexeme));
            assertThat(fakeErrorReporter.getError().message).isEqualTo("Invalid assignment target.");
        }

        private void assertErrorUnclosedBlock() {
            assertThat(fakeErrorReporter.receivedError()).isTrue();
            assertThat(fakeErrorReporter.getError().line).isEqualTo(1);
            assertThat(fakeErrorReporter.getError().location).isEqualTo(" at end");
            assertThat(fakeErrorReporter.getError().message).isEqualTo("Expect '}' after block.");
        }
    }

    private Parser createParser(Token... tokens) {
        return createParser(List.of(tokens));
    }

    private Parser createParser(List<Token> tokens) {
        var parser = new RecursiveDescentParser(tokens);
        parser.addErrorReporter(fakeErrorReporter);

        return parser;
    }

    private Expression extractOnlyExpressionFrom(List<Statement> statements) {
        return extractExpressionFrom(extractOnlyStatementFrom(statements));
    }

    private Statement.Block extractOnlyBlockStatementFrom(List<Statement> statements) {
        var statement = extractOnlyStatementFrom(statements);

        assertThat(statement).isInstanceOf(Statement.Block.class);
        return (Statement.Block) statements.get(0);
    }

    private Statement extractOnlyStatementFrom(List<Statement> statements) {
        assertThat(statements).hasSize(1);

        return statements.get(0);
    }

    private Expression extractExpressionFrom(Statement statement) {
        assertThat(statement).isInstanceOf(Statement.Expression.class);

        return ((Statement.Expression) statement).expression;
    }

    private void assertLiteralExpression(Expression expression, Object expected) {
        assertThat(expression).isInstanceOf(Expression.Literal.class);

        assertThat(((Expression.Literal) expression).value).isEqualTo(expected);
    }

    private void assertErrorAtLexeme(String lexeme) {
        assertThat(fakeErrorReporter.receivedError()).isTrue();
        assertThat(fakeErrorReporter.getError().line).isEqualTo(1);
        assertThat(fakeErrorReporter.getError().location).isEqualTo(String.format(" at '%s'", lexeme));
        assertThat(fakeErrorReporter.getError().message).isEqualTo("Expect expression.");
    }

    private void assertErrorAtSemicolon() {
        assertThat(fakeErrorReporter.receivedError()).isTrue();
        assertThat(fakeErrorReporter.getError().line).isEqualTo(1);
        assertThat(fakeErrorReporter.getError().location).isEqualTo(" at ';'");
        assertThat(fakeErrorReporter.getError().message).isEqualTo("Expect expression.");
    }

    private void assertErrorAtEOF() {
        assertThat(fakeErrorReporter.receivedError()).isTrue();
        assertThat(fakeErrorReporter.getError().line).isEqualTo(1);
        assertThat(fakeErrorReporter.getError().location).isEqualTo(" at end");
        assertThat(fakeErrorReporter.getError().message).isEqualTo("Expect expression.");
    }

    private void assertRightParenMissing() {
        assertThat(fakeErrorReporter.receivedError()).isTrue();
        assertThat(fakeErrorReporter.getError().line).isEqualTo(1);
        assertThat(fakeErrorReporter.getError().location).isEqualTo(" at ';'");
        assertThat(fakeErrorReporter.getError().message).isEqualTo("Expect ')' after expression.");
    }

    private void assertBinaryExpression(Expression expression, Object left, Token operator, Object right) {
        assertThat(expression).isInstanceOf(Expression.Binary.class);
        var binaryExpression = (Expression.Binary) expression;

        assertLiteralExpression(binaryExpression.left, left);
        assertThat(binaryExpression.operator).isEqualToComparingFieldByField(operator);
        assertLiteralExpression(binaryExpression.right, right);
    }

    private void assertVariableExpression(Expression expression, Token expected) {
        assertThat(expression).isInstanceOf(Expression.Variable.class);

        assertThat(((Expression.Variable) expression).name).isEqualToComparingFieldByField(expected);
    }

    private void assertVariableStatement(Statement statement, Token name, Object expected) {
        assertThat(statement).isInstanceOf(Statement.Variable.class);
        var variableDeclaration = (Statement.Variable) statement;

        assertThat(variableDeclaration.name).isEqualToComparingFieldByField(name);
        assertLiteralExpression(variableDeclaration.initializer, expected);
    }

    private void assertPrintStatement(Statement statement, Object expected) {
        assertThat(statement).isInstanceOf(Statement.Print.class);

        assertLiteralExpression(((Statement.Print) statement).expression, expected);
    }
}