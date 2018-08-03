package be.guldentops.geert.lox.lexer.api;

import static be.guldentops.geert.lox.lexer.api.Token.Type.BANG;
import static be.guldentops.geert.lox.lexer.api.Token.Type.BANG_EQUAL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.EOF;
import static be.guldentops.geert.lox.lexer.api.Token.Type.EQUAL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.EQUAL_EQUAL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.FALSE;
import static be.guldentops.geert.lox.lexer.api.Token.Type.GREATER;
import static be.guldentops.geert.lox.lexer.api.Token.Type.GREATER_EQUAL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.IDENTIFIER;
import static be.guldentops.geert.lox.lexer.api.Token.Type.LEFT_BRACE;
import static be.guldentops.geert.lox.lexer.api.Token.Type.LEFT_PAREN;
import static be.guldentops.geert.lox.lexer.api.Token.Type.LESS;
import static be.guldentops.geert.lox.lexer.api.Token.Type.LESS_EQUAL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.MINUS;
import static be.guldentops.geert.lox.lexer.api.Token.Type.NIL;
import static be.guldentops.geert.lox.lexer.api.Token.Type.NUMBER;
import static be.guldentops.geert.lox.lexer.api.Token.Type.PLUS;
import static be.guldentops.geert.lox.lexer.api.Token.Type.PRINT;
import static be.guldentops.geert.lox.lexer.api.Token.Type.RIGHT_BRACE;
import static be.guldentops.geert.lox.lexer.api.Token.Type.RIGHT_PAREN;
import static be.guldentops.geert.lox.lexer.api.Token.Type.SEMICOLON;
import static be.guldentops.geert.lox.lexer.api.Token.Type.SLASH;
import static be.guldentops.geert.lox.lexer.api.Token.Type.STAR;
import static be.guldentops.geert.lox.lexer.api.Token.Type.TRUE;
import static be.guldentops.geert.lox.lexer.api.Token.Type.VAR;

public class TokenObjectMother {

    private TokenObjectMother() {
    }

    public static Token leftBrace() {
        return new Token(LEFT_BRACE, "{", null, 1);
    }

    public static Token rightBrace() {
        return new Token(RIGHT_BRACE, "}", null, 1);
    }

    public static Token print() {
        return new Token(PRINT, "print", null, 1);
    }

    public static Token var() {
        return new Token(VAR, "var", null, 1);
    }

    public static Token identifier(String lexeme) {
        return new Token(IDENTIFIER, lexeme, null, 1);
    }

    public static Token greater() {
        return new Token(GREATER, ">", null, 1);
    }

    public static Token greaterEqual() {
        return new Token(GREATER_EQUAL, ">=", null, 1);
    }

    public static Token less() {
        return new Token(LESS, "<", null, 1);
    }

    public static Token lessEqual() {
        return new Token(LESS_EQUAL, "<=", null, 1);
    }

    public static Token leftParen() {
        return new Token(LEFT_PAREN, "(", null, 1);
    }

    public static Token rightParen() {
        return new Token(RIGHT_PAREN, ")", null, 1);
    }

    public static Token bang() {
        return new Token(BANG, "!", null, 1);
    }

    public static Token bangEqual() {
        return new Token(BANG_EQUAL, "!=", null, 1);
    }

    public static Token equal() {
        return new Token(EQUAL, "=", null, 1);
    }

    public static Token equalEqual() {
        return new Token(EQUAL_EQUAL, "==", null, 1);
    }

    public static Token _false() {
        return new Token(FALSE, "false", null, 1);
    }

    public static Token _true() {
        return new Token(TRUE, "true", null, 1);
    }

    public static Token nil() {
        return new Token(NIL, "nil", null, 1);
    }

    public static Token plus() {
        return new Token(PLUS, "+", null, 1);
    }

    public static Token minus() {
        return new Token(MINUS, "-", null, 1);
    }

    public static Token star() {
        return new Token(STAR, "*", null, 1);
    }

    public static Token slash() {
        return new Token(SLASH, "/", null, 1);
    }

    public static Token one() {
        return new Token(NUMBER, "1", 1.0, 1);
    }

    public static Token two() {
        return new Token(NUMBER, "2", 2.0, 1);
    }

    public static Token pi() {
        return new Token(NUMBER, "3.14", 3.14, 1);
    }

    public static Token semicolon() {
        return new Token(SEMICOLON, ";", null, 1);
    }

    public static Token eof() {
        return new Token(EOF, "", null, 1);
    }
}