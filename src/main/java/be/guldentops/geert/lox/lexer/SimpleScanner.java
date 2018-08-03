package be.guldentops.geert.lox.lexer;

import be.guldentops.geert.lox.error.Error;
import be.guldentops.geert.lox.error.ErrorReporter;
import be.guldentops.geert.lox.error.SyntaxError;
import be.guldentops.geert.lox.lexer.Token.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static be.guldentops.geert.lox.lexer.Token.Type.AND;
import static be.guldentops.geert.lox.lexer.Token.Type.BANG;
import static be.guldentops.geert.lox.lexer.Token.Type.BANG_EQUAL;
import static be.guldentops.geert.lox.lexer.Token.Type.CLASS;
import static be.guldentops.geert.lox.lexer.Token.Type.COMMA;
import static be.guldentops.geert.lox.lexer.Token.Type.DOT;
import static be.guldentops.geert.lox.lexer.Token.Type.ELSE;
import static be.guldentops.geert.lox.lexer.Token.Type.EOF;
import static be.guldentops.geert.lox.lexer.Token.Type.EQUAL;
import static be.guldentops.geert.lox.lexer.Token.Type.EQUAL_EQUAL;
import static be.guldentops.geert.lox.lexer.Token.Type.FALSE;
import static be.guldentops.geert.lox.lexer.Token.Type.FOR;
import static be.guldentops.geert.lox.lexer.Token.Type.FUN;
import static be.guldentops.geert.lox.lexer.Token.Type.GREATER;
import static be.guldentops.geert.lox.lexer.Token.Type.GREATER_EQUAL;
import static be.guldentops.geert.lox.lexer.Token.Type.IDENTIFIER;
import static be.guldentops.geert.lox.lexer.Token.Type.IF;
import static be.guldentops.geert.lox.lexer.Token.Type.LEFT_BRACE;
import static be.guldentops.geert.lox.lexer.Token.Type.LEFT_PAREN;
import static be.guldentops.geert.lox.lexer.Token.Type.LESS;
import static be.guldentops.geert.lox.lexer.Token.Type.LESS_EQUAL;
import static be.guldentops.geert.lox.lexer.Token.Type.MINUS;
import static be.guldentops.geert.lox.lexer.Token.Type.NIL;
import static be.guldentops.geert.lox.lexer.Token.Type.NUMBER;
import static be.guldentops.geert.lox.lexer.Token.Type.OR;
import static be.guldentops.geert.lox.lexer.Token.Type.PLUS;
import static be.guldentops.geert.lox.lexer.Token.Type.PRINT;
import static be.guldentops.geert.lox.lexer.Token.Type.RETURN;
import static be.guldentops.geert.lox.lexer.Token.Type.RIGHT_BRACE;
import static be.guldentops.geert.lox.lexer.Token.Type.RIGHT_PAREN;
import static be.guldentops.geert.lox.lexer.Token.Type.SEMICOLON;
import static be.guldentops.geert.lox.lexer.Token.Type.SLASH;
import static be.guldentops.geert.lox.lexer.Token.Type.STAR;
import static be.guldentops.geert.lox.lexer.Token.Type.STRING;
import static be.guldentops.geert.lox.lexer.Token.Type.SUPER;
import static be.guldentops.geert.lox.lexer.Token.Type.THIS;
import static be.guldentops.geert.lox.lexer.Token.Type.TRUE;
import static be.guldentops.geert.lox.lexer.Token.Type.VAR;
import static be.guldentops.geert.lox.lexer.Token.Type.WHILE;

class SimpleScanner implements Scanner {

    private static final Map<String, Type> keywords;

    static {
        keywords = new HashMap<>();
        keywords.put("and", AND);
        keywords.put("class", CLASS);
        keywords.put("else", ELSE);
        keywords.put("false", FALSE);
        keywords.put("for", FOR);
        keywords.put("fun", FUN);
        keywords.put("if", IF);
        keywords.put("nil", NIL);
        keywords.put("or", OR);
        keywords.put("print", PRINT);
        keywords.put("return", RETURN);
        keywords.put("super", SUPER);
        keywords.put("this", THIS);
        keywords.put("true", TRUE);
        keywords.put("var", VAR);
        keywords.put("while", WHILE);
    }

