package be.guldentops.geert.lox.parser;

import be.guldentops.geert.lox.error.FakeErrorReporter;
import be.guldentops.geert.lox.grammar.Expression;
import be.guldentops.geert.lox.grammar.Statement;
import be.guldentops.geert.lox.lexer.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static be.guldentops.geert.lox.lexer.Token.Type.STRING;
import static be.guldentops.geert.lox.lexer.TokenObjectMother._class;
import static be.guldentops.geert.lox.lexer.TokenObjectMother._else;
import static be.guldentops.geert.lox.lexer.TokenObjectMother._false;
import static be.guldentops.geert.lox.lexer.TokenObjectMother._for;
import static be.guldentops.geert.lox.lexer.TokenObjectMother._if;
import static be.guldentops.geert.lox.lexer.TokenObjectMother._return;
import static be.guldentops.geert.lox.lexer.TokenObjectMother._super;
import static be.guldentops.geert.lox.lexer.TokenObjectMother._this;
import static be.guldentops.geert.lox.lexer.TokenObjectMother._true;
import static be.guldentops.geert.lox.lexer.TokenObjectMother._while;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.and;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.bang;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.bangEqual;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.comma;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.dot;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.eof;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.equal;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.equalEqual;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.fun;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.greater;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.greaterEqual;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.identifier;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.integer;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.leftBrace;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.leftParen;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.less;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.lessEqual;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.minus;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.nil;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.one;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.or;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.pi;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.plus;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.print;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.rightBrace;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.rightParen;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.semicolon;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.slash;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.star;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.two;
import static be.guldentops.geert.lox.lexer.TokenObjectMother.var;
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

            var groupingExpression = castTo(expression, Expression.Grouping.class);

            assertThat(groupingExpression.expression()).isNotNull();
            assertLiteralExpression(groupingExpression.expression(), 1.0);
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

            var unaryExpression = castTo(expression, Expression.Unary.class);

            assertThat(unaryExpression.operator()).isEqualToComparingFieldByField(bang());
            assertLiteralExpression(unaryExpression.right(), false);
        }

        @Test
        void minusTokenFollowedByLiteralToken() {
            var parser = createParser(minus(), one(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            var unaryExpression = castTo(expression, Expression.Unary.class);

            assertThat(unaryExpression.operator()).isEqualToComparingFieldByField(minus());
            assertLiteralExpression(unaryExpression.right(), 1.0);
        }

        @Test
        void multipleUnaryOperations() {
            var parser = createParser(bang(), bang(), bang(), _false(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            var farLeftUnaryExpression = castTo(expression, Expression.Unary.class);
            assertThat(farLeftUnaryExpression.operator()).isEqualToComparingFieldByField(bang());

            var leftUnaryExpression = castTo(farLeftUnaryExpression.right(), Expression.Unary.class);
            assertThat(leftUnaryExpression.operator()).isEqualToComparingFieldByField(bang());

            var unaryExpression = castTo(leftUnaryExpression.right(), Expression.Unary.class);
            assertThat(unaryExpression.operator()).isEqualToComparingFieldByField(bang());
            assertLiteralExpression(unaryExpression.right(), false);
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

            var statements = parser.parse();

            var firstBinaryExpression = castTo(extractOnlyExpressionFrom(statements), Expression.Binary.class);
            assertThat(firstBinaryExpression.operator()).isEqualToComparingFieldByField(plus());
            assertLiteralExpression(firstBinaryExpression.right(), 3.14);

            var secondBinaryExpression = castTo(firstBinaryExpression.left(), Expression.Binary.class);
            assertLiteralExpression(secondBinaryExpression.left(), 1.0);
            assertThat(secondBinaryExpression.operator()).isEqualToComparingFieldByField(star());
            assertLiteralExpression(secondBinaryExpression.right(), 2.0);
        }
    }

    @Nested
    class LogicalExpressions {

        @Test
        void orOperatorWithBothOperands() {
            var parser = createParser(_true(), or(), _false(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertLogicalExpression(expression, true, or(), false);
        }

        @Test
        void andOperatorWithBothOperands() {
            var parser = createParser(_true(), and(), _false(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertLogicalExpression(expression, true, and(), false);
        }
    }

    @Nested
    class ThisExpression {

        @Test
        void thisToken() {
            var parser = createParser(_this(), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            assertThat(castTo(expression, Expression.This.class).keyword()).isEqualToComparingFieldByField(_this());
        }
    }

    @Nested
    class SuperExpression {

        @Test
        void superToken() {
            var parser = createParser(_super(), dot(), identifier("method"), semicolon(), eof());

            var expression = extractOnlyExpressionFrom(parser.parse());

            var aSuper = castTo(expression, Expression.Super.class);
            assertThat(aSuper.keyword()).isEqualToComparingFieldByField(_super());
            assertThat(aSuper.method()).isEqualToComparingFieldByField(identifier("method"));
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

            var statements = parser.parse();

            assertVariableStatement(extractOnlyStatementFrom(statements), identifier("a"), 1.0);
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
            var assignExpression = castTo(extractExpressionFrom(statement), Expression.Assign.class);

            assertThat(assignExpression.name()).isEqualToComparingFieldByField(name);
            assertLiteralExpression(assignExpression.value(), expected);
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

            var statements = parser.parse();

            assertThat(statements).hasSize(1);
            var block = castTo(statements.get(0), Statement.Block.class);
            var innerBlockStatement = extractOnlyStatementFrom(block.statements());

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

            var statements = parser.parse();

            assertThat(statements).hasSize(1);
            var block = castTo(statements.get(0), Statement.Block.class);
            assertThat(block.statements()).hasSize(2);
            assertVariableStatement(block.statements().get(0), identifier("a"), 1.0);
            assertPrintStatement(block.statements().get(1), 1.0);
        }
    }

    @Nested
    class IfStatement {

        @Test
        void ifOnly() {
            var parser = createParser(
                    _if(), leftParen(), _true(), rightParen(),
                    print(), one(), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();
            assertIfStatementPrints(statements.get(0), true, 1.0);
        }

        @Test
        void ifElse() {
            var parser = createParser(
                    _if(), leftParen(), _true(), rightParen(),
                    print(), one(), semicolon(),
                    _else(),
                    print(), two(), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();
            assertIfElseStatementPrints(statements.get(0), true, 1.0, 2.0);
        }

        @Test
        void danglingElseProblem() {
            var parser = createParser(
                    _if(), leftParen(), _true(), rightParen(),
                    _if(), leftParen(), _true(), rightParen(),
                    print(), one(), semicolon(),
                    _else(), // Does this else belong to the first or second else?! --> it should belong to the nearest if.
                    print(), two(), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();
            var ifStatement = castTo(statements.get(0), Statement.If.class);

            assertLiteralExpression(ifStatement.condition(), true);
            assertIfElseStatementPrints(ifStatement.thenBranch(), true, 1.0, 2.0);
            assertThat(ifStatement.elseBranch()).isNull();
        }
    }

    @Nested
    class WhileStatement {

        @Test
        void whileLoop() {
            var parser = createParser(
                    _while(), leftParen(), _true(), rightParen(),
                    print(), one(), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();
            assertWhileStatementPrints(statements.get(0), true, 1.0);
        }
    }

    @Nested
    class ForStatement {

        @Test
        void fullForLoop() {
            // for (var i = 0; i < 10; i = i + 1) print 1;
            var parser = createParser(
                    _for(), leftParen(),
                    var(), identifier("i"), equal(), integer("0"), semicolon(),
                    identifier("i"), less(), integer("10"), semicolon(),
                    identifier("i"), equal(), identifier("i"), plus(), one(),
                    rightParen(),
                    print(), one(), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            //  {
            //      var i = 0;
            //      while (i < 10) {
            //          print 1;
            //          i = i + 1;
            //      }
            //  }
            assertForLoop(statements);
        }

        @Test
        void forLoopWithExpressionStatement() {
            // var i; for (i = 0; ; ) print 1;
            var parser = createParser(
                    var(), identifier("i"), semicolon(),
                    _for(), leftParen(), identifier("i"), equal(), integer("0"), semicolon(), semicolon(), rightParen(),
                    print(), one(), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            //  var i;
            //  {
            //      i = 0;
            //      while (true) {
            //          print 1;
            //      }
            //  }
            assertThat(statements).hasSize(2).doesNotContainNull();

            assertUninitializedVariable(statements.get(0), identifier("i"));

            var block = castTo(statements.get(1), Statement.Block.class);

            assertThat(block.statements()).hasSize(2).doesNotContainNull();

            var expression = extractExpressionFrom(block.statements().get(0));
            var assignment = castTo(expression, Expression.Assign.class);
            assertThat(assignment.name()).isEqualToComparingFieldByField(identifier("i"));
            assertLiteralExpression(assignment.value(), 0.0);

            assertWhileStatementPrints(block.statements().get(1), true, 1.0);
        }

        @Test
        void infiniteForLoop() {
            // for (; ;) print 1;
            var parser = createParser(
                    _for(), leftParen(), semicolon(), semicolon(), rightParen(),
                    print(), one(), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            //  while (true) {
            //      print 1;
            //  }
            assertThat(statements).hasSize(1).doesNotContainNull();
            assertWhileStatementPrints(statements.get(0), true, 1.0);
        }

        private void assertForLoop(List<Statement> statements) {
            assertThat(statements).hasSize(1).doesNotContainNull();

            var block = castTo(statements.get(0), Statement.Block.class);

            assertThat(block.statements()).hasSize(2).doesNotContainNull();

            assertVariableStatement(block.statements().get(0), identifier("i"), 0.0);

            var whileStatement = castTo(block.statements().get(1), Statement.While.class);
            assertWhileCondition(whileStatement.condition(), identifier("i"), less(), 10.0);
            assertWhileBody(whileStatement.body());
        }

        private void assertWhileCondition(Expression condition, Token left, Token operator, double right) {
            var binaryCondition = castTo(condition, Expression.Binary.class);

            assertVariableExpression(binaryCondition.left(), left);
            assertThat(binaryCondition.operator()).isEqualToComparingFieldByField(operator);
            assertLiteralExpression(binaryCondition.right(), right);
        }

        private void assertWhileBody(Statement body) {
            var block = castTo(body, Statement.Block.class);

            assertThat(block.statements()).hasSize(2);
            assertPrintStatement(block.statements().get(0), 1.0);
            assertAssignmentStatement(block.statements().get(1), "i");
        }

        private void assertAssignmentStatement(Statement statement, String variableName) {
            var assignment = castTo(extractExpressionFrom(statement), Expression.Assign.class);
            assertThat(assignment.name()).isEqualToComparingFieldByField(identifier(variableName));

            var binaryExpression = castTo(assignment.value(), Expression.Binary.class);
            assertVariableExpression(binaryExpression.left(), identifier(variableName));
            assertThat(binaryExpression.operator()).isEqualToComparingFieldByField(plus());
            assertLiteralExpression(binaryExpression.right(), 1.0);
        }
    }

    @Nested
    class CallFunction {

        @Test
        void callNoArgumentsFunction() {
            var parser = createParser(
                    identifier("get"), leftParen(), rightParen(), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();

            var statementExpression = castTo(statements.get(0), Statement.Expression.class);
            var call = castTo(statementExpression.expression(), Expression.Call.class);

            assertVariableExpression(call.callee(), identifier("get"));
            assertThat(call.paren()).isEqualToComparingFieldByField(rightParen());
            assertThat(call.arguments()).isEmpty();
        }

        @Test
        void callSingleArgumentFunction() {
            var parser = createParser(
                    identifier("set"), leftParen(), one(), rightParen(), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();

            var statementExpression = castTo(statements.get(0), Statement.Expression.class);
            var call = castTo(statementExpression.expression(), Expression.Call.class);

            assertVariableExpression(call.callee(), identifier("set"));
            assertThat(call.paren()).isEqualToComparingFieldByField(rightParen());
            assertThat(call.arguments()).hasSize(1);
            assertLiteralExpression(call.arguments().get(0), 1.0);
        }

        @Test
        void callMultiArgumentFunction() {
            var parser = createParser(
                    identifier("sum"), leftParen(), one(), comma(), two(), rightParen(), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();

            var statementExpression = castTo(statements.get(0), Statement.Expression.class);
            var call = castTo(statementExpression.expression(), Expression.Call.class);

            assertVariableExpression(call.callee(), identifier("sum"));
            assertThat(call.paren()).isEqualToComparingFieldByField(rightParen());
            assertThat(call.arguments()).hasSize(2);
            assertLiteralExpression(call.arguments().get(0), 1.0);
            assertLiteralExpression(call.arguments().get(1), 2.0);
        }
    }

    @Nested
    class CallProperty {

        @Test
        void callGetProperty() {
            var parser = createParser(
                    identifier("point"), dot(), identifier("x"), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();

            var statementExpression = castTo(statements.get(0), Statement.Expression.class);
            assertGetExpression(statementExpression.expression(), identifier("point"), identifier("x"));
        }

        @Test
        void callGetNestedProperty() {
            var parser = createParser(
                    identifier("square"), dot(), identifier("point"), dot(), identifier("x"), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();

            var statementExpression = castTo(statements.get(0), Statement.Expression.class);

            var nestedProperty = castTo(statementExpression.expression(), Expression.Get.class);
            assertGetExpression(nestedProperty.object(), identifier("square"), identifier("point"));
            assertThat(nestedProperty.name()).isEqualToComparingFieldByField(identifier("x"));
        }

        @Test
        void callSetProperty() {
            var parser = createParser(
                    identifier("point"), dot(), identifier("x"), equal(), one(), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();

            var statementExpression = castTo(statements.get(0), Statement.Expression.class);
            assertSetExpressionToLiteral(statementExpression.expression(), identifier("point"), identifier("x"), 1.0);
        }

        @Test
        void callSetNestedProperty() {
            var parser = createParser(
                    identifier("square"), dot(), identifier("point"), dot(), identifier("x"), equal(), one(), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();

            var statementExpression = castTo(statements.get(0), Statement.Expression.class);

            var nestedProperty = castTo(statementExpression.expression(), Expression.Set.class);
            assertGetExpression(nestedProperty.object(), identifier("square"), identifier("point"));
            assertThat(nestedProperty.name()).isEqualToComparingFieldByField(identifier("x"));
            assertLiteralExpression(nestedProperty.value(), 1.0);
        }
    }

    @Nested
    class FunctionDeclaration {

        @Test
        void noParametersFunction() {
            var parser = createParser(
                    fun(), identifier("print1"), leftParen(), rightParen(), leftBrace(),
                    print(), one(), semicolon(),
                    rightBrace(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();

            assertFunctionDeclarationWithBodyPrints(statements.get(0), "print1", 1.0);
        }

        @Test
        void singleParameterFunction() {
            var parser = createParser(
                    fun(), identifier("set"), leftParen(), identifier("a"), rightParen(), leftBrace(),
                    print(), one(), semicolon(),
                    rightBrace(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();

            assertFunctionDeclarationWithBodyPrints(statements.get(0), "set", List.of("a"), 1.0);
        }

        @Test
        void multipleParametersFunction() {
            var parser = createParser(
                    fun(), identifier("set"), leftParen(), identifier("a"), comma(), identifier("b"), rightParen(), leftBrace(),
                    print(), one(), semicolon(),
                    rightBrace(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();

            var function = castTo(statements.get(0), Statement.Function.class);
            assertFunctionDeclarationWithBodyPrints(statements.get(0), "set", List.of("a", "b"), 1.0);
        }
    }

    @Nested
    class ReturnStatement {

        @Test
        void emptyReturn() {
            var parser = createParser(_return(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();

            var returnStatement = castTo(statements.get(0), Statement.Return.class);
            assertThat(returnStatement.keyword()).isEqualToComparingFieldByField(_return());
            assertThat(returnStatement.value()).isNull();
        }

        @Test
        void returnValue() {
            var parser = createParser(_return(), one(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();

            var returnStatement = castTo(statements.get(0), Statement.Return.class);
            assertThat(returnStatement.keyword()).isEqualToComparingFieldByField(_return());
            assertLiteralExpression(returnStatement.value(), 1.0);
        }
    }

    @Nested
    class ClassStatement {

        @Test
        void emptyClass() {
            var parser = createParser(_class(), identifier("EmptyClass"), leftBrace(), rightBrace(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();

            var classStatement = castTo(statements.get(0), Statement.Class.class);
            assertThat(classStatement.name()).isEqualToComparingFieldByField(identifier("EmptyClass"));
            assertThat(classStatement.superclass()).isNull();
            assertThat(classStatement.methods()).isEmpty();
        }

        @Test
        void classWithMultipleMethods() {
            var parser = createParser(_class(), identifier("Printer"), leftBrace(),
                    identifier("print1"), leftParen(), rightParen(), leftBrace(), print(), one(), semicolon(), rightBrace(),
                    identifier("print2"), leftParen(), rightParen(), leftBrace(), print(), two(), semicolon(), rightBrace(),
                    rightBrace(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();

            var classStatement = castTo(statements.get(0), Statement.Class.class);
            assertThat(classStatement.name()).isEqualToComparingFieldByField(identifier("Printer"));
            assertThat(classStatement.superclass()).isNull();
            assertThat(classStatement.methods()).hasSize(2);

            assertFunctionDeclarationWithBodyPrints(classStatement.methods().get(0), "print1", 1.0);
            assertFunctionDeclarationWithBodyPrints(classStatement.methods().get(1), "print2", 2.0);
        }

        @Test
        void classWithSuperClassMultipleMethods() {
            var parser = createParser(_class(), identifier("Circle"), less(), identifier("Shape"), leftBrace(),
                    rightBrace(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).doesNotContainNull();

            var classStatement = castTo(statements.get(0), Statement.Class.class);
            assertThat(classStatement.name()).isEqualToComparingFieldByField(identifier("Circle"));
            assertVariableExpression(classStatement.superclass(), identifier("Shape"));
            assertThat(classStatement.methods()).isEmpty();
        }
    }

    @Nested
    class ErrorCases {

        @Test
        void onlyEOFToken() {
            var parser = createParser(semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();
            assertErrorAtLexeme(";");
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
            assertErrorAtLexeme(";");
        }

        @Test
        void leftParenAndLiteralButNoRightParen() {
            var parser = createParser(leftParen(), one(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();
            assertError("[line 1] SyntaxError: at ';' expect ')' after expression.");
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
            assertErrorAtLexeme(";");
        }

        @Test
        void minusTokenWithoutRightOperand() {
            var parser = createParser(minus(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();
            assertErrorAtLexeme(";");
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
            assertErrorAtLexeme(";");
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
            assertErrorAtLexeme(";");
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
            assertErrorAtLexeme(";");
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
            assertErrorAtLexeme(";");
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
            assertErrorAtLexeme(";");
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
            assertErrorAtLexeme(";");
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
            assertErrorAtLexeme(";");
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
            assertErrorAtLexeme(";");
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
            assertErrorAtLexeme(";");
        }

        @Test
        void orTokenWithoutLeftOperand() {
            var parser = createParser(or(), one(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();
            assertErrorAtLexeme("or");
        }

        @Test
        void orTokenWithoutRightOperand() {
            var parser = createParser(one(), or(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();
            assertErrorAtLexeme(";");
        }

        @Test
        void andTokenWithoutLeftOperand() {
            var parser = createParser(and(), one(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();
            assertErrorAtLexeme("and");
        }

        @Test
        void andTokenWithoutRightOperand() {
            var parser = createParser(one(), and(), semicolon(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();
            assertErrorAtLexeme(";");
        }

        @Test
        void bangTokenWithoutRightOperandNextTokenIsEOF() {
            var parser = createParser(bang(), eof());

            var statements = parser.parse();

            assertThat(statements).hasSize(1).containsOnlyNulls();
            assertErrorAtLexeme("end");
        }

        @Test
        void assignmentTokenWithInvalidTarget() {
            var parser = createParser(
                    var(), identifier("a"), semicolon(),
                    var(), identifier("b"), semicolon(),
                    identifier("a"), plus(), identifier("b"), equal(), identifier("c"), semicolon(),
                    eof()
            );

            var statements = parser.parse();

            assertThat(statements).hasSize(3);
            // Ignore the first 2 statements.

            var assignmentStatement = statements.get(2);
            var statementExpression = castTo(assignmentStatement, Statement.Expression.class);
            var binaryExpression = castTo(statementExpression.expression(), Expression.Binary.class);

            assertVariableExpression(binaryExpression.left(), identifier("a"));
            assertThat(binaryExpression.operator()).isEqualToComparingFieldByField(plus());
            assertVariableExpression(binaryExpression.right(), identifier("b"));

            assertError("[line 1] SyntaxError: at '=' invalid assignment target.");
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
            assertError("[line 1] SyntaxError: at 'end' expect '}' after block.");
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
        class If {

            @Test
            void ifWithoutRightParen() {
                var parser = createParser(
                        _if(), leftParen(), _true(),
                        print(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at 'print' expect ')' after if condition.");
            }

            @Test
            void ifWithoutCondition() {
                var parser = createParser(
                        _if(), leftParen(), rightParen(),
                        print(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(2);
                assertThat(statements.get(0)).isNull();
                assertErrorAtLexeme(")");

                assertPrintStatement(statements.get(1), 1.0);
            }

            @Test
            void ifWithoutLeftParen() {
                var parser = createParser(
                        _if(), _true(), rightParen(),
                        print(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(2);
                assertThat(statements.get(0)).isNull();
                assertError("[line 1] SyntaxError: at 'true' expect '(' after 'if'.");

                assertPrintStatement(statements.get(1), 1.0);
            }

            @Test
            void ifElseWithoutElseBranch() {
                var parser = createParser(
                        _if(), leftParen(), _true(), rightParen(),
                        print(), one(), semicolon(),
                        _else(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertErrorAtLexeme("end");
            }
        }

        @Nested
        class While {

            @Test
            void whileWithoutRightParen() {
                var parser = createParser(
                        _while(), leftParen(), _true(),
                        print(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at 'print' expect ')' after while condition.");
            }

            @Test
            void whileWithoutCondition() {
                var parser = createParser(
                        _while(), leftParen(), rightParen(),
                        print(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(2);
                assertThat(statements.get(0)).isNull();
                assertErrorAtLexeme(")");

                assertPrintStatement(statements.get(1), 1.0);
            }

            @Test
            void whileWithoutLeftParen() {
                var parser = createParser(
                        _while(), _true(), rightParen(),
                        print(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(2);
                assertThat(statements.get(0)).isNull();
                assertError("[line 1] SyntaxError: at 'true' expect '(' after 'while'.");

                assertPrintStatement(statements.get(1), 1.0);
            }
        }

        @Nested
        class For {

            @Test
            void forWithoutRightParen() {
                var parser = createParser(
                        _for(), leftParen(), semicolon(), semicolon(),
                        print(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at 'print' expect expression.");
            }

            @Test
            void forWithoutLeftParen() {
                var parser = createParser(
                        _for(), semicolon(), semicolon(), rightParen(),
                        print(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(4);
                assertThat(statements.get(0)).isNull();
                assertThat(statements.get(1)).isNull();
                assertThat(statements.get(2)).isNull();
                assertThat(statements.get(3)).isInstanceOf(Statement.Print.class); // Recover to the print statement, this test does not care!
                assertError("[line 1] SyntaxError: at ')' expect expression.");
            }

            @Test
            void forWithOneMissingSemiColon() {
                var parser = createParser(
                        _for(), leftParen(), var(), identifier("i"), equal(), one(), semicolon(), rightParen(),
                        print(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(2);
                assertThat(statements.get(0)).isNull();
                assertThat(statements.get(1)).isInstanceOf(Statement.Print.class); // Recover to the print statement, this test does not care!
                assertError("[line 1] SyntaxError: at ')' expect expression.");
            }

            @Test
            void forWithTwoMissingSemiColons() {
                var parser = createParser(
                        _for(), leftParen(), var(), identifier("i"), equal(), one(), rightParen(),
                        print(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(2);
                assertThat(statements.get(0)).isNull();
                assertThat(statements.get(1)).isInstanceOf(Statement.Print.class); // Recover to the print statement, this test does not care!
                assertError("[line 1] SyntaxError: at ')' expect ';' after variable declaration.");
            }
        }

        @Nested
        class CallFunction {

            @Test
            void callFunctionWithoutLeftParen() {
                var parser = createParser(
                        identifier("get"), rightParen(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at ')' expect ';' after value.");
            }

            @Test
            void callFunctionWithoutRightParen() {
                var parser = createParser(
                        identifier("get"), leftParen(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at ';' expect expression.");
            }

            @Test
            void callFunctionWithMissingCommaInArgumentList() {
                var parser = createParser(
                        identifier("sum"), leftParen(), one(), two(), rightParen(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at '2' expect ')' after arguments.");
            }

            @Test
            void callFunctionWithTooManyArguments() {
                var parser = createParser(
                        identifier("sum"), leftParen(),
                        one(), comma(),
                        one(), comma(),
                        one(), comma(),
                        one(), comma(),
                        one(), comma(),
                        one(), comma(),
                        one(), comma(),
                        one(), comma(),
                        one(),
                        rightParen(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(1).doesNotContainNull(); // Still parses the call!
                assertThat(statements.get(0)).isInstanceOf(Statement.Expression.class);
                assertError("[line 1] SyntaxError: at '1' cannot have more than 8 arguments.");
            }
        }

        @Nested
        class CallGetProperty {

            @Test
            void callGetPropertyWithoutObject() {
                var parser = createParser(
                        dot(), identifier("x"), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at '.' expect expression.");
            }

            @Test
            void callGetPropertyWithoutName() {
                var parser = createParser(
                        identifier("point"), dot(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at ';' expect property name after '.'.");
            }

            @Test
            void callSetPropertyWithoutObject() {
                var parser = createParser(
                        dot(), identifier("x"), equal(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at '.' expect expression.");
            }

            @Test
            void callSetPropertyWithoutName() {
                var parser = createParser(
                        identifier("point"), dot(), equal(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at '=' expect property name after '.'.");
            }

            @Test
            void callSetPropertyWithoutEqual() {
                var parser = createParser(
                        identifier("point"), dot(), identifier("x"), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at '1' expect ';' after value.");
            }

            @Test
            void callSetPropertyWithoutValue() {
                var parser = createParser(
                        identifier("point"), dot(), identifier("x"), equal(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at ';' expect expression.");
            }
        }

        @Nested
        class Function {

            @Test
            void functionWithoutIdentifier() {
                var parser = createParser(
                        fun(), leftParen(), rightParen(), leftBrace(),
                        print(), one(), semicolon(),
                        rightBrace(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(3);
                assertThat(statements.get(0)).isNull();
                assertThat(statements.get(1)).isInstanceOf(Statement.Print.class); // Still parses some of the code.
                assertThat(statements.get(2)).isNull();
                assertError("[line 1] SyntaxError: at '}' expect expression.");
            }

            @Test
            void functionWithoutLeftParen() {
                var parser = createParser(
                        fun(), identifier("get"), rightParen(), leftBrace(),
                        print(), one(), semicolon(),
                        rightBrace(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(3);
                assertThat(statements.get(0)).isNull();
                assertThat(statements.get(1)).isInstanceOf(Statement.Print.class); // Still parses some of the code.
                assertThat(statements.get(2)).isNull();
                assertError("[line 1] SyntaxError: at '}' expect expression.");
            }

            @Test
            void functionWithoutRightParen() {
                var parser = createParser(
                        fun(), identifier("get"), leftParen(), leftBrace(),
                        print(), one(), semicolon(),
                        rightBrace(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(3);
                assertThat(statements.get(0)).isNull();
                assertThat(statements.get(1)).isInstanceOf(Statement.Print.class); // Still parses some of the code.
                assertThat(statements.get(2)).isNull();
                assertError("[line 1] SyntaxError: at '}' expect expression.");
            }

            @Test
            void functionWithMissingCommaInParameterList() {
                var parser = createParser(
                        fun(), identifier("get"), leftParen(), identifier("a"), identifier("b"), rightParen(), leftBrace(),
                        print(), one(), semicolon(),
                        rightBrace(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(3);
                assertThat(statements.get(0)).isNull();
                assertThat(statements.get(1)).isInstanceOf(Statement.Print.class); // Still parses some of the code.
                assertThat(statements.get(2)).isNull();
                assertError("[line 1] SyntaxError: at '}' expect expression.");
            }

            @Test
            void functionWithMoreThan8Parameters() {
                var parser = createParser(
                        fun(), identifier("get"), leftParen(),
                        identifier("a1"), comma(),
                        identifier("a2"), comma(),
                        identifier("a3"), comma(),
                        identifier("a4"), comma(),
                        identifier("a5"), comma(),
                        identifier("a6"), comma(),
                        identifier("a7"), comma(),
                        identifier("a8"), comma(),
                        identifier("a9"), rightParen(),
                        leftBrace(),
                        print(), one(), semicolon(),
                        rightBrace(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(1).doesNotContainNull(); // Still parses the call!
                assertThat(statements.get(0)).isInstanceOf(Statement.Function.class);
                assertError("[line 1] SyntaxError: at 'a9' cannot have more than 8 parameters.");
            }

            @Test
            void functionWithoutLeftBrace() {
                var parser = createParser(
                        fun(), identifier("get"), leftParen(), rightParen(),
                        print(), one(), semicolon(),
                        rightBrace(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(2).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at '}' expect expression.");
            }

            @Test
            void functionWithoutRightBrace() {
                var parser = createParser(
                        fun(), identifier("get"), leftParen(), rightParen(), leftBrace(),
                        print(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at 'end' expect '}' after block.");
            }
        }

        @Nested
        class Return {

            @Test
            void emptyReturnWithoutSemicolon() {
                var parser = createParser(_return(), eof());

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at 'end' expect expression.");
            }

            @Test
            void returnValueWithoutSemicolon() {
                var parser = createParser(_return(), one(), eof());

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at 'end' expect ';' after return value.");
            }
        }

        @Nested
        class Class {

            @Test
            void classWithMissingLeftBrace() {
                var parser = createParser(_class(), identifier("Foo"), rightBrace(), eof());

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at '}' expect '{' before class body.");
            }

            @Test
            void classWithMissingRightBrace() {
                var parser = createParser(_class(), identifier("Foo"), leftBrace(), eof());

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at 'end' expect '}' after class body.");
            }

            @Test
            void classWithLessTokenButMissingSuperClassIdentifier() {
                var parser = createParser(_class(), identifier("Foo"), less(), leftBrace(), rightBrace(), eof());

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at '{' expected super class name.");
            }
        }

        @Nested
        class Super {

            @Test
            void superWithMissingDot() {
                var parser = createParser(_super(), identifier("method"), semicolon(), eof());

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at 'method' expect '.' after 'super'.");
            }

            @Test
            void superWithMissingMethod() {
                var parser = createParser(_super(), dot(), semicolon(), eof());

                var statements = parser.parse();

                assertThat(statements).hasSize(1).containsOnlyNulls();
                assertError("[line 1] SyntaxError: at ';' expect superclass method name.");
            }
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

            @Test
            void afterErrorRecoversToNextIfDeclarationEvenWhenSemiColonIsMissing() {
                var parser = createParser(
                        bangEqual(), two(), // Error: bangEqual without left operand
                        _if(), leftParen(), _true(), rightParen(),
                        print(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(2);

                assertThat(statements.get(0)).isNull();
                assertErrorAtLexeme("!=");

                assertIfStatementPrints(statements.get(1), true, 1.0);
            }

            @Test
            void afterErrorRecoversToNextWhileDeclarationEvenWhenSemiColonIsMissing() {
                var parser = createParser(
                        bangEqual(), two(), // Error: bangEqual without left operand
                        _while(), leftParen(), _true(), rightParen(),
                        print(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(2);

                assertThat(statements.get(0)).isNull();
                assertErrorAtLexeme("!=");

                assertWhileStatementPrints(statements.get(1), true, 1.0);
            }

            @Test
            void afterErrorRecoversToNextForDeclarationEvenWhenSemiColonIsMissing() {
                var parser = createParser(
                        bangEqual(), two(), // Error: bangEqual without left operand
                        _for(), leftParen(), semicolon(), semicolon(), rightParen(),
                        print(), one(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(2);

                assertThat(statements.get(0)).isNull();
                assertErrorAtLexeme("!=");

                // "for (;;)" is the same as "while(true)"
                assertWhileStatementPrints(statements.get(1), true, 1.0);
            }

            @Test
            void afterErrorRecoversToNextFunctionDeclarationEvenWhenSemiColonIsMissing() {
                var parser = createParser(
                        bangEqual(), two(), // Error: bangEqual without left operand
                        fun(), identifier("print1"), leftParen(), rightParen(), leftBrace(),
                        print(), one(), semicolon(),
                        rightBrace(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(2);

                assertThat(statements.get(0)).isNull();
                assertErrorAtLexeme("!=");

                assertFunctionDeclarationWithBodyPrints(statements.get(1), "print1", 1.0);
            }

            @Test
            void afterErrorRecoversToNextReturnEvenWhenSemiColonIsMissing() {
                var parser = createParser(
                        bangEqual(), two(), // Error: bangEqual without left operand
                        _return(), semicolon(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(2);

                assertThat(statements.get(0)).isNull();
                assertErrorAtLexeme("!=");

                var returnStatement = castTo(statements.get(1), Statement.Return.class);
                assertThat(returnStatement.keyword()).isEqualToComparingFieldByField(_return());
                assertThat(returnStatement.value()).isNull();
            }

            @Test
            void afterErrorRecoversToNextClassEvenWhenSemiColonIsMissing() {
                var parser = createParser(
                        bangEqual(), two(), // Error: bangEqual without left operand
                        _class(), identifier("EmptyClass"), leftBrace(), rightBrace(),
                        eof()
                );

                var statements = parser.parse();

                assertThat(statements).hasSize(2);

                assertThat(statements.get(0)).isNull();
                assertErrorAtLexeme("!=");

                var classStatement = castTo(statements.get(1), Statement.Class.class);
                assertThat(classStatement.name()).isEqualToComparingFieldByField(identifier("EmptyClass"));
                assertThat(classStatement.superclass()).isNull();
                assertThat(classStatement.methods()).isEmpty();
            }
        }
    }

    private <T> T castTo(Object o, Class<T> clazz) {
        assertThat(o).isInstanceOf(clazz);

        return clazz.cast(o);
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

    private Expression extractExpressionFrom(Statement statement) {
        return castTo(statement, Statement.Expression.class).expression();
    }

    private Statement extractOnlyStatementFrom(List<Statement> statements) {
        assertThat(statements).hasSize(1);

        return statements.get(0);
    }

    private void assertLiteralExpression(Expression expression, Object expected) {
        assertThat(castTo(expression, Expression.Literal.class).value()).isEqualTo(expected);
    }

    private void assertErrorAtLexeme(String lexeme) {
        assertError("[line 1] SyntaxError: at '" + lexeme + "' expect expression.");
    }

    private void assertError(String message) {
        assertThat(fakeErrorReporter.receivedError()).isTrue();
        assertThat(fakeErrorReporter.getError()).hasToString(message);
    }

    private void assertBinaryExpression(Expression expression, Object left, Token operator, Object right) {
        var binaryExpression = castTo(expression, Expression.Binary.class);

        assertLiteralExpression(binaryExpression.left(), left);
        assertThat(binaryExpression.operator()).isEqualToComparingFieldByField(operator);
        assertLiteralExpression(binaryExpression.right(), right);
    }

    private void assertLogicalExpression(Expression expression, Object left, Token operator, Object right) {
        var logicalExpression = castTo(expression, Expression.Logical.class);

        assertLiteralExpression(logicalExpression.left(), left);
        assertThat(logicalExpression.operator()).isEqualToComparingFieldByField(operator);
        assertLiteralExpression(logicalExpression.right(), right);
    }

    private void assertVariableExpression(Expression expression, Token expected) {
        assertThat(castTo(expression, Expression.Variable.class).name()).isEqualToComparingFieldByField(expected);
    }

    private void assertUninitializedVariable(Statement statement, Token name) {
        var variableDeclaration = castTo(statement, Statement.Variable.class);

        assertThat(variableDeclaration.name()).isEqualToComparingFieldByField(name);
        assertThat(variableDeclaration.initializer()).isNull();
    }

    private void assertVariableStatement(Statement statement, Token name, Object expected) {
        var variableDeclaration = castTo(statement, Statement.Variable.class);

        assertThat(variableDeclaration.name()).isEqualToComparingFieldByField(name);
        assertLiteralExpression(variableDeclaration.initializer(), expected);
    }

    private void assertPrintStatement(Statement statement, Object expected) {
        assertLiteralExpression(castTo(statement, Statement.Print.class).expression(), expected);
    }

    private void assertIfStatementPrints(Statement statement, boolean condition, Object thenPrints) {
        var ifStatement = castTo(statement, Statement.If.class);

        assertLiteralExpression(ifStatement.condition(), condition);
        assertPrintStatement(ifStatement.thenBranch(), thenPrints);
        assertThat(ifStatement.elseBranch()).isNull();
    }

    private void assertIfElseStatementPrints(Statement statement, boolean condition, Object thenPrints, Object elsePrints) {
        var ifStatement = castTo(statement, Statement.If.class);

        assertLiteralExpression(ifStatement.condition(), condition);
        assertPrintStatement(ifStatement.thenBranch(), thenPrints);
        assertPrintStatement(ifStatement.elseBranch(), elsePrints);
    }

    private void assertWhileStatementPrints(Statement statement, boolean condition, Object prints) {
        var whileStatement = castTo(statement, Statement.While.class);

        assertLiteralExpression(whileStatement.condition(), condition);
        assertPrintStatement(whileStatement.body(), prints);
    }

    private void assertFunctionDeclarationWithBodyPrints(Statement statement, String name, Object prints) {
        assertFunctionDeclarationWithBodyPrints(statement, name, emptyList(), prints);
    }

    private void assertFunctionDeclarationWithBodyPrints(Statement statement, String name, List<String> parameterNames, Object prints) {
        var functionStatement = castTo(statement, Statement.Function.class);

        assertThat(functionStatement.name()).isEqualToComparingFieldByField(identifier(name));

        assertThat(functionStatement.parameters()).hasSameSizeAs(parameterNames);
        for (var i = 0; i < functionStatement.parameters().size(); i++) {
            assertThat(functionStatement.parameters().get(i)).isEqualToComparingFieldByField(identifier(parameterNames.get(i)));
        }

        assertThat(functionStatement.body()).hasSize(1);
        assertPrintStatement(functionStatement.body().get(0), prints);
    }

    private void assertGetExpression(Expression expression, Token object, Token name) {
        var getExpression = castTo(expression, Expression.Get.class);

        assertVariableExpression(getExpression.object(), object);
        assertThat(getExpression.name()).isEqualToComparingFieldByField(name);
    }

    private void assertSetExpressionToLiteral(Expression expression, Token object, Token name, Object expectedValue) {
        var setExpression = castTo(expression, Expression.Set.class);

        assertVariableExpression(setExpression.object(), object);
        assertThat(setExpression.name()).isEqualToComparingFieldByField(name);
        assertLiteralExpression(setExpression.value(), expectedValue);
    }
}
