package be.guldentops.geert.lox;

import be.guldentops.geert.lox.error.impl.ConsoleErrorReporter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoxAcceptanceTest {

    private PrintStream originalOut = System.out;
    private ByteArrayOutputStream outContent = new ByteArrayOutputStream();

    /**
     * Acceptance tests hook-in on Lox.
     * <p>
     * LoxMain uses System.exit() which quits the currently running JVM!
     * <p>
     * If we would use LoxMain we would:
     * * Not get any more info (messages) from the test runner.
     * * After a failure all the following tests would be aborted.
     */
    private Lox lox;

    @BeforeEach
    void setUp() {
        System.setOut(new PrintStream(outContent));

        lox = new Lox(new ConsoleErrorReporter());
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Nested
    class RunLoxProgramFromFile {

        @Test
        void canRunHelloWorldScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/HelloWorld.lox"));

            assertThat(outContent.toString()).isEqualTo("Hello world!\n");
        }

        @Test
        void canRunSimpleArithmeticScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/statements/SimplePrintStatements.lox"));

            assertThat(outContent.toString()).isEqualTo("one\ntrue\n3\n");
        }

        @Test
        void canRunGlobalVariablesScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/variables/GlobalVariable.lox"));

            assertThat(outContent.toString()).isEqualTo("espresso\n");
        }

        @Test
        void canRunRedefineVariablesScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/variables/RedefineGlobalVariable.lox"));

            assertThat(outContent.toString()).isEqualTo("before\nafter\n");
        }

        @Test
        void canRunUninitializedVariableScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/variables/UninitializedVariable.lox"));

            assertThat(outContent.toString()).isEqualTo("nil\n");
        }

        @Test
        void canRunMultipleVariablesScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/variables/MultipleVariables.lox"));

            assertThat(outContent.toString()).isEqualTo("3\n");
        }

        @Test
        void canRunMultipleNestedBlocksScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/blocks/MultipleNestedBlocks.lox"));

            assertThat(outContent.toString()).isEqualTo(
                    "inner a\n" +
                            "outer b\n" +
                            "global c\n" +
                            "outer a\n" +
                            "outer b\n" +
                            "global c\n" +
                            "global a\n" +
                            "global b\n" +
                            "global c\n"
            );
        }

        @Test
        void canRunIfOperatorScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/logical/operators/IfOperator.lox"));

            assertThat(outContent.toString()).isEqualTo("if true\nif else true\n");
        }

        @Test
        void canRunLogicalOperatorsScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/logical/operators/LogicalOperators.lox"));

            assertThat(outContent.toString()).isEqualTo("hi\nyes\n");
        }

        @Test
        void canRunWhileLoopScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/loops/WhileLoop.lox"));

            assertThat(outContent.toString()).isEqualTo("3\n2\n1\n0\n");
        }

        @Test
        void canRunForLoopScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/loops/ForLoop.lox"));

            assertThat(outContent.toString()).isEqualTo("0\n1\n2\n3\n4\n5\n6\n7\n8\n9\n");
        }

        @Test
        void canRunCallFunctionWithReturnScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/functions/FunctionWithReturn.lox"));

            assertThat(outContent.toString()).isEqualTo("3\n");
        }

        @Test
        void canRunRecursiveFunctionScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/functions/RecursiveFunction.lox"));

            assertThat(outContent.toString()).isEqualTo("1\n2\n3\n");
        }

        @Test
        void canRunPrintFunctionScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/functions/PrintFunction.lox"));

            assertThat(outContent.toString()).isEqualTo("<fn add>\n");
        }

        @Test
        void canRunPrintResultOfFunctionWithoutReturnScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/functions/PrintResultOfFunctionWithoutReturn.lox"));

            assertThat(outContent.toString()).isEqualTo("don't return anything\nnil\n");
        }

        @Test
        void canRunReturnFromNestedBlocksScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/functions/ReturnFromNestedBlocks.lox"));

            assertThat(outContent.toString()).isEqualTo("3\n");
        }

        @Test
        void canRunFibonacciScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/functions/Fibonacci.lox"));

            assertThat(outContent.toString()).isEqualTo("0\n" +
                    "1\n" +
                    "1\n" +
                    "2\n" +
                    "3\n" +
                    "5\n" +
                    "8\n" +
                    "13\n" +
                    "21\n" +
                    "34\n" +
                    "55\n" +
                    "89\n" +
                    "144\n" +
                    "233\n" +
                    "377\n" +
                    "610\n" +
                    "987\n" +
                    "1597\n" +
                    "2584\n" +
                    "4181\n");
        }

        @Test
        void canRunNestedFunctionsScript() throws Exception {
            lox.runFile(getAbsoluteFilePathOf("lox/src/functions/NestedFunctions.lox"));

            assertThat(outContent.toString()).isEqualTo("1\n2\n");
        }
    }

    @Nested
    class ErrorCase {

        @Test
        void nonExistingFile() {
            assertThatThrownBy(() -> lox.runFile("non-existing file location")).isInstanceOf(NoSuchFileException.class);
        }
    }

    private String getAbsoluteFilePathOf(String name) throws URISyntaxException {
        return Paths.get(ClassLoader.getSystemResource(name).toURI()).toAbsolutePath().toString();
    }
}