    private final String sourceCode;
    private final List<Token> tokens = new ArrayList<>();

    private int start = 0;
    private int current = 0;
    private int line = 1;

    private final List<ErrorReporter> errorReporters = new ArrayList<>();

    SimpleScanner(String sourceCode) {
        if (sourceCode == null) throw new IllegalArgumentException("source code should not be null!");

        this.sourceCode = sourceCode;
    }

    @Override
    public void addErrorReporter(ErrorReporter errorReporter) {
        this.errorReporters.add(errorReporter);
    }

    @Override
    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            // We are at the beginning of the next lexeme.
            start = current;
            scanToken();
        }

        tokens.add(new Token(EOF, "", null, line));
        return tokens;
    }

    private boolean isAtEnd() {
        return current >= sourceCode.length();
    }

    private void scanToken() {
        var nextChar = advance();

        switch (nextChar) {
            case '(':
                addToken(LEFT_PAREN);
                break;
            case ')':
                addToken(RIGHT_PAREN);
                break;
            case '{':
                addToken(LEFT_BRACE);
                break;
            case '}':
                addToken(RIGHT_BRACE);
                break;
            case ',':
                addToken(COMMA);
                break;
            case '.':
                addToken(DOT);
                break;
            case '-':
                addToken(MINUS);
                break;
            case '+':
                addToken(PLUS);
                break;
            case ';':
                addToken(SEMICOLON);
                break;
            case '*':
                addToken(STAR);
                break;
            case '!':
                addToken(match('=') ? BANG_EQUAL : BANG);
                break;
            case '=':
                addToken(match('=') ? EQUAL_EQUAL : EQUAL);
                break;
            case '<':
                addToken(match('=') ? LESS_EQUAL : LESS);
                break;
            case '>':
                addToken(match('=') ? GREATER_EQUAL : GREATER);
                break;
            case '/':
                if (match('/')) {
                    // A comment goes until the end of the line.
                    while (peek() != '\n' && !isAtEnd()) advance();
                } else {
                    addToken(SLASH);
                }
                break;
            case ' ':
            case '\r':
            case '\t':
                // Ignore whitespace.
                break;
            case '\n':
                line++;
                break;
            case '"':
                scanStringLiteral();
                break;
            default:
                if (isDigit(nextChar)) {
                    scanNumberLiteral();
                } else if (isAlpha(nextChar)) {
                    scanIdentifier();
                } else {
                    reportError(new SyntaxError(line, String.valueOf(nextChar), "unexpected character."));
                }
                break;
        }
    }

    private char advance() {
        current++;
        return sourceCode.charAt(current - 1);
    }

    private void addToken(Type type) {
        addToken(type, null);
    }

    private void addToken(Type type, Object literal) {
        var text = sourceCode.substring(start, current);
        tokens.add(new Token(type, text, literal, line));
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (sourceCode.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private char peek() {
        if (isAtEnd()) return '\0';

        return sourceCode.charAt(current);
    }

    private void scanStringLiteral() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        // Unterminated string.
        if (isAtEnd()) {
            reportError(new SyntaxError(line, "Unterminated string."));
        } else {
            // Consume the closing ".
            advance();

            addToken(STRING, trimSurroundingQuotes());
        }
    }

    private String trimSurroundingQuotes() {
        return sourceCode.substring(start + 1, current - 1);
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private void scanNumberLiteral() {
        while (isDigit(peek())) advance();

        if (isFractionalDigit()) {
            // Consume the "."
            advance();

            while (isDigit(peek())) advance();
        }

        addToken(NUMBER, Double.parseDouble(sourceCode.substring(start, current)));
    }

    private boolean isFractionalDigit() {
        return peek() == '.' && isDigit(peekNext());
    }

    private char peekNext() {
        if (current + 1 >= sourceCode.length()) return '\0';

        return sourceCode.charAt(current + 1);
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private void scanIdentifier() {
        while (isAlphaNumeric(peek())) advance();

        // See if the identifier is a reserved word.
        var text = sourceCode.substring(start, current);

        var type = keywords.get(text);
        if (type == null) type = IDENTIFIER;
        addToken(type);
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }

    private void reportError(Error error) {
        for (var errorReporter : errorReporters) {
            errorReporter.handle(error);
        }
    }
}
