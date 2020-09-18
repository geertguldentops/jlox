package be.guldentops.geert.lox.tools;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * No explicit test for this class exists since the code it generates is extensively tested in the parser, resolver and interpreter!
 */
class GenerateAbstractSyntaxTree {

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: generate_ast <output directory>");
            System.exit(1);
        }

        var outputDir = args[0];

        defineAbstractSyntaxTree(outputDir, "Expression", Arrays.asList(
                "Assign   : Token name, Expression value",
                "Binary   : Expression left, Token operator, Expression right",
                "Call     : Expression callee, Token paren, List<Expression> arguments",
                "Get      : Expression object, Token name",
                "Grouping : Expression expression",
                "Literal  : Object value",
                "Logical  : Expression left, Token operator, Expression right",
                "Set      : Expression object, Token name, Expression value",
                "Super    : Token keyword, Token method",
                "This     : Token keyword",
                "Unary    : Token operator, Expression right",
                "Variable : Token name"
        ));

        defineAbstractSyntaxTree(outputDir, "Statement", Arrays.asList(
                "Block      : List<Statement> statements",
                "Class      : Token name, be.guldentops.geert.lox.grammar.Expression.Variable superclass, List<Statement.Function> methods",
                "Expression : be.guldentops.geert.lox.grammar.Expression expression",
                "Function   : Token name, List<Token> parameters, List<Statement> body",
                "If         : be.guldentops.geert.lox.grammar.Expression condition, Statement thenBranch, Statement elseBranch",
                "Print      : be.guldentops.geert.lox.grammar.Expression expression",
                "Return     : Token keyword, be.guldentops.geert.lox.grammar.Expression value",
                "Variable   : Token name, be.guldentops.geert.lox.grammar.Expression initializer",
                "While      : be.guldentops.geert.lox.grammar.Expression condition, Statement body"
        ));
    }

    private static void defineAbstractSyntaxTree(String outputDir,
                                                 String baseClassName,
                                                 List<String> types) {
        var path = outputDir + "/" + baseClassName + ".java";

        try (var writer = new PrintWriter(path, StandardCharsets.UTF_8)) {
            writeBaseClassHeader(baseClassName, writer);
            writeBaseClassBody(baseClassName, types, writer);
            writeBaseClassFooter(writer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeBaseClassHeader(String baseClassName, PrintWriter writer) {
        writer.println("package be.guldentops.geert.lox.grammar;");
        writer.println("");
        writer.println("import java.util.List;");
        writer.println("import be.guldentops.geert.lox.lexer.Token;");
        writer.println("");
        writer.printf("public sealed interface %s {", baseClassName);
        writer.println();
    }

    private static void writeBaseClassBody(String baseClassName, List<String> types, PrintWriter writer) {
        writeAbstractAcceptMethod(writer);
        writeVisitorInterface(writer, baseClassName, types);
        writeInnerClasses(baseClassName, types, writer);
    }

    private static void writeAbstractAcceptMethod(PrintWriter writer) {
        writer.println("");
        writer.println("  <R> R accept(Visitor<R> visitor);");
    }

    private static void writeVisitorInterface(PrintWriter writer, String baseClassName, List<String> types) {
        writer.println();
        writer.println(" interface Visitor<R> {");

        for (var type : types) {
            writer.println();
            var typeName = type.split(":")[0].trim();
            writer.printf("    R visit%s%s (%s  %s);", typeName, baseClassName, typeName, baseClassName.toLowerCase());
        }

        writer.println("  }");
    }

    private static void writeInnerClasses(String baseClassName, List<String> types, PrintWriter writer) {
        for (var type : types) {
            var className = type.split(":")[0].trim();
            var fields = type.split(":")[1].trim();
            defineType(writer, baseClassName, className, fields);
        }
    }

    private static void defineType(PrintWriter writer,
                                   String baseClassName,
                                   String className,
                                   String fieldList) {
        String[] fields = fieldList.split(", ");

        writeRecordHeader(writer, baseClassName, fields, className);
        writeAcceptMethodImplementation(writer, baseClassName, className);
        writeClassFooter(writer);
    }

    private static void writeRecordHeader(PrintWriter writer, String baseClassName, String[] fields, String className) {
        writer.println();
        writer.printf("record %s(%s) implements %s {", className, String.join(", ", fields), baseClassName);
        writer.println();
    }

    private static void writeAcceptMethodImplementation(PrintWriter writer, String baseClassName, String className) {
        writer.println();
        writer.println("    public <R> R accept(Visitor<R> visitor) {");
        writer.printf("      return visitor.visit%s%s(this);", className, baseClassName);
        writer.println();
        writer.println("    }");
    }

    private static void writeClassFooter(PrintWriter writer) {
        writer.println("  }");
    }

    private static void writeBaseClassFooter(PrintWriter writer) {
        writer.println("}");
    }
}
