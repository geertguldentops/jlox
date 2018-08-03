package be.guldentops.geert.lox.lexer;

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
import static be.guldentops.geert.lox.lexer.Token.Type.SUPER;
import static be.guldentops.geert.lox.lexer.Token.Type.THIS;
import static be.guldentops.geert.lox.lexer.Token.Type.TRUE;
import static be.guldentops.geert.lox.lexer.Token.Type.VAR;
import static be.guldentops.geert.lox.lexer.Token.Type.WHILE;

public class TokenObjectMother {

    private TokenObjectMother() {
    }

    public static Token leftBrace() {
        return new Token(LEFT_BRACE, "{", null, 1);
    }

    public static Token rightBrace() {
        return new Token(RIGHT_BRACE, "}", null, 1);
    }

    public static Token comma() {
        return new Token(COMMA, ",", null, 1);
    }

    public static Token print() {
        return new Token(PRINT, "print", null, 1);
    }

    public static Token var() {
        return new Token(VAR, "var", null, 1);
    }

    public static Token fun() {
        return new Token(FUN, "fun", null, 1);
    }

    public static Token identifier(String lexeme) {
        return new Token(IDENTIFIER, lexeme, null, 1);
    }

    public static Token _if() {
        return new Token(IF, "if", null, 1);
    }

    public static Token _else() {
        return new Token(ELSE, "else", null, 1);
    }

    public static Token _while() {
        return new Token(WHILE, "while", null, 1);
    }

    public static Token _for() {
        return new Token(FOR, "for", null, 1);
    }

    public static Token or() {
        return new Token(OR, "or", null, 1);
    }

    public static Token and() {
        return new Token(AND, "and", null, 1);
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

    public static Token integer(String integer) {
        return new Token(NUMBER, integer, Double.valueOf(integer), 1);
    }

    public static Token pi() {
        return new Token(NUMBER, "3.14", 3.14, 1);
    }

    public static Token _return() {
        return new Token(RETURN, "return", null, 1);
    }

    public static Token _class() {
        return new Token(CLASS, "class", null, 1);
    }

    public static Token _this() {
        return new Token(THIS, "this", null, 1);
    }

    public static Token _super() {
        return new Token(SUPER, "super", null, 1);
    }

    public static Token dot() {
        return new Token(DOT, ".", null, 1);
    }

    public static Token semicolon() {
        return new Token(SEMICOLON, ";", null, 1);
    }

    public static Token eof() {
        return new Token(EOF, "", null, 1);
    }
}